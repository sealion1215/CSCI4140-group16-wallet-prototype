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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CheckBox;
import android.content.Intent;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.reddcoin.wallet.Configuration;
import com.reddcoin.wallet.R;
import com.reddcoin.wallet.WalletApplication;

import com.reddcoin.wallet.ui.FileBrowserActivity;

import static android.app.Activity.RESULT_OK;


/**
 * @author Remo Glauser
 */
public final class ServerSettingsFragment extends Fragment {//implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Activity activity;
    private WalletApplication application;
    private Configuration config;
    
    private final int REQUEST_CODE_PICK_DIR = 1;
    private final int REQUEST_CODE_PICK_FILE = 2;

    SharedPreferences sharedPreferences;
    EditText addressEdit;
    EditText portEdit;
    Button startBrowserButton;
    EditText certEdit;
    CheckBox sslBox;

    public ServerSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("MYCheck", "oncreate");
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_server_settings, container, false);

        String serverAddress = sharedPreferences.getString(config.PREFS_KEY_SERVER_ADDRESS, "Set IP or address");
        String serverPort = sharedPreferences.getString(config.PREFS_KEY_SERVER_PORT, "Set port");

        String serverCert = sharedPreferences.getString(config.PREFS_KEY_SERVER_CERT, "/Path to *.crt file");
        boolean useSSL = sharedPreferences.getBoolean(config.PREFS_KEY_USE_SSL, false);

        addressEdit =  (EditText) view.findViewById(R.id.ServerAddress);
        addressEdit.setText(serverAddress);

        portEdit =  (EditText) view.findViewById(R.id.ServerPort);
        portEdit.setText(serverPort);

        certEdit = (EditText) view.findViewById(R.id.ServerCert);
        certEdit.setText(getFileName(serverCert));

        startBrowserButton = (Button) view.findViewById(R.id.Browse);

        startBrowserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onOpenBrowserClicked();
            }
        });

        sslBox = (CheckBox) view.findViewById(R.id.SSLBox);

        sslBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                onCheckedChange(isChecked);
            }
        });

        sslBox.setChecked(useSSL);
        startBrowserButton.setEnabled(useSSL);
        certEdit.setEnabled(useSSL);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("MyDebug", "Fragment onDetach");

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(config.PREFS_KEY_SERVER_ADDRESS, addressEdit.getText().toString());
        editor.putString(config.PREFS_KEY_SERVER_PORT, portEdit.getText().toString());
        editor.commit();

    }

    public void onOpenBrowserClicked(){
        Intent fileExploreIntent = new Intent(
                FileBrowserActivity.INTENT_ACTION_SELECT_DIR,
                null,
                getActivity(),
                FileBrowserActivity.class
        );

        startActivityForResult(
                fileExploreIntent,
                REQUEST_CODE_PICK_FILE
        );
    }

    public void onCheckedChange(boolean isChecked){
        sslBox.setChecked(isChecked);
        startBrowserButton.setEnabled(isChecked);
        certEdit.setEnabled(isChecked);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(config.PREFS_KEY_USE_SSL, isChecked);
        editor.commit();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_DIR) {
            if(resultCode == RESULT_OK) {
                String newDir = data.getStringExtra(FileBrowserActivity.returnDirectoryParameter);
                Toast.makeText(
                        getActivity(),
                        "Chose *.crt file of the server, not a DIRECTORY",
                        Toast.LENGTH_LONG).show(); 
                
            } else {//if(resultCode == this.RESULT_OK) {
                Toast.makeText(
                        getActivity(),
                        "Received NO result from file browser",
                        Toast.LENGTH_LONG).show(); 
            }
        }
        
        if (requestCode == REQUEST_CODE_PICK_FILE) {
            if(resultCode == RESULT_OK) {
                String newFile = data.getStringExtra(FileBrowserActivity.returnFileParameter);
                String responseToUser = "updated";

                if(newFile.matches("\\.crt$")){
                    certEdit.setText(getFileName(newFile));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(config.PREFS_KEY_SERVER_CERT, newFile);
                    editor.commit();
                    
                }else{
                    responseToUser = "Invalid file. Must be a *.crt file";
                }
                Toast.makeText(
                        getActivity(),
                        "responseToUser",
                        Toast.LENGTH_LONG).show();
                
            } else {//if(resultCode == this.RESULT_OK) {
                Toast.makeText(
                        getActivity(),
                        "Received NO result from file browser",
                        Toast.LENGTH_LONG).show(); 
            }
        }

        
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getFileName(String filePath){
        int index = filePath.lastIndexOf("/");
        return filePath.substring(index+1,filePath.length()-1);
    }

}