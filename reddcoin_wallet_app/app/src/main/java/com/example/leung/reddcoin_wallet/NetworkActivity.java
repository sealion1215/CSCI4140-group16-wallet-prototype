package com.example.leung.reddcoin_wallet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.wallet.DeterministicSeed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.net.HttpURLConnection;

import java.net.URLConnection;

public class NetworkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        Button testBtn = (Button) findViewById(R.id.btn_test);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MyTestButton", "clicked");
                //createMnemonic()
                request();
            }
        });

    }

    public void request(){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
            // Do network action in this function
                sendRequest();
            }
        });

        thread.start();
    }


    public void sendRequest(){
        try {
            CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);

            //URL url = new URL("http://www.google.com");
            URL url = new URL("http://10.0.2.2:8081");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setReadTimeout(3000);
            urlConnection.setConnectTimeout(3500);

            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            JSONObject data = new JSONObject();
            data.put("method","blockchain.block.get_header");
            data.put("id", 0);

            JSONArray array = new JSONArray();
            array.put(0);
            data.put("params", array);

            urlConnection.setDoOutput(true);
            //urlConnection.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            writeStream(out, data.toString());

            //urlConnection.connect();
            int status = urlConnection.getResponseCode();
            //System.out.println(status);

            for (String header : urlConnection.getHeaderFields().keySet()) {
                if (header != null) {
                    for (String value : urlConnection.getHeaderFields().get(header)) {
                        System.out.println(header + ":" + value);
                    }
                }
            }

            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String result = readStream(in);
                Log.d("MyConnect", result);
            }catch(Exception e2){Log.d("MyConnect", "Error: receiving: " + e2);}

            //System.out.println("disconnect");
            Log.d("MyConnect", "disconnect");
            urlConnection.disconnect();
        }
        catch(Exception e1) {Log.d("MyConnect", "Error: connecting: " + e1);}
    }

    private String readStream(InputStream is) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        for (String line = r.readLine(); line != null; line =r.readLine())
        {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

    private void writeStream(OutputStream out, String output)throws IOException
    {
        out.write(output.getBytes());
        out.flush();
    }


    protected List<String> createMnemonic(){

        //Use for testin
        List<String> wordList = Arrays.asList(
                "outside",
                "other",
                "mobile",
                "loud",
                "love",
                "knee",
                "idle",
                "giant",
                "hope",
                "flower",
                "fabric",
                "early");

        try{
            MnemonicCode mnemonic = new MnemonicCode();
            byte[] seed = mnemonic.toSeed(wordList, "");

            Log.d("MyCreateMnemonic", "Start");
            //byte[] seed = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS/8];
            // List<String> mnemonic = Arrays.asList();
            DeterministicSeed detSeed = new DeterministicSeed(wordList,seed, "", MnemonicCode.BIP39_STANDARDISATION_TIME_SECS);

            byte[] seed2 = detSeed.getSeedBytes();
            DeterministicKey dkKey = HDKeyDerivation.createMasterPrivateKey(seed2);
            Log.d("MyKey", dkKey.toString());

            Log.d("Mytest", seed.toString());
            Log.d("Mytest", seed2.toString());
            List<String> checkList = detSeed.getMnemonicCode();

            //List<String> checkList = mnemonic.getWordList();
            for(int i = 0; i < checkList.size();i++){
                Log.d("MyCreateMnemonic", checkList.get(i) + " n="+i+1);
            }
            Log.d("MyCreateMnemonic", "End");
            return checkList;
        }
        catch(Exception e){
            Log.d("MyCreateMnemonic", "IOException " + e);
        }
        return Collections.emptyList();
    }

}