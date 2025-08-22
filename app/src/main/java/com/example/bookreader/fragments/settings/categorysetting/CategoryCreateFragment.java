package com.example.bookreader.fragments.settings.categorysetting;

import static com.example.bookreader.constants.Constants.ACTION_ID_CANCEL;
import static com.example.bookreader.constants.Constants.ACTION_ID_ICON;
import static com.example.bookreader.constants.Constants.ACTION_ID_PARENT_CATEGORY;
import static com.example.bookreader.constants.Constants.ACTION_ID_SAVE;
import static com.example.bookreader.constants.Constants.ACTION_ID_TITLE;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionsStylist;

import com.example.bookreader.R;
import com.example.bookreader.constants.ResourcesIcons;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.extentions.BookGuidedStepFragment;

import java.util.List;
import java.util.Objects;


public class CategoryCreateFragment extends BookGuidedStepFragment {
    private final CategoryDto category;
    private final CategoryDto oldCategory;
    private final CategoryRepository categoryRepository = new CategoryRepository();


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

    @NonNull
    @Override
    public GuidedActionsStylist onCreateActionsStylist() {
        return new CategoryCreateFragmentActionsStylist();
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
                .subActions( getIconsActions(category))
                .build());

        actions.add(new GuidedAction.Builder(context)
                .id(ACTION_ID_PARENT_CATEGORY)
                .title("Батьківська категорія")
                .description(getCategoryDescription())
                .subActions(getCategoryActions(category,ACTION_ID_PARENT_CATEGORY).join())
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
            if (name.length() > 25 || name.length() < 3) {
                Toast.makeText(getContext(), "Назва категорії повинна містити від 3 до 25 символів", Toast.LENGTH_SHORT).show();
                action.setDescription(category.name);
            }
            else {
                 category.name = name;
            }
            checkChangedAndAddControls(isSettingChanged());
        }
        return -1;
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        switch ((int) action.getId()) {
            case ACTION_ID_SAVE:
                if(oldCategory == null){
                    categoryRepository.getByNameAsync(getDescription(ACTION_ID_PARENT_CATEGORY)).thenAccept(cat->{
                        if(cat == null){
                            categoryRepository.insertAsync(category.getCategory(),(id->{
                                if(category.parentId == null){
                                    var parentCategoryAction = findActionById(ACTION_ID_PARENT_CATEGORY);
                                    if(parentCategoryAction != null){
                                        var actions = parentCategoryAction.getSubActions();
                                        if(actions != null){
                                            category.id = id;
                                            actions.add(1,
                                                    new GuidedAction.Builder(getContext())
                                                    .id(category.id)
                                                    .title(category.name)
                                                    .icon(category.iconId)
                                                    .hasNext(false)
                                                    .checkSetId(ACTION_ID_PARENT_CATEGORY)
                                                    .checked(false)
                                                    .build());
                                            parentCategoryAction.setSubActions(actions);
                                        }
                                    }
                                }
                                requireActivity().runOnUiThread(()->{
                                    Toast.makeText(getContext(), "Категорія " + '"' + category.name + '"' + " збережена", Toast.LENGTH_SHORT).show();
                                });
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
                        requireActivity().runOnUiThread(()->{
                            Toast.makeText(getContext(), "Категорія " + '"' + category.name + '"' + " збережена", Toast.LENGTH_SHORT).show();
                        });
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
            setDescription(description,ACTION_ID_PARENT_CATEGORY);
        }
        else{
            category.iconId = Math.toIntExact(action.getId());
            setIcon(category.iconId,ACTION_ID_ICON);
        }
        checkChangedAndAddControls(isSettingChanged());
        return true;
    }

    private void setDefault(){
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
        setDescription(oldCategory == null ? "Нова категорія" : oldCategory.name,ACTION_ID_TITLE);
        updateCategoryActionChecked(category);
        setIcon(category.iconId,ACTION_ID_ICON);
    }

    private boolean isSettingChanged(){
        return (oldCategory == null
                && (category.parentId != null
                || !category.name.equals("Нова категорія")
                || category.iconId != ResourcesIcons.IconsArray[0]))
                || (oldCategory != null
                && (category.iconId != oldCategory.iconId
                || !category.name.equals(oldCategory.name)
                || !Objects.equals(category.parentId,oldCategory.parentId)));
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

    private void updateParentCategoryDescription(){
        String description = getCategoryDescription();
        setDescription(description,ACTION_ID_PARENT_CATEGORY);
    }
}

