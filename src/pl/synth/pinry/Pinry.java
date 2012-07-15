package pl.synth.pinry;

import android.accounts.*;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.IOException;

public class Pinry extends Activity {
    private static final String TAG = "Pinry";

    public static final String AUTHORITY = "pl.synth.pinry.pins";

    public static final class Pins implements BaseColumns {
        private Pins() {}

        public static final String TABLE_NAME = "pins";

        private static final String SCHEME = "content://";
        private static final String PATH_PINS = "/pins";
        private static final String PATH_PIN_ID = "/pins/";

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PINS);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_PIN_ID + "#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.pinry.pin";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.pinry.pin";

        public static final String DEFAULT_SORT_ORDER = "published DESC";

        public static final int PIN_ID_PATH_POSITION = 1;

        public static final String COLUMN_NAME_IMAGE_PATH = "image_path";
        public static final String COLUMN_NAME_IMAGE_URL = "image_url";
        public static final String COLUMN_NAME_THUMBNAIL_PATH = "thumbnail_path";
        public static final String COLUMN_NAME_PUBLISHED = "published";
        public static final String COLUMN_NAME_SOURCE_URL = "url";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_SYNC_STATE = "sync_state";

        public static final class SyncState {
            public static final String WAITING = "waiting";
            public static final String TRANSIT = "transit";
            public static final String SYNCED = "synced";
        }

    }

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
                        Intent i = new Intent(Pinry.this, Pinry.class);
                        startActivity(i);
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
            return;
        } else if (accounts.length > 1) {
            Log.d(TAG, "More than one Pinry account and we don't support that yet. Choosing the first one");
        }

        Account account = accounts[0];

        ContentResolver.setSyncAutomatically(account, "pl.synth.pinry.pins", true);

        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
        finish();
    }
}
