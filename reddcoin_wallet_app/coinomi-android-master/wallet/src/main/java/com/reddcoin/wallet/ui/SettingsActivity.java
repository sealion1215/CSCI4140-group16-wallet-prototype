package com.reddcoin.wallet.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.reddcoin.wallet.R;

/**
 * @author John L. Jegutanis
 */
public class SettingsActivity extends BaseWalletActivity {
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
    }

//    @Override
//    protected int getLayout() {
//        return R.layout.activity_settings;
//    }
}
