package com.example.bookreader.fragments.settings.booksettings;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.activities.BookDetailsActivity;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.utility.eventlistener.GlobalEventType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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


    public EditBookFragment(BookDto book) {
        this.book = book;
        title = book.title;
        year = book.year;
        author = book.author;
        setDefaultCategoryAndSubCategory();
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(
                "Редагування книги",
                book.title,
                "Змінюйте інформацію",
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
                .description("Теги")
                .hasNext(false)
                //.checkSetId(1)
                //.checked(true)
                .build());

        actions.add(
                new GuidedAction.Builder(context)
                        .id(ACTION_ID_CATEGORY)
                        .title("Категорія")
                        .description(getParentCategoryName())
                        .hasNext(false)
                        .subActions(getCategoryActions())
                        .build()
        );

        actions.add( new GuidedAction.Builder(context)
                .id(ACTION_ID_SUBCATEGORY)
                .title("Cубкатегорія")
                .description(getSubCategoryName())
                .hasNext(false)
                .subActions(
                        getSubCategoryActions(category != null ? category.id : null)
                )
                .build());
    }

    @Override
    public void onGuidedActionEditCanceled(@NonNull GuidedAction action) {
        processTextEditActions(action);
    }

    @Override
    public long onGuidedActionEditedAndProceed(@NonNull GuidedAction action) {
        processTextEditActions(action);
        updateActions();
        return ACTION_ID_NO_ACTION; // залишитись на поточній дії
    }

    @Override
    public boolean onSubGuidedActionClicked(GuidedAction action) {
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
                CategoryDto selectedCategory = app.getCategoriesCash().stream()
                        .filter(cat -> cat.id == action.getId())
                        .findFirst().orElse(null);
                if(parenActionId == ACTION_ID_SUBCATEGORY){
                    subCategory = selectedCategory;
                    if(selectedCategory != null){
                        category = app.getCategoriesCash().stream()
                                .filter(cat ->selectedCategory.parentId != null &&  cat.id == selectedCategory.parentId)
                                .findFirst().orElse(null);
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
        switch ((int) action.getId()) {
            case ACTION_ID_TAGS:
                // Відкриваємо діалог вибору тегів
//                TagSelectionDialog dialog = TagSelectionDialog.newInstance(new ArrayList<>(book.getTags()));
//                dialog.setTargetFragment(this, 100); // requestCode = 100
//                dialog.show(getFragmentManager(), "tag_dialog");
                break;
        }
    }

    public void setDefaultCategoryAndSubCategory(){
        CategoryDto category = app.getCategoriesCash().stream()
                .filter(cat->book.categoryId != null && cat.id == book.categoryId).findFirst().orElse(null);
        if(category != null){
            if(category.parentId == null){
                this.category = category;
            }
            else{
                this.subCategory = category;
                this.category = app.getCategoriesCash().stream()
                        .filter(cat->cat.id == category.parentId).findFirst().orElse(null);
            }
        }else{
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
        return !Objects.equals(title,book.title)
                || !Objects.equals(year,book.year)
                || !Objects.equals(author,book.author)
                || !Objects.equals(book.categoryId, getCurrentCategoryId());
    }

    public void updateCategoryActionChecked(){
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
            subCategoryAction.setSubActions(getSubCategoryActions(category != null ? category.id : null));
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

    private List<GuidedAction> getCategoryActions(){
        Context context = getContext();
        List<GuidedAction> actions = app.getCategoriesCash().stream()
                .filter(cat->cat.parentId == null)
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
    }

    private List<GuidedAction> getSubCategoryActions(Long parentId){
        Context context = getContext();
        List<GuidedAction> actions = app.getCategoriesCash().stream()
                .filter(cat->(parentId == null && cat.parentId != null) || (parentId != null && Objects.equals(cat.parentId,parentId)))
                .map(cat->
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
        setDefaultCategoryAndSubCategory();
        updateCategoryActionDescription(category,ACTION_ID_CATEGORY);
        updateCategoryActionDescription(subCategory,ACTION_ID_SUBCATEGORY);
        updateCategoryActionChecked();
        updateSubcategorySubActions();
    }

    private void processTextEditActions(GuidedAction action){
        switch ((int) action.getId()) {
            case ACTION_ID_TITLE:
                title = action.getDescription().toString();
                break;
            case ACTION_ID_YEAR:
                year = action.getDescription().toString();
                break;
            case ACTION_ID_AUTHOR:
                author =  action.getDescription().toString();
                break;
        }
        checkChangedAndAddControls();
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
                app.getGlobalEventListener().sendEvent(GlobalEventType.BOOK_UPDATED,book);
                closeFragment();
            });
        }
        else{
            app.getGlobalEventListener().sendEvent(GlobalEventType.LOAD_BOOK_UPDATED,book);
            closeFragment();
        }

    }

    private void closeFragment(){
        requireActivity().runOnUiThread(()->{
            Toast.makeText(getContext(), "Збережено!", Toast.LENGTH_SHORT).show();
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
}

