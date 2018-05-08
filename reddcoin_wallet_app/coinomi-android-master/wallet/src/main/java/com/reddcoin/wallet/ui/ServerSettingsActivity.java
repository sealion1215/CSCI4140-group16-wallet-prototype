package com.reddcoin.wallet.ui;

import android.os.Bundle;
import com.reddcoin.wallet.R;

/**
 * @author Remo Glauser
 */
public class ServerSettingsActivity extends BaseWalletActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);

        if (savedInstanceState == null) {
            ServerSettingsFragment fragment = new ServerSettingsFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }
}