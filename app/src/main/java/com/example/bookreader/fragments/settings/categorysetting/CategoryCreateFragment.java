package com.example.bookreader.fragments.settings.categorysetting;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.example.bookreader.R;
import com.example.bookreader.constants.ResourcesIcons;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.extentions.BookGuidedStepFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class CategoryCreateFragment extends BookGuidedStepFragment {
    private CategoryDto category;
    private final CategoryDto oldCategory;
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private static final int ACTION_ID_TITLE = 111111111;
    private static final int ACTION_ID_ICON = 111111112;
    private static final int ACTION_ID_PARENT_CATEGORY = 111111113;
    private static final int ACTION_ID_SAVE = 111111114;
    private static final int ACTION_ID_CANCEL = 111111115;
    private static final int ACTION_ID_DIVIDER = 111111116;

    public CategoryCreateFragment(@Nullable CategoryDto category){
        this.category = category == null ? new CategoryDto() : category;
        this.oldCategory = category != null ? new CategoryDto(category) : null;
        if(oldCategory == null){
            this.category.name = "Нова категорія";
            this.category.iconId = ResourcesIcons.IconsArray[0];
        }
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(
                oldCategory == null ? "Створіть категорію" : "Редагуйте категорію",
                "Категорії можуть мати рідкатегорії",
                "Змінюйте інформацію",
                ContextCompat.getDrawable(requireContext(), R.drawable.book_tag)
        );
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        Context context = getContext();
        actions.add(new GuidedAction.Builder(context)
                .id(ACTION_ID_TITLE)
                .title("Назва")
                .description(oldCategory != null ? category.name : "Нова категорія")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .descriptionEditable(true)
                .multilineDescription(true)
                .hasNext(false)
                .build());

        actions.add(new GuidedAction.Builder(context)
                .id(ACTION_ID_ICON)
                .icon(category.iconId)
                .title("Іконка")
                .build());

        actions.add(new GuidedAction.Builder(context)
                .id(ACTION_ID_PARENT_CATEGORY)
                .title("Батьківська категорія")
                .description(getCategoryDescription())
                .subActions(getCategoryActions().join())
                .build());

    }

    @Override
    public void onGuidedActionEditCanceled(@NonNull GuidedAction action) {
        if ((int) action.getId() == ACTION_ID_TITLE) {
            action.setDescription(category.name);
        }
    }

    @Override
    public long onGuidedActionEditedAndProceed(@NonNull GuidedAction action) {
        if ((int) action.getId() == ACTION_ID_TITLE && action.getDescription() != null) {
            String name = action.getDescription().toString();
            if (name.length() > 50 || name.length() < 3) {
                Toast.makeText(getContext(), "Назва категорії повинна містити від 3 до 50 символів", Toast.LENGTH_SHORT).show();
                action.setDescription(category.name);
            } else {
                 category.name = name;
            }
            checkChangedAndAddControls();
        }
        return -1;
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        switch ((int) action.getId()) {
            case ACTION_ID_SAVE:
                if(oldCategory == null){
                    categoryRepository.getByNameAsync(getActionName()).thenAccept(cat->{
                        if(cat == null){
                            categoryRepository.insertAsync(category.getCategory(),(id->{
                                if(category.parentId == null){
                                    var parentCategoryAction = findActionById(ACTION_ID_PARENT_CATEGORY);
                                    if(parentCategoryAction != null){
                                        var actions = parentCategoryAction.getSubActions();
                                        if(actions != null){
                                            category.id = id;
                                            actions.add(1,createCategoryAction(category,ACTION_ID_PARENT_CATEGORY,false));
                                            parentCategoryAction.setSubActions(actions);
                                        }
                                    }
                                }
                                setDefault();
                            }));
                        }
                        else{
                            requireActivity().runOnUiThread(()->{
                                Toast.makeText(getContext(), "Така сатегорія вже існує", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
                else{
                    categoryRepository.updateAsync(category.getCategory(),(v)->{
                        Bundle result = new Bundle();
                        result.putSerializable("new_categories", category);
                        getParentFragmentManager().setFragmentResult("new_categories_result", result);
                        getParentFragmentManager().popBackStack();
                    });
                }
                break;
            case ACTION_ID_CANCEL:
                setDefault();
                break;

        }
    }

    @Override
    public boolean onSubGuidedActionClicked(@NonNull GuidedAction action) {
        int actionId = action.getCheckSetId();
        if(actionId == ACTION_ID_PARENT_CATEGORY){
            CategoryDto selectedCategory = categoryRepository.getCategoryByIdAsyncCF(action.getId()).join();
            Long parentCategoryId = null;
            String description = "Не встановлено";
            if(selectedCategory != null){
                parentCategoryId = selectedCategory.id;
                description = selectedCategory.name;
            }
            category.parentId = parentCategoryId;
            setCategoryDescription(description);
            checkChangedAndAddControls();
        }
        return true;
    }

    private void  setDefault(){
        if(oldCategory == null){
            category.id = 0;
            category.parentId = null;
            category.name = "Нова категорія";
            category.iconId = ResourcesIcons.IconsArray[0];
        }
        else{
            category.id = oldCategory.id;
            category.parentId = oldCategory.parentId;
            category.iconId = oldCategory.iconId;
            category.name = oldCategory.name;
        }
        removeConfirmAction();
        updateParentCategoryDescription();
        updateCategoryName();
        updateCategoryActionChecked();
    }

    public void updateCategoryName(){
        var action = findActionById(ACTION_ID_TITLE);
        String title = oldCategory == null ? "Нова категорія" : oldCategory.name;
        if(action != null){
            action.setDescription(title);
            int index = getActions().indexOf(action);
            notifyActionChanged(index);
        }
    }

    public  void updateCategoryActionChecked(){
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

    private GuidedAction createCategoryAction(CategoryDto category, int actionId, boolean isChecked){
        return new GuidedAction.Builder(getContext())
                .id(category.id)
                .title(category.name)
                .icon(category.iconId)
                .hasNext(false)
                .checkSetId(actionId)
                .checked(isChecked)
                .build();
    }

    private GuidedAction createCategoryAction(int id, String title,int checkSetId, boolean isChecked){
        return new GuidedAction.Builder(getContext())
                .id(id)
                .title(title)
                .checkSetId(checkSetId)
                .checked(isChecked)
                .build();
    }

    private GuidedAction createCategoryAction(int id, String title,int iconId){
        return new GuidedAction.Builder(getContext())
                .id(id)
                .title(title)
                .icon(iconId)
                .build();
    }

    private GuidedAction createDividerAction(){
        return new GuidedAction.Builder(getContext())
                .id(ACTION_ID_DIVIDER)
                .title("____________________________________________")
                .infoOnly(true)
                .focusable(false)
                .build();
    }

    private void checkChangedAndAddControls(){
        boolean changed = (oldCategory == null
                && (category.parentId != null
                || !category.name.equals("Нова категорія")
                || category.iconId != ResourcesIcons.IconsArray[0]))
                       || (oldCategory != null
                && (category.iconId != oldCategory.iconId
                || !category.name.equals(oldCategory.name)
                || !Objects.equals(category.parentId,oldCategory.parentId)));
        if(changed){
            addConfirmActions();
        }
        else{
            removeConfirmAction();
        }
    }

    private String getCategoryDescription(){
        if(category.parentId != null){
            CategoryDto parentCategory = categoryRepository.getCategoryByIdAsyncCF(category.parentId).join();
            if(parentCategory != null){
                return parentCategory.name;
            }
        }
        return "Не встановлено";
    }

    private void setCategoryDescription(String description){
        var categoryAction = findActionById(ACTION_ID_PARENT_CATEGORY);
        if(categoryAction != null){
            categoryAction.setDescription(description);
            var index = getActions().indexOf(categoryAction);
            notifyActionChanged(index);
        }
    }

    private void updateParentCategoryDescription(){
        String description = getCategoryDescription();
        setCategoryDescription(description);
    }

    private String getActionName(){
        var action = findActionById(ACTION_ID_TITLE);
        if(action != null && action.getDescription() != null){
            return action.getDescription().toString();
        }
        return "";
    }

    private int getCategoryIconId(){
        return category != null && category.iconId != 0 ? category.iconId : ResourcesIcons.IconsArray[0];
    }

    private void removeConfirmAction(){
        List<GuidedAction>  actions = new ArrayList<>(getActions());
        if(actions.removeIf(act->act.getId() == ACTION_ID_SAVE)){
            actions.removeIf(act->act.getId() ==  ACTION_ID_CANCEL);
            actions.removeIf(act->act.getId() ==  ACTION_ID_DIVIDER);
            setActions(actions);
        }
    }

    private void addConfirmActions(){
        List<GuidedAction>  actions = new ArrayList<>(getActions());
        if(actions.stream().noneMatch(act->act.getId() == ACTION_ID_SAVE)){
            actions.add(createDividerAction());
            actions.add(createCategoryAction(ACTION_ID_SAVE,getString(R.string.save),R.drawable.save));
            actions.add(createCategoryAction(ACTION_ID_CANCEL,getString(R.string.cancel),R.drawable.redo));
            setActions(actions);
        }
    }

    private CompletableFuture<List<GuidedAction>> getCategoryActions(){
        return  categoryRepository.getAllParentCategoriesAsyncCF().thenApply(categories->{
            List<GuidedAction> actions = categories.stream()
                    .map(cat->
                            createCategoryAction(
                                    cat,
                                    ACTION_ID_PARENT_CATEGORY,
                                    oldCategory != null && cat.id == category.id))
                    .collect(Collectors.toList());
            actions.add(0,
                    createCategoryAction(-1,
                            "Не встановлено",
                            ACTION_ID_PARENT_CATEGORY,
                            oldCategory == null));
            return  actions;
        });
    }
}
