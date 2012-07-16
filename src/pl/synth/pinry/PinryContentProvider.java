package pl.synth.pinry;

import android.accounts.Account;
import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;

import java.util.HashMap;

public class PinryContentProvider extends ContentProvider {
    private static final String TAG = "PinryContentProvider";

    private static final String DATABASE_NAME = "pinry.db";
    private static final int DATABASE_VERSION = 1;

    private static final int PINS = 1;
    private static final int PIN_ID = 2;

    private static final UriMatcher uriMatcher;

    private static HashMap<String, String> pinsProjectionMap;

    private DatabaseHelper openHelper;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(Pinry.AUTHORITY, "pins", PINS);
        uriMatcher.addURI(Pinry.AUTHORITY, "pins/#", PIN_ID);

        pinsProjectionMap = new HashMap<String, String>();

        pinsProjectionMap.put(Pinry.Pins._ID, Pinry.Pins._ID);
        pinsProjectionMap.put(Pinry.Pins.COLUMN_NAME_DESCRIPTION, Pinry.Pins.COLUMN_NAME_DESCRIPTION);
        pinsProjectionMap.put(Pinry.Pins.COLUMN_NAME_SOURCE_URL, Pinry.Pins.COLUMN_NAME_SOURCE_URL);
        pinsProjectionMap.put(Pinry.Pins.COLUMN_NAME_PUBLISHED, Pinry.Pins.COLUMN_NAME_PUBLISHED);
        pinsProjectionMap.put(Pinry.Pins.COLUMN_NAME_IMAGE_PATH, Pinry.Pins.COLUMN_NAME_IMAGE_PATH);
        pinsProjectionMap.put(Pinry.Pins.COLUMN_NAME_IMAGE_URL, Pinry.Pins.COLUMN_NAME_IMAGE_URL);
        pinsProjectionMap.put(Pinry.Pins.COLUMN_NAME_THUMBNAIL_PATH, Pinry.Pins.COLUMN_NAME_THUMBNAIL_PATH);

    }

    @Override
    public boolean onCreate() {
        openHelper = new DatabaseHelper(getContext());

        return true; /* failures reported by thrown exceptions */
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(Pinry.Pins.TABLE_NAME);

        switch(uriMatcher.match(uri)) {
            case PINS:
                queryBuilder.setProjectionMap(pinsProjectionMap);
                break;
            case PIN_ID:
                queryBuilder.setProjectionMap(pinsProjectionMap);
                queryBuilder.appendWhere(
                        Pinry.Pins._ID +
                                "=" +
                                uri.getPathSegments().get(Pinry.Pins.PIN_ID_PATH_POSITION));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);

        }

        String orderBy = Pinry.Pins.DEFAULT_SORT_ORDER;
        SQLiteDatabase db = openHelper.getReadableDatabase();

        Cursor c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;

    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case PINS:
                return Pinry.Pins.CONTENT_TYPE;
            case PIN_ID:
                return Pinry.Pins.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues initial) {
        if (uriMatcher.match(uri) != PINS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;

        if (initial != null) {
            values = new ContentValues(initial);
        } else {
            values = new ContentValues();
        }

        if (!values.containsKey(Pinry.Pins.COLUMN_NAME_PUBLISHED)) {
            values.put(Pinry.Pins.COLUMN_NAME_PUBLISHED, 0);
        }

        if (!values.containsKey(Pinry.Pins.COLUMN_NAME_DESCRIPTION)) {
            values.put(Pinry.Pins.COLUMN_NAME_DESCRIPTION, "");
        }

        if (!values.containsKey(Pinry.Pins.COLUMN_NAME_SYNC_STATE)) {
            values.put(Pinry.Pins.COLUMN_NAME_SYNC_STATE, Pinry.Pins.SyncState.WAITING);
        }

        SQLiteDatabase db = openHelper.getWritableDatabase();

        long rowId = db.insert(
                Pinry.Pins.TABLE_NAME,
                null,
                values);

        if (rowId > 0) {
            Uri pinUri = ContentUris.withAppendedId(Pinry.Pins.CONTENT_ID_URI_BASE, rowId);
            getContext().getContentResolver().notifyChange(pinUri, null);
            if (values.getAsString(Pinry.Pins.COLUMN_NAME_SYNC_STATE) != Pinry.Pins.SyncState.SYNCED) {
                ContentResolver.requestSync(null, Pinry.AUTHORITY, new Bundle());
            }
            return pinUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        return db.delete(Pinry.Pins.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        return db.update(Pinry.Pins.TABLE_NAME, values, selection, selectionArgs);
    }

    static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + Pinry.Pins.TABLE_NAME + " ("
                + Pinry.Pins._ID + " INTEGER PRIMARY KEY,"
                + Pinry.Pins.COLUMN_NAME_SOURCE_URL + " TEXT,"
                + Pinry.Pins.COLUMN_NAME_PUBLISHED + " INTEGER,"
                + Pinry.Pins.COLUMN_NAME_DESCRIPTION + " TEXT,"
                + Pinry.Pins.COLUMN_NAME_IMAGE_PATH + " TEXT,"
                + Pinry.Pins.COLUMN_NAME_IMAGE_URL + " TEXT,"
                + Pinry.Pins.COLUMN_NAME_THUMBNAIL_PATH + " TEXT,"
                + Pinry.Pins.COLUMN_NAME_SYNC_STATE + " TEXT"
                + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
