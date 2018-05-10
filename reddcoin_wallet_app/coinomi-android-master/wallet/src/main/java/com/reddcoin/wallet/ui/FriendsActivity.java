package com.reddcoin.wallet.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.reddcoin.wallet.Constants;
import com.reddcoin.wallet.R;

import com.reddcoin.core.coins.CoinType;
import com.reddcoin.core.wallet.WalletPocketHD;
import com.reddcoin.core.util.GenericUtils;
import com.reddcoin.core.uri.CoinURI;
import com.reddcoin.core.uri.CoinURIParseException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;

import java.util.ArrayList;

import static com.reddcoin.core.Preconditions.checkNotNull;

public class FriendsActivity extends BaseWalletActivity{

    private static final int REQUEST_CODE_SCAN = 0;

    private CoinType type;
    private ImageButton scanQrCodeButton;
    private ArrayList<Friend> friendList;

    

    public static class Friend
    {
        public final String name, address;

        public Friend(String iName, String iAddress)
        {
            name = iName;
            address = iAddress;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        friendList = loadFriendList(this);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(false);

        scanQrCodeButton = (ImageButton) findViewById(R.id.scan_qr_code);
        scanQrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleScan();
            }
        });
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode == REQUEST_CODE_SCAN) {
            if (resultCode == Activity.RESULT_OK) {
                final String input = intent.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);

                try {
                    final CoinURI coinUri = new CoinURI(type, input);

                    Address address = coinUri.getAddress();
                    updateView(address.toString());
                    // Coin amount = coinUri.getAmount();
                    // String label = coinUri.getLabel();

                    // updateStateFrom(address, amount, label);
                    //Toast.makeText(this, address.toString(), Toast.LENGTH_LONG).show();

                } catch (final CoinURIParseException x) {
                    String error = getResources().getString(R.string.uri_error, x.getMessage());
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
            }
        } else {/* Do nothing */}
    }

    private void handleScan() {
        startActivityForResult(new Intent(this, ScanActivity.class), REQUEST_CODE_SCAN);
    }

    private void updateView(String address) {
        EditText textField = (EditText) findViewById(R.id.inputAddress);
        textField.setText(address);
    }

    private static String getPrefArrayString(String arrayName, int index, SharedPreferences prefs){
        return prefs.getString(arrayName + "_" + index, null);
    }

    private static int getNumOfFriends(SharedPreferences prefs){
        return prefs.getInt(Constants.FRIEND_SIZE, 0);
    }

    private static void savePrefElement(String arrayName, int index, String element, SharedPreferences.Editor editor){
        editor.putString(arrayName + "_" + index, element);
    }

    private static boolean pushFriendList(ArrayList<Friend> list, String name, String address, Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.FRIEND_PREFERENCE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int l = list.size();
        boolean inserted = false;
        Friend insertElement = new Friend(name, address);
        for(int i = 0; i < l; i++){
            Friend temp = list.get(i);
            if (name.compareToIgnoreCase(temp.name) <= 0){
                inserted = true;
                list.add(i, insertElement);
            }
        }
        if (! inserted){
            list.add(l, insertElement);
        }

        return saveFriendList(list, editor);
    }

    private static boolean saveFriendList(ArrayList<Friend> list, SharedPreferences.Editor editor){
        int l = list.size();
        editor.putInt(Constants.FRIEND_SIZE, l);
        for (int i = 0; i < l; i++){
            Friend temp = list.get(i);
            savePrefElement(Constants.FRIEND_NAME_STORAGE, i, temp.name, editor);
            savePrefElement(Constants.FRIEND_ADDRESS_STORAGE, i, temp.address, editor);
        }
        return editor.commit();
    }

    public static ArrayList<Friend> loadFriendList(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.FRIEND_PREFERENCE_KEY, Context.MODE_PRIVATE);
        ArrayList<Friend> arrL = new ArrayList<Friend>();

        int size = getNumOfFriends(prefs);
        for(int i = 0;i < size;i++){
            Friend obj = new Friend(getPrefArrayString(Constants.FRIEND_NAME_STORAGE, i, prefs), getPrefArrayString(Constants.FRIEND_ADDRESS_STORAGE, i, prefs));
            arrL.add(obj);
        }
        return arrL;
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

                if (pushFriendList(friendList, friendName, friendAddress, this)) {
                    Toast.makeText(this, "Added!", Toast.LENGTH_SHORT).show();
                }
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
