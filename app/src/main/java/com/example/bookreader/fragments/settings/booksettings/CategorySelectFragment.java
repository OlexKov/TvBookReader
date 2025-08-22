package com.example.bookreader.fragments.settings.booksettings;

import static com.example.bookreader.constants.Constants.ACTION_ID_ADD_CATEGORY;
import static com.example.bookreader.constants.Constants.ACTION_ID_CANCEL;
import static com.example.bookreader.constants.Constants.ACTION_ID_CATEGORY;
import static com.example.bookreader.constants.Constants.ACTION_ID_SAVE;
import static com.example.bookreader.constants.Constants.ACTION_ID_SUBCATEGORY;


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
import com.example.bookreader.extentions.BookGuidedStepFragment;
import com.example.bookreader.fragments.settings.categorysetting.CategoryCreateFragment;

import java.util.List;
import java.util.Objects;

public class CategorySelectFragment extends BookGuidedStepFragment {
    private final BookCategories oldBookCategories;
    private final BookCategories bookCategories;
    private final CategoryRepository categoryRepository = new CategoryRepository();

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
        getParentFragmentManager().setFragmentResultListener(
                "new_category_result",
                this,
                (requestKey, bundle) -> {
                    var data = bundle.getSerializable("new_category");
                    if(data instanceof CategoryDto newCategory){
                        if(newCategory.parentId == null){
                            updateCategoryActions();
                        }
                        else{
                            updateSubcategoryActions();
                        }
                    }
                }
        );
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        Context context = getContext();

        actions.add(
                new GuidedAction.Builder(context)
                        .id(ACTION_ID_ADD_CATEGORY)
                        .title("Додати категорію")
                        .icon(R.drawable.add)
                        .build()
        );

        actions.add(
                new GuidedAction.Builder(context)
                        .id(ACTION_ID_CATEGORY)
                        .title("Категорія")
                        .description(getParentCategoryName())
                        .hasNext(false)
                        .subActions(getCategoryActions(bookCategories.category,ACTION_ID_CATEGORY).join())
                        .build()
        );

        actions.add( new GuidedAction.Builder(context)
                .id(ACTION_ID_SUBCATEGORY)
                .title("Cубкатегорія")
                .description(getSubCategoryName())
                .hasNext(false)
                .subActions(
                        getSubCategoryActions(bookCategories.category != null ? bookCategories.category.id : null,
                                bookCategories.subCategory != null ? bookCategories.subCategory.id : null).join()
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
                updateCategoryActionChecked(bookCategories.subCategory);
            }
        }
        else{
            bookCategories.category = selectedCategory;
            bookCategories.subCategory = null;
            updateSubcategoryActions();
        }
        updateDescriptions();
        checkChangedAndAddControls(isCategoryChanged());
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
            case ACTION_ID_ADD_CATEGORY:
                GuidedStepSupportFragment.add(getParentFragmentManager(),
                        new CategoryCreateFragment(null));
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
        updateCategoryActionChecked(bookCategories.subCategory);
    }

    private boolean isCategoryChanged(){
        Long oldCatId  = oldBookCategories.category    != null ? oldBookCategories.category.id    : null;
        Long oldSubId  = oldBookCategories.subCategory != null ? oldBookCategories.subCategory.id : null;
        Long newCatId  = bookCategories.category       != null ? bookCategories.category.id       : null;
        Long newSubId  = bookCategories.subCategory    != null ? bookCategories.subCategory.id    : null;

        return !Objects.equals(oldCatId, newCatId) || !Objects.equals(oldSubId, newSubId);
    }

    private void updateCategoryActionDescription(CategoryDto category,int actionId){
        setDescription(category != null ? category.name : "Не встановлено",actionId);
    }

    private void updateSubcategoryActions(){
        List<GuidedAction> actions  = getSubCategoryActions(
                bookCategories.category != null ? bookCategories.category.id : null,
                bookCategories.subCategory != null ? bookCategories.subCategory.id : null).join();
        updateActions(actions,ACTION_ID_SUBCATEGORY);
    }

    private void updateCategoryActions(){
        List<GuidedAction> actions  = getCategoryActions(bookCategories.category,ACTION_ID_CATEGORY).join();
        updateActions(actions,ACTION_ID_CATEGORY);
    }

    private String getParentCategoryName(){
        return bookCategories.category != null ? bookCategories.category.name : "Не встановлено";
    }

    private String getSubCategoryName(){
        return bookCategories.subCategory != null ? bookCategories.subCategory.name : "Не встановлено";
    }

}
