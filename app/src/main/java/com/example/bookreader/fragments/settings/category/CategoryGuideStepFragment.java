package com.example.bookreader.fragments.settings.category;

import static com.example.bookreader.constants.Constants.ACTION_ID_CANCEL;
import static com.example.bookreader.constants.Constants.ACTION_ID_DIVIDER;
import static com.example.bookreader.constants.Constants.ACTION_ID_ICON;
import static com.example.bookreader.constants.Constants.ACTION_ID_PARENT_CATEGORY;
import static com.example.bookreader.constants.Constants.ACTION_ID_SAVE;
import static com.example.bookreader.constants.Constants.ACTION_ID_SUBCATEGORY;

import android.content.Context;

import androidx.leanback.widget.GuidedAction;

import com.example.bookreader.R;
import com.example.bookreader.constants.ResourcesIcons;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.fragments.settings.BookGuidedStepFragment;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CategoryGuideStepFragment extends BookGuidedStepFragment {

    public  void updateCategoryActionChecked(CategoryDto category){
        GuidedAction categoryAction = findActionById(ACTION_ID_PARENT_CATEGORY);
        if(categoryAction != null){
            List<GuidedAction> actions = categoryAction.getSubActions();
            if(actions != null){
                actions.forEach(act->{
                    act.setChecked((category == null && act.getId() == -1)
                            || (category != null && Objects.equals(act.getId(), category.parentId)));
                });
            }
        }
    }

    public CompletableFuture<List<GuidedAction>> getCategoryActions(CategoryDto currentCategory, int actionId){
        Context context = getContext();
        CategoryRepository categoryRepository = new CategoryRepository();
        return  categoryRepository.getAllParentCategoriesAsyncCF().thenApply(categories->{
            List<GuidedAction> actions = categories.stream()
                    .map(cat->
                            new GuidedAction.Builder(context)
                                    .id(cat.id)
                                    .title(cat.name)
                                    .icon(cat.iconId)
                                    .hasNext(false)
                                    .checkSetId(actionId)
                                    .checked(currentCategory != null && cat.id == currentCategory.id)
                                    .build())
                    .collect(Collectors.toList());
            actions.add(0,new GuidedAction.Builder(context)
                    .id(-1)
                    .title("Не встановлено")
                    .hasNext(false)
                    .checkSetId(actionId)
                    .checked(currentCategory == null)
                    .build());
            return  actions;
        });
    }

    public List<GuidedAction> getSubCategoriesActions(List<CategoryDto> subCategories,Long subId){
        Context context = getContext();
        List<GuidedAction> actions = subCategories.stream().map(cat->
                        new GuidedAction.Builder(context)
                                .id(cat.id)
                                .title(cat.name)
                                .hasNext(false)
                                .icon(cat.iconId)
                                .checkSetId(ACTION_ID_SUBCATEGORY)
                                .checked(subId != null && cat.id == subId)
                                .build())
                .collect(Collectors.toList());

        actions.add(0,new GuidedAction.Builder(context)
                .id(-1)
                .title("Не встановлено")
                .hasNext(false)
                .checkSetId(ACTION_ID_SUBCATEGORY)
                .checked( subId == null)
                .build());
        return  actions;
    }

    public CompletableFuture<List<GuidedAction>> getSubCategoryActions(Long parentId,Long currentSubCategoryId){
        CategoryRepository categoryRepository = new CategoryRepository();
        if(parentId == null){
            return categoryRepository.getAllSubcategoriesAsyncCF().thenApply((subCategories)-> getSubCategoriesActions(subCategories,currentSubCategoryId));
        }
        return categoryRepository.getAllSubcategoriesByParentIdAsyncCF(parentId).thenApply((subCategories)-> getSubCategoriesActions(subCategories,currentSubCategoryId));
    }

    public List<GuidedAction> getIconsActions(CategoryDto currentCategory){
        return  Arrays.stream(ResourcesIcons.IconsArray).mapToObj(iconId->
                        new GuidedAction.Builder(getContext())
                                .id(iconId)
                                .checkSetId(ACTION_ID_ICON)
                                .icon(iconId)
                                .checked(iconId == currentCategory.iconId)
                                .build())

                .collect(Collectors.toList());
    }

}
