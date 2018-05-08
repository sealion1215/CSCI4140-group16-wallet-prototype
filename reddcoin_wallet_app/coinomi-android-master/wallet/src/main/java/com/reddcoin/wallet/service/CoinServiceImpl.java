package com.reddcoin.wallet.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.google.common.collect.ImmutableList;
import com.reddcoin.core.coins.ReddcoinMain;
import com.reddcoin.core.network.CoinAddress;
import com.reddcoin.core.network.ConnectivityHelper;
import com.reddcoin.core.network.ServerClients;
import com.reddcoin.core.wallet.Wallet;
import com.reddcoin.core.wallet.WalletAccount;
import com.reddcoin.stratumj.ServerAddress;
import com.reddcoin.wallet.Configuration;
import com.reddcoin.wallet.Constants;
import com.reddcoin.wallet.WalletApplication;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.CheckForNull;


/**
 * @author John L. Jegutanis
 * @author Andreas Schildbach
 */
public class CoinServiceImpl extends Service implements CoinService {
    private WalletApplication application;
    private Configuration config;
    private ConnectivityManager connManager;
    private ConnectivityHelper connHelper;

    @CheckForNull
    private ServerClients clients;

    private String lastAccount;

//    private PowerManager.WakeLock wakeLock;

    private NotificationManager nm;
    private static final int NOTIFICATION_ID_CONNECTED = 0;
    private static final int NOTIFICATION_ID_COINS_RECEIVED = 1;

    private int notificationCount = 0;
    private BigInteger notificationAccumulatedAmount = BigInteger.ZERO;
    private final List<Address> notificationAddresses = new LinkedList<Address>();
    private AtomicInteger transactionsReceived = new AtomicInteger();
    private long serviceCreatedAt;

    private static final int MIN_COLLECT_HISTORY = 2;
    private static final int IDLE_BLOCK_TIMEOUT_MIN = 2;
    private static final int IDLE_TRANSACTION_TIMEOUT_MIN = 9;
    private static final int MAX_HISTORY_SIZE = Math.max(IDLE_TRANSACTION_TIMEOUT_MIN, IDLE_BLOCK_TIMEOUT_MIN);
    private static final long APPWIDGET_THROTTLE_MS = DateUtils.SECOND_IN_MILLIS;

    private static final Logger log = LoggerFactory.getLogger(CoinService.class);

    private SharedPreferences sharedPref;


    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        private boolean hasConnectivity;
        private boolean hasStorage = true;
        private int currentNetworkType = -1;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();

            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                hasConnectivity = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

                NetworkInfo activeInfo = connManager.getActiveNetworkInfo();
                boolean isNetworkChanged;
                if (activeInfo != null && activeInfo.isConnected()) {
                    isNetworkChanged = currentNetworkType != activeInfo.getType();
                    currentNetworkType = activeInfo.getType();
                } else {
                    isNetworkChanged = false;
                    currentNetworkType = -1;
                }
                log.info("network is " + (hasConnectivity ? "up" : "down"));
                log.info("network type " + (isNetworkChanged ? "changed" : "didn't change"));

                check(isNetworkChanged);
            } else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action)) {
                hasStorage = false;
                log.info("device storage low");

                check(false);
            } else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action)) {
                hasStorage = true;
                log.info("device storage ok");

                check(false);
            }
        }

