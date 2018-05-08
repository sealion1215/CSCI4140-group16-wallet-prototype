package com.reddcoin.wallet.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.reddcoin.wallet.Configuration;
import com.reddcoin.wallet.R;
import com.reddcoin.wallet.WalletApplication;


/**
 * @author Remo Glauser
 */
public final class ServerSettingsFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Activity activity;
    private WalletApplication application;
    private Configuration config;

    SharedPreferences sharedPreferences;
    EditText addressEdit;
    EditText portEdit;

    public ServerSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("MYCheck", "oncreate");
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());


    //     if (preference instanceof ListPreference) {
    //         ListPreference listPreference = (ListPreference) preference;
    //         int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
    //         if (prefIndex >= 0) {
    //             preference.setSummary(listPreference.getEntries()[prefIndex]);       

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_server_settings, container, false);

        String serverAddress = sharedPreferences.getString(config.PREFS_KEY_SERVER_ADDRESS, "Set IP or address");
        String serverPort = sharedPreferences.getString(config.PREFS_KEY_SERVER_PORT, "Set port");

        addressEdit =  (EditText) view.findViewById(R.id.ServerAddress);
        addressEdit.setText(serverAddress);

        portEdit =  (EditText) view.findViewById(R.id.ServerPort);
        portEdit.setText(serverPort);


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("MYCheck", "Fragment onDetach");

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(config.PREFS_KEY_SERVER_ADDRESS, addressEdit.getText().toString());
        editor.putString(config.PREFS_KEY_SERVER_PORT, portEdit.getText().toString());
        editor.commit();

    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        // if (Configuration.PREFS_KEY_EXCHANGE_CURRENCY.equals(key)) {
        //     defaultCurrency = config.getExchangeCurrencyCode();

        //     updateView();
        // }
    }

    @Override
    public void onResume() {
        super.onResume();
        //unregister the preferenceChange listener
        //getPreferenceScreen().getSharedPreferences()
        //        .registerOnSharedPreferenceChangeListener(this);
    }


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

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        //getPreferenceScreen().getSharedPreferences()
        //        .unregisterOnSharedPreferenceChangeListener(this);
    }
}