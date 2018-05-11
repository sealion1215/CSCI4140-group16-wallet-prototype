package com.reddcoin.wallet.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
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

    private static CoinType type;
    private ImageButton scanQrCodeButton;
    private ArrayList<Friend> friendList;
    FriendManageAdapter adapter;
    

    public static class Friend
    {
        public final String name, address;

        public Friend(String iName, String iAddress)
        {
            name = iName;
            address = iAddress;
        }

    }

    public class FriendManageAdapter extends ArrayAdapter<Friend>{
        public FriendManageAdapter(Context context, ArrayList<Friend> list){
            super(context, 0, list);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            // Get the data item for this position
            final Friend friend = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_friends_item, parent, false);
            }
            // Lookup view for data population
            TextView friendName = (TextView) convertView.findViewById(R.id.friendName);
//            final Button removeButton = (Button) convertView.findViewById(R.id.removeButton);

            // Populate the data into the template view using the data object
            friendName.setText(friend.name);
            friendName.setOnClickListener(new View.OnClickListener(){
                private Friend fd = friend;
                private int index = position;
                public void onClick(View v){
                    final Dialog myDlg = new Dialog(FriendsActivity.this);
                    myDlg.setContentView(R.layout.activity_friends_popup);
                    myDlg.show();

                    Button saveBtn = (Button) myDlg.findViewById(R.id.saveButton);
                    Button removeBtn = (Button) myDlg.findViewById(R.id.removeButton);
                    EditText nameText = myDlg.findViewById(R.id.inputName);
                    EditText addressText = myDlg.findViewById(R.id.inputAddress);
                    nameText.setText(fd.name);
                    addressText.setText(fd.address);

                    saveBtn.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                            addFriend(friendList, nameText.getText().toString(), addressText.getText().toString(),
                                    () -> {
                                        FriendManageAdapter.this.notifyDataSetChanged();
                                        Toast.makeText(FriendsActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                                        myDlg.cancel();
                                    },
                                    () -> {
                                        friendList.remove(index);
                                    },
                                    () -> {
                                        Toast.makeText(FriendsActivity.this, "Invalid Address.", Toast.LENGTH_SHORT).show();
                                    },
                                    FriendsActivity.this);
                            Toast.makeText(FriendsActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                            myDlg.cancel();
                        }
                    });

                    removeBtn.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                            removeFriend(friendList, index, FriendsActivity.this);
                            FriendManageAdapter.this.notifyDataSetChanged();
                            Toast.makeText(FriendsActivity.this, "Removed", Toast.LENGTH_SHORT).show();
                            myDlg.cancel();
                        }
                    });
                }
            });
//            removeButton.setOnClickListener(new View.OnClickListener() {
//                private int index = position;
//                public void onClick(View v) {
//                    removeFriend(friendList, index, FriendsActivity.this);
//                    FriendManageAdapter.this.notifyDataSetChanged();
//                }
//            });

            // Return the completed view to render on screen
            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friendList = loadFriendList(this);
        adapter = new FriendManageAdapter(this, friendList);
        ListView listView = (ListView) findViewById(R.id.friendList);
        listView.setAdapter(adapter);
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
                break;
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

    private static boolean removeFriend(ArrayList<Friend> list, int index, Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.FRIEND_PREFERENCE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        list.remove(index);
        return saveFriendList(list, editor);
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

    private interface Callback{
        void exec();
    }

    //for public usage
    public static void addFriend(ArrayList<Friend> friendList, String friendName, String friendAddress, Callback success, Callback invalid, BaseWalletActivity activity){
        addFriend(friendList, friendName, friendAddress, success,  null, invalid, activity);
    }

    public static void addFriend(ArrayList<Friend> friendList, String friendName, String friendAddress, Callback success, Callback beforePush, Callback invalid, BaseWalletActivity activity){
        if (! (friendName.isEmpty() || friendAddress.isEmpty()) ){
            if(validate(friendAddress, activity)){
                if(beforePush != null)
                    beforePush.exec();
                if (pushFriendList(friendList, friendName, friendAddress, activity)) {
                    success.exec();
                }
            }else{
                invalid.exec();
            }
        }
    }

    public void addFriendClick(View view){
        EditText nameText = findViewById(R.id.inputName);
        EditText addressText = findViewById(R.id.inputAddress);
        String friendName = nameText.getText().toString();
        String friendAddress = addressText.getText().toString();

        addFriend(friendList, friendName, friendAddress,
            () -> {
                //hide keyboard
                View temp = this.getCurrentFocus();
                if (temp != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                //clear input box
                nameText.getText().clear();
                addressText.getText().clear();

                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Added!", Toast.LENGTH_SHORT).show();
            },
            () -> {
                Toast.makeText(this, "Invalid Address.", Toast.LENGTH_SHORT).show();
            },
            this
        );
    }

    private static boolean validate(String addressStr, BaseWalletActivity activity){
        addressStr = GenericUtils.fixAddress(addressStr);
        if(type == null){
            Intent intent = activity.getIntent();
            String accountId = intent.getStringExtra(WalletActivity.EXTRA_ACCOUNT);
            WalletPocketHD pocket = (WalletPocketHD) activity.getWalletApplication().getAccount(accountId);
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
