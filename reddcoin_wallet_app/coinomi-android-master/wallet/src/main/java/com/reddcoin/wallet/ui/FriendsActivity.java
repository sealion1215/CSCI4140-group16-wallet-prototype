package com.reddcoin.wallet.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

    public class Friend
    {
        public final String name, address;

        public Friend(String iName, String iAddress)
        {
            name   = iName;
            address = iAddress;
        }
    }

    private boolean saveArray(String[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(getString(R.string.friend_list_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", array.length);
        for(int i=0;i<array.length;i++)
            editor.putString(arrayName + "_" + i, array[i]);
        return editor.commit();
    }

    private String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(getString(R.string.friend_list_key), Context.MODE_PRIVATE);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }

    public void addFriend(View view){
        EditText nameText = findViewById(R.id.inputName);
        EditText addressText = findViewById(R.id.inputAddress);
        String friendName = nameText.getText().toString();
        String friendAddress = addressText.getText().toString();
        if (! (friendName.isEmpty() || friendAddress.isEmpty()) ){
            if(validate(friendAddress)){
                //hide keyboard
                View temp = this.getCurrentFocus();
                if (temp != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                //clear input box
                nameText.getText().clear();
                addressText.getText().clear();



                Toast.makeText(this, "Added!", Toast.LENGTH_SHORT).show();
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
