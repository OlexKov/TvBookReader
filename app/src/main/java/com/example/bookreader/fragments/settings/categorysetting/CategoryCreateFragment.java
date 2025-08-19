package com.example.bookreader.fragments.settings.categorysetting;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.extentions.BookGuidedStepFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class CategoryCreateFragment extends BookGuidedStepFragment {
    private final CategoryDto category;
    private final CategoryDto oldCategory;
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private static final int ACTION_ID_TITLE = 111111111;
    private static final int ACTION_ID_ICON = 111111112;
    private static final int ACTION_ID_PARENT_CATEGORY = 111111113;
    private static final int ACTION_ID_SAVE = 111111114;
    private static final int ACTION_ID_CANCEL = 111111115;

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

    public CategoryCreateFragment(@Nullable CategoryDto category){
        this.category = category == null ? new CategoryDto() : category;
        this.oldCategory = category != null ? new CategoryDto(category) : null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        Context context = getContext();
        actions.add(new GuidedAction.Builder(context)
                .id(ACTION_ID_TITLE)
                .title("Назва")
                .description(category != null ? category.name : "")
                .descriptionEditable(true)
                .multilineDescription(true)
                .hasNext(false)
                .build());

        actions.add(new GuidedAction.Builder(context)
                .id(ACTION_ID_ICON)
                .title("Іконка")
                .build());

        actions.add(new GuidedAction.Builder(context)
                .id(ACTION_ID_PARENT_CATEGORY)
                .title("Батьківська категорія")
                .description(oldCategory != null ? oldCategory.name : "Не встановлено")
                .subActions(getCategoryActions().join())
                .build());

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

    private CompletableFuture<List<GuidedAction>> getCategoryActions(){
        Context context = getContext();
        return  categoryRepository.getAllParentCategoriesAsyncCF().thenApply(categories->{

            List<GuidedAction> actions = categories.stream()
                    .map(cat->
                            new GuidedAction.Builder(context)
                                    .id(cat.id)
                                    .title(cat.name)
                                    .icon(cat.iconId)
                                    .hasNext(false)
                                    .checkSetId(ACTION_ID_PARENT_CATEGORY)
                                    .checked(oldCategory != null && cat.id == oldCategory.id)
                                    .build())
                    .collect(Collectors.toList());
            actions.add(0,new GuidedAction.Builder(context)
                    .id(-1)
                    .title("Не встановлено")
                    .hasNext(false)
                    .checkSetId(ACTION_ID_PARENT_CATEGORY)
                    .checked(oldCategory == null)
                    .build());
            return  actions;
        });
    }


}
