package com.example.bookreader.fragments.settings.booksettings;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.example.bookreader.R;
import com.example.bookreader.customclassses.BookCategories;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class CategorySelectFragment extends GuidedStepSupportFragment {
    private final BookCategories oldBookCategories;
    private final BookCategories bookCategories;
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private static final int ACTION_ID_CATEGORY = 111111119;
    private static final int ACTION_ID_SUBCATEGORY = 111111120;
    private static final int ACTION_ID_SAVE = 111111114;
    private static final int ACTION_ID_CANCEL = 111111116;

    public CategorySelectFragment(BookCategories categories){
        this.bookCategories = categories;
        this.oldBookCategories = new BookCategories(categories.category,categories.subCategory);
    }


    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(
                "Оберіть категорію",
                "Категорії можуть мати рідкатегорії",
                "Змінюйте інформацію",
                ContextCompat.getDrawable(requireContext(), R.drawable.book_tag)
        );
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        Context context = getContext();

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
                        getSubCategoryActions(bookCategories.category != null ? bookCategories.category.id : null).join()
                )
                .build());
    }

    @Override
    public boolean onSubGuidedActionClicked(@NonNull GuidedAction action) {
        int parenActionId = action.getCheckSetId();
        CategoryDto selectedCategory = categoryRepository.getCategoryByIdAsyncCF(action.getId()).join();
        if(parenActionId == ACTION_ID_SUBCATEGORY){
            bookCategories.subCategory = selectedCategory;
            if(selectedCategory != null && selectedCategory.parentId != null){
                bookCategories.category = categoryRepository.getCategoryByIdAsyncCF(selectedCategory.parentId).join();
                updateCategoryActionChecked();
            }
        }
        else{
            bookCategories.category = selectedCategory;
            bookCategories.subCategory = null;
            updateSubcategoryActions();
        }
        updateDescriptions();
        checkChangedAndAddControls();
       return true;
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        switch ((int) action.getId()) {
            case ACTION_ID_SAVE:
                Bundle result = new Bundle();
                result.putSerializable("categories", bookCategories);
                getParentFragmentManager().setFragmentResult("categories_result", result);
                getParentFragmentManager().popBackStack();
                break;
            case ACTION_ID_CANCEL:
                setDefault();
                break;
        }
    }

    private void updateDescriptions(){
        updateCategoryActionDescription(bookCategories.category,ACTION_ID_CATEGORY);
        updateCategoryActionDescription(bookCategories.subCategory,ACTION_ID_SUBCATEGORY);
    }

    private void setDefault(){
        bookCategories.category = oldBookCategories.category;
        bookCategories.subCategory = oldBookCategories.subCategory;
        updateDescriptions();
        removeConfirmAction();
        updateSubcategoryActions();
        updateCategoryActionChecked();
    }

    private void  checkChangedAndAddControls(){
        if(isCategoryChanged()){
            addConfirmActions();
        }
        else{
            removeConfirmAction();
        }
    }

    private void removeConfirmAction(){
        List<GuidedAction>  actions = new ArrayList<>(getActions());
        if(actions.removeIf(act->act.getId() == ACTION_ID_SAVE)){
            actions.removeIf(act->act.getId() ==  ACTION_ID_CANCEL);
            setActions(actions);
        }
    }

    private void addConfirmActions(){
        List<GuidedAction>  actions = new ArrayList<>(getActions());
        if(actions.stream().noneMatch(act->act.getId() == ACTION_ID_SAVE)){
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
            actions.addAll(controlActions);
            setActions(actions);
        }
    }

    private boolean isCategoryChanged(){
        Long oldCatId  = oldBookCategories.category    != null ? oldBookCategories.category.id    : null;
        Long oldSubId  = oldBookCategories.subCategory != null ? oldBookCategories.subCategory.id : null;
        Long newCatId  = bookCategories.category       != null ? bookCategories.category.id       : null;
        Long newSubId  = bookCategories.subCategory    != null ? bookCategories.subCategory.id    : null;

        return !Objects.equals(oldCatId, newCatId) || !Objects.equals(oldSubId, newSubId);
    }

    private void updateCategoryActionDescription(CategoryDto category,int actionId){
        GuidedAction categoryAction = findActionById(actionId);
        if(categoryAction != null) {
            categoryAction.setDescription(category != null ? category.name : "Не встановлено");
            notifyActionChanged(getActions().indexOf(categoryAction));
        }
    }

    private void updateSubcategoryActions(){
        GuidedAction subCategoryAction = findActionById(ACTION_ID_SUBCATEGORY);
        if(subCategoryAction != null){
            subCategoryAction.setSubActions(getSubCategoryActions(bookCategories.category != null ? bookCategories.category.id : null).join());
            notifyActionChanged(getActions().indexOf(subCategoryAction));
        }
    }

    public  void updateCategoryActionChecked(){
        GuidedAction categoryAction = findActionById(ACTION_ID_CATEGORY);
        if(categoryAction != null){
            List<GuidedAction> categorySubActions = categoryAction.getSubActions();
            if(categorySubActions != null){
                categorySubActions.forEach(act->{
                    act.setChecked((bookCategories.subCategory == null && act.getId() == -1)
                            || (bookCategories.subCategory != null && act.getId() == bookCategories.subCategory.parentId));
                });
            }

        }
    }

    private CompletableFuture<List<GuidedAction>> getSubCategoryActions(Long parentId){
        if(parentId == null){
            return categoryRepository.getAllSubcategoriesAsyncCF().thenApply(this::getSubCategoriesActions);
        }
        return categoryRepository.getAllSubcategoriesByParentIdAsyncCF(parentId).thenApply(this::getSubCategoriesActions);
    }

    private List<GuidedAction> getSubCategoriesActions(List<CategoryDto> subCategories){
        Context context = getContext();
        List<GuidedAction> actions = subCategories.stream().map(cat->
                        new GuidedAction.Builder(context)
                                .id(cat.id)
                                .title(cat.name)
                                .hasNext(false)
                                .checkSetId(ACTION_ID_SUBCATEGORY)
                                .checked(bookCategories.subCategory != null && cat.id == bookCategories.subCategory.id)
                                .build())
                .collect(Collectors.toList());

        actions.add(0,new GuidedAction.Builder(context)
                .id(-1)
                .title("Не встановлено")
                .hasNext(false)
                .checkSetId(ACTION_ID_SUBCATEGORY)
                .checked(bookCategories.subCategory == null)
                .build());
        return  actions;
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
                                    .checked(bookCategories.category != null && cat.id == bookCategories.category.id)
                                    .build())
                    .collect(Collectors.toList());
            actions.add(0,new GuidedAction.Builder(context)
                    .id(-1)
                    .title("Не встановлено")
                    .hasNext(false)
                    .checkSetId(ACTION_ID_CATEGORY)
                    .checked(bookCategories.category == null)
                    .build());
            return  actions;
        });
    }

    private String getParentCategoryName(){
        return bookCategories.category != null ? bookCategories.category.name : "Не встановлено";
    }

    private String getSubCategoryName(){
        return bookCategories.subCategory != null ? bookCategories.subCategory.name : "Не встановлено";
    }

}
