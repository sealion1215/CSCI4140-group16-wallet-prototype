package com.reddcoin.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.reddcoin.core.coins.CoinType;
import com.reddcoin.core.wallet.WalletPocketHD;
import com.reddcoin.wallet.Constants;
import com.reddcoin.wallet.R;
import com.reddcoin.core.util.GenericUtils;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;

import static com.reddcoin.core.Preconditions.checkNotNull;

public class FriendsActivity extends BaseWalletActivity{
    private CoinType type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    public void addFriend(View view){
        EditText nameText = findViewById(R.id.inputName);
        EditText addressText = findViewById(R.id.inputAddress);
        String friendName = nameText.getText().toString();
        String friendAddress = addressText.getText().toString();
        if (! (friendName.isEmpty() || friendAddress.isEmpty()) ){
            if(validate(friendAddress)){

            }else{
                Toast.makeText(this, "Invalid Address.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validate(String addressStr){
        addressStr = GenericUtils.fixAddress(addressStr);
        if(type == null){
            Intent intent = getIntent();
            String accountId = intent.getStringExtra(WalletActivity.EXTRA_ACCOUNT);
            WalletPocketHD pocket = (WalletPocketHD) getWalletApplication().getAccount(accountId);
            type = pocket.getCoinType();
        }

        try {
            Address address = new Address(type, addressStr);
            return true;
        } catch (final AddressFormatException x) {
            return false;
        }
    }
}
