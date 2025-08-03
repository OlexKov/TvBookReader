package com.example.bookreader.fragments.settings.appsettings;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.leanback.preference.LeanbackSettingsFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class SettingRootFragment extends LeanbackSettingsFragmentCompat {

    @Override
    public void onPreferenceStartInitialScreen() {
         startPreferenceFragment(new MainSettingFragment());
    }

    @Override
    public boolean onPreferenceStartScreen(@NonNull PreferenceFragmentCompat caller, @NonNull PreferenceScreen pref) {
        final Fragment fragment = new  MainSettingFragment();
        final Bundle args = new Bundle(1);
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.getKey());
        fragment.setArguments(args);
        startImmersiveFragment(fragment);
        return true;
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
        final Bundle args = pref.getExtras();
        final Fragment f = getChildFragmentManager().getFragmentFactory().instantiate(
                requireActivity().getClassLoader(), pref.getFragment());
        f.setArguments(args);
        f.setTargetFragment(caller, 0);
        if (!(f instanceof DescriptionSettingsFragment) && !(f instanceof PreviewSettingsFragment)) {
            startPreferenceFragment(f);
        } else {
            startImmersiveFragment(f);
        }
        return true;
    }


}
