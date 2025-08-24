package com.example.bookreader.fragments.settings;
import static com.example.bookreader.constants.Constants.ACTION_ID_CANCEL;
import static com.example.bookreader.constants.Constants.ACTION_ID_DIVIDER;
import static com.example.bookreader.constants.Constants.ACTION_ID_SAVE;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidedAction;
import com.example.bookreader.R;
import java.util.List;

public abstract class BookGuidedStepFragment extends GuidedStepSupportFragment {

    @Override
    public int onProvideTheme() {
        return R.style.App_GuidedStep;
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

    public void checkChangedAndAddControls(boolean isChanged){
        if(isChanged){
            setConfirmActions();
        }
        else{
            removeConfirmAction();
        }
    }

 }
