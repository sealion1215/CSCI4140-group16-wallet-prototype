package com.reddcoin.wallet.ui;

import android.os.Bundle;
//import android.support.v4.preference.PreferenceFragment;
import android.preference.PreferenceFragment;

import com.reddcoin.wallet.R;

/**
 * @author John L. Jegutanis
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.preferences);
    }
    //@Override
    //public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
    //    addPreferencesFromResource(R.xml.preferences);
    //}
}
