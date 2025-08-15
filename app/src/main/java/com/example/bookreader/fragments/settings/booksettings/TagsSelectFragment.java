package com.example.bookreader.fragments.settings.booksettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.TagDto;
import com.example.bookreader.data.database.entity.Tag;
import com.example.bookreader.data.database.repository.TagRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TagsSelectFragment extends GuidedStepSupportFragment {
    private final List<Long> tagsIds;
    private List<GuidedAction> tagsActions;
    private final TagRepository tagRepository = new TagRepository();

    private static final int ACTION_ID_NEW_TAG = 11111110;
    private static final int ACTION_ID_CLEAR_TAGS = 11111111;
    private static final long ACTION_ID_NO_ACTION = -1;

    public TagsSelectFragment(List<Long> tagsIds){
        this.tagsIds = tagsIds;
    }

    @Override
    public void onStop() {
        super.onStop();
        Bundle result = new Bundle();
        result.putLongArray("tags", tagsIds.stream().mapToLong(Long::longValue).toArray());
        getParentFragmentManager().setFragmentResult("tags_result", result);
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(
                "Оберіть теги",
                "Теги допомагають при пошуку книги",
                "Змінюйте інформацію",
                ContextCompat.getDrawable(getContext(), R.drawable.book_tag)
        );
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        tagRepository.getAllAsync().thenAccept(tags->{
            this.tagsActions = getTagActions(tags);
            actions.addAll(tagsActions);
        });
    }

    @Override
    public void onGuidedActionEditCanceled(@NonNull GuidedAction action) {
        switch ((int) action.getId()) {
            case ACTION_ID_NEW_TAG:
                action.setDescription("");
                break;
        }
    }

    @Override
    public long onGuidedActionEditedAndProceed(@NonNull GuidedAction action) {
        var description = action.getDescription();
        if(description != null){
            String descriptionString = description.toString();
            switch ((int) action.getId()) {
                case ACTION_ID_NEW_TAG:
                    tagRepository.insertAsync(Tag.builder().name(descriptionString).build()).thenAccept(newTagId->{
                        int index = tagsIds.isEmpty() ? 1:2;
                        tagsActions.add(index,new GuidedAction.Builder(getContext())
                                .id(newTagId)
                                .title(descriptionString)
                                .hasNext(false)
                                .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
                                .checked(false)
                                .build());
                        setActions(tagsActions);
                        action.setDescription("");
                    });
                    break;
            }
        }
        return ACTION_ID_NO_ACTION;
    }

    @Override
    public void onGuidedActionClicked(@NonNull GuidedAction action) {
        if(action.getCheckSetId() == GuidedAction.CHECKBOX_CHECK_SET_ID){
            long actionId = action.getId();
            if(action.isChecked()){
                if(!tagsIds.contains(actionId)){
                    tagsIds.add(action.getId());
                }
            }
            else{
                if(tagsIds.contains(actionId)) {
                    tagsIds.remove(action.getId());
                }
            }

        }
        else{
            switch ((int) action.getId()) {
                case ACTION_ID_CLEAR_TAGS:
                    clearActionsChecked(getActions());

            }
        }
        checkAndSetClearTagsButton(getActions());
    }

    private List<GuidedAction> getTagActions(List<TagDto> tags){
        Context context = getContext();
        List<GuidedAction> actions = tags.stream()
                .sorted(Comparator.comparing((tag)->tag.name))
                .map(tag->
                        new GuidedAction.Builder(context)
                                .id(tag.id)
                                .title(tag.name)
                                .hasNext(false)
                                .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
                                .checked(tagsIds.contains(tag.id))
                                .build())
                .collect(Collectors.toList());
        actions.add(0, new GuidedAction.Builder(context)
                .id(ACTION_ID_NEW_TAG)
                .title("Додати тег")
                .descriptionEditable(true)
                .build());
        if(!tagsIds.isEmpty()){
            actions.add(1, new GuidedAction.Builder(getContext())
                    .id(ACTION_ID_CLEAR_TAGS)
                    .title("Очистити")
                    .build());
        }
        return actions;
    }

    private void checkAndSetClearTagsButton(List<GuidedAction> actions) {
        boolean changed = false;
        if (!tagsIds.isEmpty()) {
            if (actions.stream().noneMatch(action -> (int)action.getId() == ACTION_ID_CLEAR_TAGS)) {
                actions.add(1, new GuidedAction.Builder(getContext())
                        .id(ACTION_ID_CLEAR_TAGS)
                        .title("Очистити")
                        .build());
                changed = true;
            }
        } else if (actions.stream().anyMatch(action -> (int)action.getId() == ACTION_ID_CLEAR_TAGS)) {
            actions.removeIf(action -> action.getId() == ACTION_ID_CLEAR_TAGS);
            changed = true;
        }
        if (changed) {
            setActions(actions);
        }
    }

    private void clearActionsChecked(List<GuidedAction> tagActions){
        for(int i = 0;i < tagActions.size(); i++){
            var action = tagActions.get(i);
            if(action.isChecked()){
                action.setChecked(false);
                notifyActionChanged(i);
            }
        }
        tagsIds.clear();
        checkAndSetClearTagsButton(tagActions);
    }
}
