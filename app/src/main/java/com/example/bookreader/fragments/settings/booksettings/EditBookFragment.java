package com.example.bookreader.fragments.settings.booksettings;



import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.activities.BookDetailsActivity;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.dto.TagDto;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.data.database.repository.TagRepository;
import com.example.bookreader.utility.eventlistener.GlobalEventListener;
import com.example.bookreader.utility.eventlistener.GlobalEventType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EditBookFragment extends GuidedStepSupportFragment {
    private final BookDto book;
    private static final int ACTION_ID_TITLE = 1111111111;
    private static final int ACTION_ID_YEAR = 111111112;
    private static final int ACTION_ID_TAGS = 111111113;
    private static final int ACTION_ID_SAVE = 111111114;
    private static final int ACTION_ID_AUTHOR = 111111115;
    private static final int ACTION_ID_CANCEL = 111111116;
    private static final int ACTION_ID_CONTROL_ACTIONS = 111111117;
    private static final int ACTION_ID_CATEGORY = 111111119;
    private static final int ACTION_ID_SUBCATEGORY = 111111120;
    private static final long ACTION_ID_NO_ACTION = -1;

    private final BookReaderApp app = BookReaderApp.getInstance();
    private String title;
    private String year;
    private String author;
    private CategoryDto category;
    private CategoryDto subCategory;
    private List<Long> currentBookTagsIds;
    private final List<Long> bookTagsIds;
    private final String defaultTagsDescription;
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final TagRepository tagRepository = new TagRepository();
    private final GlobalEventListener globalEventListener = app.getGlobalEventListener();


    public EditBookFragment(BookDto book) {
        this.book = book;
        title = book.title;
        year = book.year;
        author = book.author;
        List<TagDto> tags = book.tagsIds.isEmpty()
                ? tagRepository.getByBookIdAsync(book.id).join()
                : tagRepository.getByIdsAsync(book.tagsIds).join() ;
        defaultTagsDescription = tags.isEmpty() ? "Не встановлено" : tags.stream().map(tag->tag.name).collect(Collectors.joining(" | "));
        this.bookTagsIds = tags.stream().map(tag->tag.id).collect(Collectors.toList());
        currentBookTagsIds = new ArrayList<>(this.bookTagsIds);
        setDefaultCategoryAndSubCategory();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(
                "tags_result",
                this,
                (requestKey, bundle) -> {
                    long[] tagsIds = bundle.getLongArray("tags");
                    currentBookTagsIds = tagsIds != null
                            ? Arrays.stream(tagsIds)
                            .boxed()
                            .collect(Collectors.toList())
                            : new ArrayList<>();
                    checkChangedAndAddControls();
                    updateCurrentTagDescription(currentBookTagsIds);
                }
        );
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(
                "Редагування книги",
                "Змінюйте інформацію",
                book.title,
                Drawable.createFromPath(book.previewPath)
        );
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        Context context = getContext();
        actions.add(new GuidedAction.Builder(context)
                .id(ACTION_ID_TITLE)
                .title("Назва")
                .description(book.title)
                .descriptionEditable(true)
                .multilineDescription(true)
                .hasNext(false)
                .build());

        actions.add(new GuidedAction.Builder(context)
                .id(ACTION_ID_YEAR)
                .title("Рік")
                .description(book.year)
                .descriptionEditable(true)
                .descriptionEditInputType(InputType.TYPE_CLASS_NUMBER)
                .hasNext(false)
                .build());

        actions.add(new GuidedAction.Builder(context)
                .id(ACTION_ID_AUTHOR)
                .title("Автор")
                .description(book.author)
                .descriptionEditable(true)
                .multilineDescription(true)
                .hasNext(false)
                .build());

        actions.add(new GuidedAction.Builder(context)
                .id(ACTION_ID_TAGS)
                .title("Теги")
                .description(defaultTagsDescription)
                .build());

        actions.add(
                new GuidedAction.Builder(context)
                        .id(ACTION_ID_CATEGORY)
                        .title("Категорія")
                        .description(getParentCategoryName())
                        .hasNext(false)
                        .subActions(getCategoryActions().join())
                        .build()
        );

        actions.add( new GuidedAction.Builder(context)
                .id(ACTION_ID_SUBCATEGORY)
                .title("Cубкатегорія")
                .description(getSubCategoryName())
                .hasNext(false)
                .subActions(
                        getSubCategoryActions(category != null ? category.id : null).join()
                )
                .build());
    }

    @Override
    public void onGuidedActionEditCanceled(@NonNull GuidedAction action) {
        switch ((int) action.getId()) {
            case ACTION_ID_TITLE:
                action.setDescription(title);
                break;
            case ACTION_ID_YEAR:
                action.setDescription(year);
                break;
            case ACTION_ID_AUTHOR:
                action.setDescription(author);
                break;
        }
    }

    @Override
    public long onGuidedActionEditedAndProceed(@NonNull GuidedAction action) {
        processTextEditActions(action);
        checkChangedAndAddControls();
        updateActions();
        return ACTION_ID_NO_ACTION; // залишитись на поточній дії
    }

    @Override
    public boolean onSubGuidedActionClicked(@NonNull GuidedAction action) {
        switch ((int) action.getId()) {
            case ACTION_ID_SAVE:
                saveToDatabase();
                break;
            case ACTION_ID_CANCEL:
                setDefault();
                removeControlActions();
                break;

            default:
                int parenActionId = action.getCheckSetId();
                CategoryDto selectedCategory = categoryRepository.getCategoryByIdAsyncCF(action.getId()).join();
                if(parenActionId == ACTION_ID_SUBCATEGORY){
                    subCategory = selectedCategory;
                    if(selectedCategory != null && selectedCategory.parentId != null){
                        category = categoryRepository.getCategoryByIdAsyncCF(selectedCategory.parentId).join();
                        updateCategoryActionChecked();
                    }
                }
                else{
                    category = selectedCategory;
                    subCategory = null;
                    updateSubcategorySubActions();
                }
                updateCategoryActionDescription(category,ACTION_ID_CATEGORY);
                updateCategoryActionDescription(subCategory,ACTION_ID_SUBCATEGORY);
                checkChangedAndAddControls();
                break;
        }
        return true;
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if ((int) action.getId() == ACTION_ID_TAGS) {
            GuidedStepSupportFragment.add(getParentFragmentManager(),
                    new TagsSelectFragment(currentBookTagsIds));
        }
    }

    public void setDefaultCategoryAndSubCategory(){
        if(book.categoryId != null){
            categoryRepository.getCategoryByIdAsyncCF(book.categoryId).thenAccept((category)->{
                if(category.parentId == null){
                    this.category = category;
                }
                else{
                    this.subCategory = category;
                    categoryRepository.getCategoryByIdAsyncCF(category.parentId).thenAccept(parentCategory->{
                        this.category = parentCategory;
                    });
                }
            });
        }
        else{
            this.subCategory = null;
            this.category = null;
        }
    }

    private void checkChangedAndAddControls(){
        if(isSettingChanged()){
            addControlActions();
        }
        else{
            removeControlActions();
        }
    }

    private Long getCurrentCategoryId() {
        return subCategory != null ? Long.valueOf(subCategory.id) : (category != null ? category.id : null);
    }

    private boolean isSettingChanged(){
        return     !Objects.equals(title,book.title)
                || !Objects.equals(year,book.year)
                || !Objects.equals(author,book.author)
                || !Objects.equals(book.categoryId, getCurrentCategoryId())
                || isTagsChanged();
    }

    public  void updateCategoryActionChecked(){
        GuidedAction categoryAction = findActionById(ACTION_ID_CATEGORY);
        if(categoryAction != null){
            List<GuidedAction> categorySubActions = categoryAction.getSubActions();
            if(categorySubActions != null){
                categorySubActions.forEach(act->{
                    act.setChecked((subCategory == null && act.getId() == -1) || (subCategory != null && act.getId() == subCategory.parentId));
                });
            }

        }
    }

    private void updateSubcategorySubActions(){
        GuidedAction subCategoryAction = findActionById(ACTION_ID_SUBCATEGORY);
        if(subCategoryAction != null){
            subCategoryAction.setSubActions(getSubCategoryActions(category != null ? category.id : null).join());
            notifyActionChanged(getActions().indexOf(subCategoryAction));
        }
    }

    private void updateCategoryActionDescription(CategoryDto category,int actionId){
        GuidedAction categoryAction = findActionById(actionId);
        if(categoryAction != null) {
            categoryAction.setDescription(category != null ? category.name : "Не встановлено");
            notifyActionChanged(getActions().indexOf(categoryAction));
        }
    }

    private CompletableFuture<List<GuidedAction>> getCategoryActions(){
        Context context = getContext();
        return  categoryRepository.getAllParentCategoriesAsyncCF().thenApply(categories->{
            List<GuidedAction> actions = categories.stream()
                      .map(cat->
                            new GuidedAction.Builder(context)
                                    .id(cat.id)
                                    .title(cat.name)
                                    .hasNext(false)
                                    .checkSetId(ACTION_ID_CATEGORY)
                                    .checked(category != null && cat.id == category.id)
                                    .build())
                    .collect(Collectors.toList());
            actions.add(0,new GuidedAction.Builder(context)
                    .id(-1)
                    .title("Не встановлено")
                    .hasNext(false)
                    .checkSetId(ACTION_ID_CATEGORY)
                    .checked(category == null)
                    .build());
            return  actions;
        });
    }

    private CompletableFuture<List<GuidedAction>> getSubCategoryActions(Long parentId){
        if(parentId == null){
            return categoryRepository.getAllSubcategoriesAsyncCF().thenApply(this::getSubCategoriesActions);
        }
        return categoryRepository.getAllSubcategoriesByParentIdAsyncCF(parentId).thenApply(this::getSubCategoriesActions);
    }

    private boolean isTagsChanged(){
        return !new HashSet<>(bookTagsIds).equals(new HashSet<>(currentBookTagsIds));
    }

    private List<GuidedAction> getSubCategoriesActions(List<CategoryDto> subCategories){
        Context context = getContext();
        List<GuidedAction> actions = subCategories.stream().map(cat->
                        new GuidedAction.Builder(context)
                                .id(cat.id)
                                .title(cat.name)
                                .hasNext(false)
                                .checkSetId(ACTION_ID_SUBCATEGORY)
                                .checked(subCategory != null && cat.id == subCategory.id)
                                .build())
                .collect(Collectors.toList());

        actions.add(0,new GuidedAction.Builder(context)
                .id(-1)
                .title("Не встановлено")
                .hasNext(false)
                .checkSetId(ACTION_ID_SUBCATEGORY)
                .checked(subCategory == null)
                .build());
        return  actions;
    }

    private String getParentCategoryName(){
        return category != null ? category.name : "Не встановлено";
    }

    private String getSubCategoryName(){
        return subCategory != null ? subCategory.name : "Не встановлено";
    }

    private void setDefault(){
        GuidedAction action = findActionById(ACTION_ID_AUTHOR);
        if(action != null){
            action.setDescription(book.author);
            notifyActionChanged(2);
        }

        action = findActionById(ACTION_ID_TITLE);
        if(action != null){
            action.setDescription(book.title);
            notifyActionChanged(0);
        }

        action = findActionById(ACTION_ID_YEAR);
        if(action != null){
            action.setDescription(book.year);
            notifyActionChanged(1);
        }

        action = findActionById(ACTION_ID_TAGS);
        if(action != null){
            setDefaultTags(action);
        }

        setDefaultCategoryAndSubCategory();
        updateCategoryActionDescription(category,ACTION_ID_CATEGORY);
        updateCategoryActionDescription(subCategory,ACTION_ID_SUBCATEGORY);
        updateCategoryActionChecked();
        updateSubcategorySubActions();
    }

    private void processTextEditActions(GuidedAction action){
        var description = action.getDescription();
        if(description != null){
            String descriptionString = description.toString();
            switch ((int) action.getId()) {
                case ACTION_ID_TITLE:
                    title = descriptionString;
                    break;
                case ACTION_ID_YEAR:
                    year = descriptionString;
                    break;
                case ACTION_ID_AUTHOR:
                    author =  descriptionString;
                    break;
            }
        }

    }

    private void removeControlActions(){
        List<GuidedAction>  actions = new ArrayList<>(getActions());
        if(actions.stream().anyMatch(act->act.getId() == ACTION_ID_CONTROL_ACTIONS)){
            actions = actions.stream().filter(act->act.getId() != ACTION_ID_CONTROL_ACTIONS)
                    .collect(Collectors.toList());
            setActions(actions);
        }
    }

    private void addControlActions(){
        List<GuidedAction>  actions = new ArrayList<>(getActions());
        if(actions.stream().noneMatch(act->act.getId() == ACTION_ID_CONTROL_ACTIONS)){
            Context context = getContext();
            List<GuidedAction> controlActions = List.of(
                    new GuidedAction.Builder(context)
                            .id(ACTION_ID_SAVE)
                            .title(getString(R.string.save))
                            .build(),
                    new GuidedAction.Builder(context)
                            .id(ACTION_ID_CANCEL)
                            .title(getString(R.string.cancel))
                            .build());
            actions.add(
                    new GuidedAction.Builder(context)
                            .id(ACTION_ID_CONTROL_ACTIONS)
                            .title("Завершити")
                            .description(getString(R.string.save)+" "+getString(R.string.cancel))
                            .subActions(controlActions)
                            .build());
            setActions(actions);
        }
    }

    private void saveToDatabase(){
        book.title = title;
        book.author = author;
        book.year = year;
        Long oldCategoryId = book.categoryId;
        if(subCategory == null && category == null){
            book.categoryId = null;
        }
        else{
            book.categoryId  = subCategory == null ? category.id : subCategory.id;
        }


        if(requireActivity() instanceof BookDetailsActivity){
            Book updatedBook = book.getBook();
            BookRepository bookRepository = new BookRepository();
            bookRepository.updateAndGetAsync(updatedBook).thenAccept((book)->{
                if(!Objects.equals(book.categoryId , oldCategoryId)){
                    globalEventListener.sendEvent(GlobalEventType.BOOK_CATEGORY_CHANGED,book);
                }else{
                    globalEventListener.sendEvent(GlobalEventType.BOOK_UPDATED,book);
                }
                globalEventListener.sendEvent(GlobalEventType.UPDATE_BOOK_DETAILS,book);
            });

            if(isTagsChanged()){
                List<Long> existingTagIds = new ArrayList<>(bookTagsIds);
                existingTagIds.retainAll(currentBookTagsIds);
                existingTagIds.forEach(id->{
                    bookTagsIds.remove(id);
                    currentBookTagsIds.remove(id);
                });
                tagRepository.removeTagsFromBookAsync(bookTagsIds,book.id).thenAccept(count->{
                    currentBookTagsIds.forEach(tagId->{
                        tagRepository.addTagToBook(tagId,book.id);
                    });
                    globalEventListener.sendEvent(GlobalEventType.BOOK_TAGS_CHANGED,book);
                });
            }
        }
        else{
           book.tagsIds = new ArrayList<>(currentBookTagsIds);
        }
        Bundle result = new Bundle();
        result.putSerializable("updated_book", book);
        getParentFragmentManager().setFragmentResult("book_edit_result", result);
        closeFragment();
    }

    private void closeFragment(){
        requireActivity().runOnUiThread(()->{
            Toast.makeText(getContext(), "Зміни Збережено!", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        });
    }

    private void updateActions() {
        // Оновити опис дії для тегів, щоб показати актуальні вибрані теги
        GuidedAction tagAction = findActionById(ACTION_ID_TAGS);
        if (tagAction != null) {
            //  tagAction.setDescription(TextUtils.join(", ", book.getTags()));
            //  notifyActionChanged(tagAction);
        }
    }

    private void setDefaultTags(GuidedAction tagAction){
        tagAction.setDescription(defaultTagsDescription);
        currentBookTagsIds = new ArrayList<>(bookTagsIds);
        notifyActionChanged(3);
    }

    private void updateCurrentTagDescription(List<Long> tagsIds){
        tagRepository.getByIdsAsync(tagsIds).thenAccept((tags)->{
            String description = tagsIds.isEmpty()
                    ? "Не встановлено"
                    : tags.stream().map(tag->tag.name).collect(Collectors.joining(" | "));
            var action = findActionById(ACTION_ID_TAGS);
            if(action != null){
                action.setDescription(description);
                notifyActionChanged(3);
            }
        });
    }

}

