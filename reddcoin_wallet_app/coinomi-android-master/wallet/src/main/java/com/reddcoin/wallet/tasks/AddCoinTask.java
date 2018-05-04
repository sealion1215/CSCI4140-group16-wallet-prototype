package com.reddcoin.wallet.tasks;

import android.os.AsyncTask;

import com.reddcoin.core.coins.CoinType;
import com.reddcoin.core.wallet.Wallet;
import com.reddcoin.core.wallet.WalletAccount;

import org.spongycastle.crypto.params.KeyParameter;

import javax.annotation.Nullable;

/**
 * @author John L. Jegutanis
 */
public abstract class AddCoinTask  extends AsyncTask<Void, Void, Void> {
    protected final CoinType type;
    private final Wallet wallet;
    @Nullable private final String password;
    private WalletAccount newAccount;
    private Exception exception;

    public AddCoinTask(CoinType type, Wallet wallet, @Nullable String password) {
        this.type = type;
        this.wallet = wallet;
        this.password = password;
    }

    @Override abstract protected void onPreExecute();

    @Override
    protected Void doInBackground(Void... params) {
        KeyParameter key = null;
        exception = null;
        try {
            if (wallet.isEncrypted() && wallet.getKeyCrypter() != null) {
                key = wallet.getKeyCrypter().deriveKey(password);
            }
            newAccount = wallet.createAccount(type, true, key);
            wallet.saveNow();
        } catch (Exception e) {
            exception = e;
        }

        return null;
    }

    @Override
    final protected void onPostExecute(Void aVoid) {
        onPostExecute(exception, newAccount);
    }

    abstract protected void onPostExecute(Exception exception, WalletAccount newAccount);
}