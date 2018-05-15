package com.reddcoin.wallet.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.reddcoin.wallet.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author John L. Jegutanis
 */
public class SettingsActivity extends BaseWalletActivity {

    private static final Logger log = LoggerFactory.getLogger(SettingsActivity.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            // FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // transaction.add(R.id.container, new SettingsFragment());
            // transaction.commit();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsFragment())
                    .commit();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        // Create a file in the Internal Storage

    //     String fileName = "MyFile";
    //     String content = "hello world";

    //     if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    //         File file;

    //         FileOutputStream outputStream = null;
    //         try {
    //             file = new File(Environment.getExternalStorageDirectory(), "MyFile");

    //             outputStream = new FileOutputStream(file);
    //             outputStream.write(content.getBytes());
    //             outputStream.close();
    //         } catch (Exception e) {
    //             e.printStackTrace();
    //         }
    //         System.out.print("Write file successfull");

    //         BufferedReader input = null;
    //         file = null;
    //         try {
    //             file = new File(Environment.getExternalStorageDirectory(), "MyFile");

    //             input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    //             String line;
    //             StringBuffer buffer = new StringBuffer();
    //             while ((line = input.readLine()) != null) {
    //                 buffer.append(line);
    //             }
    //             System.out.print(file.getAbsolutePath());

    //             log.debug("ReadFile: ", buffer.toString());
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //         }
    //     } else {
    //         log.error("NO Access to Device");
    //     }
    // }

//    @Override
//    protected int getLayout() {
//        return R.layout.activity_settings;
    }
}
