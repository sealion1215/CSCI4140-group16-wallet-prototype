package com.reddcoin_wallet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.reddcoin_wallet.R;
import com.reddcoin_wallet.application.WalletApplication;
import com.reddcoin_wallet.service.CoinService;
import com.reddcoin_wallet.service.CoinServiceImpl;
import com.reddcoin_wallet.util.Constants;

public class MainActivity extends AppCompatActivity {

    private Intent connectCoinIntent;
    private String currentAccountId = "1000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar myToolbar = findViewById(R.id.my_toolbar);
//        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        android.view.MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mybar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        android.content.Intent intent;
        switch (item.getItemId()) {
            case R.id.action_friend:
                intent = new android.content.Intent(this, FriendActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_setting:
                intent = new android.content.Intent(this, SettingActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWalletApplication().startBlockchainService(CoinService.ServiceMode.CANCEL_COINS_RECEIVED);
        connectCoinService();
        //TODO
//        checkLowStorageAlert();
    }

    protected WalletApplication getWalletApplication() {
        return (WalletApplication) getApplication();
    }

    private void connectCoinService() {
        if (connectCoinIntent == null) {
            connectCoinIntent = new Intent(CoinService.ACTION_CONNECT_COIN, null,
                    getWalletApplication(), CoinServiceImpl.class);
        }
        // Open connection if needed or possible
        connectCoinIntent.putExtra(Constants.ARG_ACCOUNT_ID, currentAccountId);
        getWalletApplication().startService(connectCoinIntent);
    }

    public void goSend(View view){
        android.content.Intent intent = new android.content.Intent(this, SendActivity.class);
        startActivity(intent);
    }

    public void goRequest(View view){
        android.content.Intent intent = new android.content.Intent(this, RequestActivity.class);
        startActivity(intent);
    }

    public void goTest(View view){
        android.content.Intent intent = new android.content.Intent(this, NetworkActivity.class);
        startActivity(intent);
    }
}
