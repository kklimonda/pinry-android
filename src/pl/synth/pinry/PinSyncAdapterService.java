package pl.synth.pinry;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import java.util.ArrayList;

public class PinSyncAdapterService extends Service {
    private static SyncAdapterImpl syncAdapter = null;

    @Override
    public IBinder onBind(Intent intent) {
        return getSyncAdapter().getSyncAdapterBinder();
    }

    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
        private Context context;

        public SyncAdapterImpl(Context context, boolean autoInitialize) {
            super(context, autoInitialize);
            this.context = context;
        }

        private String getLastKnownUpdate() {
            ContentResolver contentResolver = context.getContentResolver();
            String sortOrder = "published DESC LIMIT 1";
            Cursor c = contentResolver.query(Pinry.Pins.CONTENT_URI, new String[] {Pinry.Pins.COLUMN_NAME_PUBLISHED}, null, null, sortOrder);
            if (c.getCount() == 0 ) {
                /* We have no images stored locally, so don't limit the API call to the images created after some date. */
                return null;
            }

            c.moveToFirst();
            return c.getString(c.getColumnIndex("published"));
        }

        private void pullUpdates(Account account) {
            ContentResolver contentResolver = context.getContentResolver();
            AccountManager manager = AccountManager.get(context);
            String url = manager.getUserData(account, "url");
            NetworkClient client = new NetworkClient(url, context);

            ArrayList<Pin> newPins = client.getPinsSince(getLastKnownUpdate());

            ContentValues values = new ContentValues();

            for (Pin pin : newPins) {
                values.clear();

                Uri uri = ContentUris.withAppendedId(Pinry.Pins.CONTENT_ID_URI_BASE, pin.getId());
                Cursor c = contentResolver.query(uri, new String[]{Pinry.Pins._ID, Pinry.Pins.COLUMN_NAME_IMAGE_PATH}, null, null, null);
                if (c.getCount() > 0) {
                    c.close();
                    continue;
                }
                c.close();

                values.put(Pinry.Pins._ID, pin.getId());
                values.put(Pinry.Pins.COLUMN_NAME_IMAGE_PATH, pin.getLocalPath());
                values.put(Pinry.Pins.COLUMN_NAME_THUMBNAIL_PATH, pin.getThumbnailPath());
                values.put(Pinry.Pins.COLUMN_NAME_DESCRIPTION, pin.getDescription());
                values.put(Pinry.Pins.COLUMN_NAME_SYNC_STATE, Pinry.Pins.SyncState.SYNCED);
                values.put(Pinry.Pins.COLUMN_NAME_SOURCE_URL, pin.getSourceUrl());
                values.put(Pinry.Pins.COLUMN_NAME_PUBLISHED, pin.getPublishedDate());

                contentResolver.insert(Pinry.Pins.CONTENT_URI, values);
            }
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
            pullUpdates(account);
        }
    }

    private SyncAdapterImpl getSyncAdapter() {
        if (syncAdapter == null) {
            syncAdapter = new SyncAdapterImpl(this, true);
        }
        return syncAdapter;
    }
}
