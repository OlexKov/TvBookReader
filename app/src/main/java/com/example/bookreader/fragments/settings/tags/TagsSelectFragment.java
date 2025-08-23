package com.example.bookreader.fragments.settings.tags;

import static com.example.bookreader.constants.Constants.ACTION_ID_CLEAR_TAGS;
import static com.example.bookreader.constants.Constants.ACTION_ID_NEW_TAG;
import static com.example.bookreader.constants.Constants.ACTION_ID_NO_ACTION;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.example.bookreader.R;
import com.example.bookreader.data.database.entity.Tag;
import com.example.bookreader.data.database.repository.TagRepository;
import com.example.bookreader.fragments.settings.BookGuidedStepFragment;

import java.util.List;

public class TagsSelectFragment extends BookGuidedStepFragment {
    private final List<Long> tagsIds;
    private List<GuidedAction> tagsActions;
    private final TagRepository tagRepository = new TagRepository();

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
                ContextCompat.getDrawable(requireContext(), R.drawable.book_tag)
        );
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        tagRepository.getAllAsync().thenAccept(tags->{
            this.tagsActions = getTagActions(tags,tagsIds);
            actions.addAll(tagsActions);
        });
    }

    @Override
    public void onGuidedActionEditCanceled(@NonNull GuidedAction action) {
        if ((int) action.getId() == ACTION_ID_NEW_TAG) {
            action.setDescription("");
        }
    }

    @Override
    public long onGuidedActionEditedAndProceed(@NonNull GuidedAction action) {
        var description = action.getDescription();
        if(description != null){
            String descriptionString = description.toString();
            if ((int) action.getId() == ACTION_ID_NEW_TAG) {
                tagRepository.getByNameAsync(descriptionString).thenAccept(tag->{
                    requireActivity().runOnUiThread(()->setDescription("",action));
                    if(tag == null){
                        Long newTagId = tagRepository.insert(Tag.builder().name(descriptionString).build());
                        int index = tagsIds.isEmpty() ? 3 : 4;
                        tagsActions.add(index, new GuidedAction.Builder(getContext())
                                .id(newTagId)
                                .title(descriptionString)
                                .hasNext(false)
                                .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
                                .checked(false)
                                .build());
                        setActions(tagsActions);
                    }
                    else{
                        requireActivity().runOnUiThread(()->{
                            Toast.makeText(getContext(), "Тег з такою назвою вже існує", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
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
            else if(tagsIds.contains(actionId)){
                tagsIds.remove(action.getId());
            }
        }
        else if ((int) action.getId() == ACTION_ID_CLEAR_TAGS){
           clearCheckedActions(getActions());
        }
        checkAndSetClearTagsButton(getActions());
    }

    private void checkAndSetClearTagsButton(List<GuidedAction> actions) {
        boolean changed = !tagsIds.isEmpty() ? addTagsClearButton(actions) : removeTagsClearButton(actions);
        if (changed) {
            setActions(actions);
        }
    }

    private void clearCheckedActions(List<GuidedAction> tagActions){
        clearActionsChecked(tagActions,()->{
            tagsIds.clear();
            checkAndSetClearTagsButton(tagActions);
        });
    }
}
