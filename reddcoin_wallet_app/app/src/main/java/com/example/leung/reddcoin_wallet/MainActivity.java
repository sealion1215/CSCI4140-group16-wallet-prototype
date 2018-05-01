package com.example.leung.reddcoin_wallet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MainActivity extends AppCompatActivity {

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

    public void goSend(View view){
        android.content.Intent intent = new android.content.Intent(this, SendActivity.class);
        startActivity(intent);
    }

    public void goRequest(View view){
        android.content.Intent intent = new android.content.Intent(this, RequestActivity.class);
        startActivity(intent);
    }
}