//        @SuppressLint("Wakelock")
        private void check(boolean isNetworkChanged) {
            Wallet wallet = application.getWallet();
            final boolean hasEverything = hasConnectivity && hasStorage && (wallet != null);

            if (hasEverything && clients == null) {
//                log.debug("acquiring wakelock");
//                wakeLock.acquire();

                log.info("Creating coins clients");
                clients = getServerClients(wallet);
                if (lastAccount != null) clients.startAsync(wallet.getAccount(lastAccount));
            } else if (hasEverything && isNetworkChanged) {
                log.info("Restarting coins clients as network changed");
                clients.resetConnections();
            } else if (!hasEverything && clients != null) {
                log.info("stopping stratum clients");
                clients.stopAllAsync();
                clients = null;

//                log.debug("releasing wakelock");
//                wakeLock.release();
            }
        }
    };


    private ServerClients getServerClients(Wallet wallet) {

        String address = sharedPref.getString(config.PREFS_KEY_SERVER_ADDRESS, "");

        Integer port;
        try {
            port = Integer.valueOf(sharedPref.getString(config.PREFS_KEY_SERVER_PORT, ""));
        }catch (Exception ex){
            port = 0;
        }

        List<CoinAddress> servers = ImmutableList.of(
            new CoinAddress(ReddcoinMain.get(),
                    new ServerAddress(address, port))
            );
        
        return new ServerClients(servers, wallet, connHelper);
    }

    private final BroadcastReceiver tickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            log.debug("Received a tick {}", intent);

            if (clients != null) {
                clients.ping();
            }

            long lastStop = application.getLastStop();
            if (lastStop > 0) {
                long secondsIdle = (SystemClock.elapsedRealtime() - lastStop) / 1000;

                if (secondsIdle > Constants.STOP_SERVICE_AFTER_IDLE_SECS) {
                    log.info("Idling detected, stopping service");
                    stopSelf();
                }
            }
        }
    };

    public class LocalBinder extends Binder {
        public CoinService getService() {
            return CoinServiceImpl.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(final Intent intent) {
        log.debug(".onBind()");

        return mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        log.debug(".onUnbind()");

        return super.onUnbind(intent);
    }

    @Override
    public void onCreate()
    {
        serviceCreatedAt = System.currentTimeMillis();
        log.debug(".onCreate()");

        super.onCreate();

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final String lockName = getPackageName() + " blockchain sync";

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, lockName);

        application = (WalletApplication) getApplication();
        config = application.getConfiguration();
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connHelper = getConnectivityHelper(connManager);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
        registerReceiver(connectivityReceiver, intentFilter);
        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        sharedPref = PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext());

        SharedPreferences.OnSharedPreferenceChangeListener sharedListener = new
                           SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                  String key) {
                Wallet wallet = application.getWallet();

                if (wallet != null)
                    clients = getServerClients(wallet);
            }
        };

        sharedListener.onSharedPreferenceChanged(sharedPref,config.PREFS_KEY_SERVER_ADDRESS);
        sharedListener.onSharedPreferenceChanged(sharedPref,config.PREFS_KEY_SERVER_PORT);
    }

    private ConnectivityHelper getConnectivityHelper(final ConnectivityManager manager) {
        return new ConnectivityHelper() {
            @Override
            public boolean isConnected() {
                NetworkInfo activeInfo = manager.getActiveNetworkInfo();
                return activeInfo != null && activeInfo.isConnected();
            }
        };
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId)
    {
        log.info("service start command: " + intent
                + (intent.hasExtra(Intent.EXTRA_ALARM_COUNT) ? " (alarm count: " + intent.getIntExtra(Intent.EXTRA_ALARM_COUNT, 0) + ")" : ""));

        final String action = intent.getAction();

        if (CoinService.ACTION_CANCEL_COINS_RECEIVED.equals(action)) {
            notificationCount = 0;
            notificationAccumulatedAmount = BigInteger.ZERO;
            notificationAddresses.clear();

            nm.cancel(NOTIFICATION_ID_COINS_RECEIVED);

        } else if (CoinService.ACTION_RESET_WALLET.equals(action)) {
            if (application.getWallet() != null) {
                Wallet wallet = application.getWallet();
                if (intent.hasExtra(Constants.ARG_ACCOUNT_ID)) {
                    String accountId = intent.getStringExtra(Constants.ARG_ACCOUNT_ID);
                    WalletAccount pocket = wallet.getAccount(accountId);
                    if (pocket != null) {
                        WalletAccount account = wallet.refresh(accountId);

                        if (clients != null && account != null) {
                            clients.resetAccount(account);
                        }
                    } else {
                        log.warn("Tried to start a service for account id {} but no pocket found.",
                                accountId);
                    }

                } else {
                    log.warn("Missing account id argument, not doing anything");
                }
            } else {
                log.warn("Got wallet reset intent, but no wallet is available");
            }
        } else if (CoinService.ACTION_CONNECT_COIN.equals(action)) {
            if (application.getWallet() != null) {
                Wallet wallet = application.getWallet();
                if (intent.hasExtra(Constants.ARG_ACCOUNT_ID)) {
                    lastAccount = intent.getStringExtra(Constants.ARG_ACCOUNT_ID);
                    WalletAccount pocket = wallet.getAccount(lastAccount);
                    if (pocket != null) {
                        if (clients == null && connHelper.isConnected()) {
                            clients = getServerClients(wallet);
                        }

                        if (clients != null) {
                            clients.startAsync(pocket);
                        }
                    } else {
                        log.warn("Tried to start a service for account id {} but no pocket found.",
                                lastAccount);
                    }
                } else {
                    log.warn("Missing account id argument, not doing anything");
                }
            } else {
                log.error("Got connect coin intent, but no wallet is available");
            }
        } else if (CoinService.ACTION_BROADCAST_TRANSACTION.equals(action)) {
            final Sha256Hash hash = new Sha256Hash(intent.getByteArrayExtra(CoinService.ACTION_BROADCAST_TRANSACTION_HASH));
            final Transaction tx = null; // FIXME

            if (clients != null)
            {
                log.info("broadcasting transaction " + tx.getHashAsString());
                broadcastTransaction(tx);
            }
            else
            {
                log.info("client not available, not broadcasting transaction " + tx.getHashAsString());
            }
        }

        return START_REDELIVER_INTENT;
    }

    private void broadcastTransaction(Transaction tx) {
        // TODO send broadcast message
    }

    @Override
    public void onDestroy()
    {
        log.debug(".onDestroy()");

        unregisterReceiver(tickReceiver);
        unregisterReceiver(connectivityReceiver);

        if (clients != null) {
            clients.stopAllAsync();
            clients = null;
        }

        application.saveWalletNow();

//        if (wakeLock.isHeld())
//        {
//            log.debug("wakelock still held, releasing");
//            wakeLock.release();
//        }

        super.onDestroy();

        log.info("service was up for " + ((System.currentTimeMillis() - serviceCreatedAt) / 1000 / 60) + " minutes");
    }

    @Override
    public void onLowMemory() {
        log.warn("low memory detected, stopping service");
        stopSelf();
    }
}
