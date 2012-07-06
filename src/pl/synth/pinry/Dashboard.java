package pl.synth.pinry;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class Dashboard extends Activity {
    private static final String TAG = "Dashboard";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("pl.synth.pinry.account");
        for (Account account : accounts) {
            manager.removeAccount(account, null, null);
        }
        Log.d(TAG, "force account creation");
        manager.addAccount("pl.synth.pinry.account", null, null, null, this, null, null);
    }
}
