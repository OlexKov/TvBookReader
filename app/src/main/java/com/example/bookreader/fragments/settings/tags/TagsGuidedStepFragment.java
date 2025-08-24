package com.example.bookreader.fragments.settings.tags;

import static com.example.bookreader.constants.Constants.ACTION_ID_CLEAR_TAGS;
import static com.example.bookreader.constants.Constants.ACTION_ID_DIVIDER;
import static com.example.bookreader.constants.Constants.ACTION_ID_NEW_TAG;

import android.content.Context;
import androidx.leanback.widget.GuidedAction;
import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.TagDto;
import com.example.bookreader.fragments.settings.BookGuidedStepFragment;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TagsGuidedStepFragment extends BookGuidedStepFragment {

    public List<GuidedAction> getTagActions(List<TagDto> tags, List<Long> booksTagsIds){
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
