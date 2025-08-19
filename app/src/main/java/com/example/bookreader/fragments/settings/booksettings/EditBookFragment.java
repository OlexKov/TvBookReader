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
import com.example.bookreader.customclassses.BookCategories;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.dto.TagDto;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.data.database.repository.TagRepository;
import com.example.bookreader.extentions.BookGuidedStepFragment;
import com.example.bookreader.utility.eventlistener.GlobalEventListener;
import com.example.bookreader.utility.eventlistener.GlobalEventType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EditBookFragment extends BookGuidedStepFragment {
    private final BookDto book;
    private static final int ACTION_ID_TITLE = 1111111111;
    private static final int ACTION_ID_YEAR = 111111112;
    private static final int ACTION_ID_TAGS = 111111113;
    private static final int ACTION_ID_SAVE = 111111114;
    private static final int ACTION_ID_AUTHOR = 111111115;
    private static final int ACTION_ID_CANCEL = 111111116;
    private static final int ACTION_ID_CATEGORY = 111111119;
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

        getParentFragmentManager().setFragmentResultListener(
                "categories_result",
                this,
                (requestKey, bundle) -> {
                    var data = bundle.getSerializable("categories");
                    if(data instanceof BookCategories categories ){
                        category = categories.category;
                        subCategory = categories.subCategory;
                        updateCategoriesActionDescription();
                        checkChangedAndAddControls();
                    }
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
                        .description(getCategoryDescription())
                        .build()
        );
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
        return ACTION_ID_NO_ACTION; // залишитись на поточній дії
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        switch ((int) action.getId()) {
            case ACTION_ID_SAVE:
                saveToDatabase();
                break;
            case ACTION_ID_CANCEL:
                setDefault();
                removeControlActions();
                break;
            case ACTION_ID_TAGS:
                GuidedStepSupportFragment.add(getParentFragmentManager(),
                        new TagsSelectFragment(currentBookTagsIds));
                break;
            case ACTION_ID_CATEGORY:
                GuidedStepSupportFragment.add(getParentFragmentManager(),
                        new CategorySelectFragment(new BookCategories(category, subCategory)));
                break;
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

    private void updateCategoriesActionDescription(){
        GuidedAction action = findActionById(ACTION_ID_CATEGORY);
        if(action != null){
            action.setDescription(getCategoryDescription());
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

    private boolean isTagsChanged(){
        return !new HashSet<>(bookTagsIds).equals(new HashSet<>(currentBookTagsIds));
    }

    private String getCategoryDescription(){
        String categoryText = "";
        if(category != null){
            categoryText += category.name + " -> ";
        }

        if(subCategory != null){
            categoryText += subCategory.name;
        }
        return !categoryText.isBlank() ? categoryText : "Не встановлено";
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
        updateCategoriesActionDescription();
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
        if(actions.removeIf(act->act.getId() == ACTION_ID_SAVE)){
            actions.removeIf(act->act.getId() ==  ACTION_ID_CANCEL);
            setActions(actions);
        }
    }

    private void addControlActions(){
        List<GuidedAction>  actions = new ArrayList<>(getActions());
        if(actions.stream().noneMatch(act->act.getId() == ACTION_ID_SAVE)){
            Context context = getContext();
            List<GuidedAction> controlActions = List.of(
                    new GuidedAction.Builder(context)
                            .id(ACTION_ID_SAVE)
                            .title(getString(R.string.save))
                            .icon(R.drawable.save)
                            .build(),
                    new GuidedAction.Builder(context)
                            .id(ACTION_ID_CANCEL)
                            .title(getString(R.string.cancel))
                            .icon(R.drawable.redo)
                            .build());
            actions.addAll(controlActions);
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

