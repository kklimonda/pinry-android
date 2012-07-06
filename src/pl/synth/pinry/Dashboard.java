package pl.synth.pinry;

import android.accounts.*;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class Dashboard extends Activity {
    private static final String TAG = "Dashboard";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("pl.synth.pinry.account");

        if (accounts.length == 0) {
            manager.addAccount("pl.synth.pinry.account", null, null, null, this, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Bundle result = future.getResult();
                        Intent i = (Intent) result.get(AccountManager.KEY_INTENT);
                        startActivity(Dashboard.this.getIntent());
                    } catch (OperationCanceledException e) {
                        Log.d(TAG, "addAccount raised OperationCancelledException: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "addAccount raised IOException: " + e.getMessage());
                    } catch (AuthenticatorException e) {
                        Log.d(TAG, "addAccount raised AuthenticatorException: " + e.getMessage());
                    }
                }
            }, null);
            finish();
        }
    }
}
