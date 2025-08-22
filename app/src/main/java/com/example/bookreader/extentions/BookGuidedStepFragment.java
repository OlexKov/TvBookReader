package com.example.bookreader.extentions;

import static com.example.bookreader.constants.Constants.ACTION_ID_CANCEL;
import static com.example.bookreader.constants.Constants.ACTION_ID_CLEAR_TAGS;
import static com.example.bookreader.constants.Constants.ACTION_ID_DIVIDER;
import static com.example.bookreader.constants.Constants.ACTION_ID_ICON;
import static com.example.bookreader.constants.Constants.ACTION_ID_NEW_TAG;
import static com.example.bookreader.constants.Constants.ACTION_ID_PARENT_CATEGORY;
import static com.example.bookreader.constants.Constants.ACTION_ID_SAVE;
import static com.example.bookreader.constants.Constants.ACTION_ID_SUBCATEGORY;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidedAction;

import com.example.bookreader.R;
import com.example.bookreader.constants.ResourcesIcons;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.dto.TagDto;
import com.example.bookreader.data.database.repository.CategoryRepository;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class BookGuidedStepFragment extends GuidedStepSupportFragment {

    @Override
    public int onProvideTheme() {
        return R.style.App_GuidedStep;
    }

    public void setConfirmActions(){
        Context context = getContext();
        var actions = getActions();
        if(actions.stream().noneMatch(act->act.getId() == ACTION_ID_SAVE)){
            List<GuidedAction> controlActions = List.of(
                    new GuidedAction.Builder(context)
                            .id(ACTION_ID_DIVIDER)
                            .title("--------------------------------------------------------------------------------------")
                            .infoOnly(true)
                            .focusable(false)
                            .build(),
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

    public void removeConfirmAction(){
        List<GuidedAction> actions = getActions();
        if(actions.removeIf(act->act.getId() == ACTION_ID_SAVE)){
            actions.removeIf(act->act.getId() ==  ACTION_ID_CANCEL);
            actions.removeIf(act->act.getId() ==  ACTION_ID_DIVIDER);
            setActions(actions);
        }
    }

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

    public void  checkChangedAndAddControls(boolean isChanged){
        if(isChanged){
            setConfirmActions();
        }
        else{
            removeConfirmAction();
        }
    }

    public void setIcon(int iconId,long actionId){
        var iconAction = findActionById(actionId);
        if(iconAction != null){
            iconAction.setIcon(ContextCompat.getDrawable(requireContext(),iconId));
            int index = getActions().indexOf(iconAction);
            notifyActionChanged(index);
        }
    }

    public void setDescription(String description,long actionId){
        var action = findActionById(actionId);
        setDescription(description,action);
    }

    public void setDescription(String description,GuidedAction action){
        if(action != null){
            action.setDescription(description);
            int index = getActions().indexOf(action);
            notifyActionChanged(index);
        }
    }

    public void updateActions(List<GuidedAction> actions,int actionId){
        GuidedAction subCategoryAction = findActionById(actionId);
        if(subCategoryAction != null){
            subCategoryAction.setSubActions(actions);
            notifyActionChanged(getActions().indexOf(subCategoryAction));
        }
    }

    public String getDescription(int actionId){
        var action = findActionById(actionId);
        if(action != null && action.getDescription() != null){
            return action.getDescription().toString();
        }
        return "";
    }

    public CompletableFuture<List<GuidedAction>> getCategoryActions(CategoryDto currentCategory,int actionId){
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

    public CompletableFuture<List<GuidedAction>> getSubCategoryActions(Long parentId,Long currentSubCategoryId){
        CategoryRepository categoryRepository = new CategoryRepository();
        if(parentId == null){
            return categoryRepository.getAllSubcategoriesAsyncCF().thenApply((subCategories)-> getSubCategoriesActions(subCategories,currentSubCategoryId));
        }
        return categoryRepository.getAllSubcategoriesByParentIdAsyncCF(parentId).thenApply((subCategories)-> getSubCategoriesActions(subCategories,currentSubCategoryId));
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

    public List<GuidedAction> getTagActions(List<TagDto> tags,List<Long> booksTagsIds){
        Context context = getContext();
        List<GuidedAction> actions = tags.stream()
                .sorted(Comparator.comparing((tag)->tag.name))
                .map(tag->
                        new GuidedAction.Builder(context)
                                .id(tag.id)
                                .title(tag.name)
                                .hasNext(false)
                                .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
                                .checked(booksTagsIds.contains(tag.id))
                                .build())
                .collect(Collectors.toList());
        actions.add(0,new GuidedAction.Builder(context)
                .id(ACTION_ID_DIVIDER)
                .title("--------------------------------------------------------------------------------------")
                .infoOnly(true)
                .focusable(false)
                .build());

        actions.add(0, new GuidedAction.Builder(context)
                .id(ACTION_ID_NEW_TAG)
                .title("Додати тег")
                .descriptionEditable(true)
                .icon(R.drawable.add)
                .build());
        if(!booksTagsIds.isEmpty()){
            addTagsClearButton(actions);
        }
        return actions;
    }

    public boolean addTagsClearButton(List<GuidedAction> actions){
        if (actions.stream().noneMatch(action -> (int)action.getId() == ACTION_ID_CLEAR_TAGS)) {
            actions.add(1, new GuidedAction.Builder(getContext())
                    .id(ACTION_ID_CLEAR_TAGS)
                    .title("Очистити")
                    .icon(R.drawable.clear)
                    .build());
            return true;
        }
        return false;
    }

    public boolean removeTagsClearButton(List<GuidedAction> actions){
        return actions.removeIf(action -> action.getId() == ACTION_ID_CLEAR_TAGS);
    }

    public void clearActionsChecked(List<GuidedAction> tagActions, Runnable post){
        for(int i = 0; i < tagActions.size(); i++){
            var action = tagActions.get(i);
            if(action.isChecked()){
                action.setChecked(false);
                notifyActionChanged(i);
            }
        }
        post.run();
    }

}
