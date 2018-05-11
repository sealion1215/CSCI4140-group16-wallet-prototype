package com.reddcoin.wallet.ui;

// import android.os.Bundle;
// //import android.support.v4.preference.PreferenceFragment;
// import android.preference.PreferenceFragment;
// import android.support.v7.preference.PreferenceFragmentCompat;
// import android.support.v4.app.Fragment;

// import com.reddcoin.wallet.R;

// /**
//  * @author John L. Jegutanis
//  */
// public class SettingsFragment extends PreferenceFragmentCompat {
//     //@Override
//     //public void onCreate(Bundle paramBundle) {
//     //    super.onCreate(paramBundle);
//     //    addPreferencesFromResource(R.xml.preferences);
//     //}
//     @Override
//     public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//         // Load the preferences from an XML resource
//         addPreferencesFromResource(R.xml.preferences);
//     }
// }

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import com.reddcoin.wallet.R;

/**
 * shows the settings option for choosing the movie categories in ListPreference.
 */
public class SettingsFragment extends PreferenceFragmentCompat {//implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    // SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        //add xml
        addPreferencesFromResource(R.xml.preferences);

        // sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // onSharedPreferenceChanged(sharedPreferences, getString(R.string.movies_categories_key));
        // onSharedPreferenceChanged(sharedPreferences, getString(R.string.server_url_key));
    }


    // @Override
    // public void onResume() {
    //     super.onResume();
    //     //unregister the preferenceChange listener
    //     getPreferenceScreen().getSharedPreferences()
    //             .registerOnSharedPreferenceChangeListener(this);
    // }

    // @Override
    // public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    //     Preference preference = findPreference(key);
    //     if (preference instanceof ListPreference) {
    //         ListPreference listPreference = (ListPreference) preference;
    //         int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
    //         if (prefIndex >= 0) {
    //             preference.setSummary(listPreference.getEntries()[prefIndex]);
    //         }
    //     } else if (preference instanceof EditTextPreference){
    //         EditTextPreference editText = (EditTextPreference) preference;
    //         preference.setSummary(editText.getText());           
    //     }else {
    //         preference.setSummary(sharedPreferences.getString(key, ""));

    //     }
    // }

    // @Override
    // public void onPause() {
    //     super.onPause();
    //     //unregister the preference change listener
    //     getPreferenceScreen().getSharedPreferences()
    //             .unregisterOnSharedPreferenceChangeListener(this);
    //}
}