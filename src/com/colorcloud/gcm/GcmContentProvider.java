package com.colorcloud.gcm;

import static com.colorcloud.gcm.Constants.PACKAGE_NAME;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.format.DateUtils;

import com.colorcloud.gcm.GcmApp.GCMLog;
import com.colorcloud.gcm.dbhelper.GcmDatabase;
import com.colorcloud.gcm.dbhelper.GcmDatabase.MessageTable.Tuple;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *<code><pre>
 * CLASS:
 *  implements content provider for location database
 *  Map uri to db table, ask db adapter to direct the db query, either to the sqlite db or to cloud db.
 *
 * RESPONSIBILITIES:
 *
 * COLABORATORS:
 *
 * USAGE:
 * 	See each method.
 *
 *</pre></code>
 */
public class GcmContentProvider extends ContentProvider {
    private final static String TAG = "LSAPP_Prov";
    public static final String AUTHORITY = PACKAGE_NAME;
    public static final String JSON_CONTENT_DIRECTORY = "jsonvalue";
    public static final String AGGREGATION_CONTENT_DIRECTORY = "aggregate";
    public static final String POITAG_CONTENT_DIRECTORY = "poi";
    public static final String CONSOLIDATE_CONTENT_DIRECTORY = "consolidate";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri MSG_CONTENT_URI = Uri.withAppendedPath(CONTENT_URI, GcmDatabase.MessageTable.TABLE_NAME);
    
    public static final Uri CELL_CONTENT_AGGREGATE_URI = Uri.withAppendedPath(MSG_CONTENT_URI, AGGREGATION_CONTENT_DIRECTORY);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private Context mContext;
    private volatile GcmDatabase mLocationDatabase = null;

    static class MessageUri {
        public static final int MSG_ID = 1;
        public static final int MSG_ITEMS = 2;
    }
    
    static {
        // Contacts URI matching table
        final UriMatcher matcher = sURIMatcher;

        matcher.addURI(AUTHORITY, GcmDatabase.MessageTable.TABLE_NAME, MessageUri.MSG_ITEMS);
        matcher.addURI(AUTHORITY, GcmDatabase.MessageTable.TABLE_NAME+"/#", MessageUri.MSG_ID);
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        //mLocationDatabase = LocationDatabase.getInstance(mContext);
        return true;
    }

    /**
     * lazy initialization, for instant field, double check idom
     * @return the reference to database
     */
    private GcmDatabase getLocationDatabase() {
        GcmDatabase dbhandle = mLocationDatabase;
        if(dbhandle == null) {
            synchronized(this) {
                dbhandle = mLocationDatabase; // double check
                if(dbhandle == null) {
                    mLocationDatabase = dbhandle = GcmDatabase.getInstance(mContext);  // called once
                }
            }
        }
        return dbhandle;
    }

    // MIME type should start with vnd.android.cursor.item for a single record, or vnd.android.cursor.dir/ for multiple items. 
    private static final String DIR_PREFIX = 	"vnd.android.cursor.dir/vnd.gcm.";
    private static final String ITEM_PREFIX = 	"vnd.android.cursor.item/vnd.gcm.";

    @Override
    public String getType(Uri uri) {
        final int match = sURIMatcher.match(uri);
        GCMLog.d(TAG, "getType : " + uri.toString() + "Macthes " + match);
        switch (match) {
            case MessageUri.MSG_ID:
                return ITEM_PREFIX + "messages";
        case MessageUri.MSG_ITEMS:
            return DIR_PREFIX+"messages";		// "vnd.android.cursor.dir/vnd.gcm.messages";
        }
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        
        Cursor c = null;
        int match = sURIMatcher.match(uri);
        GCMLog.d(TAG, "query: " + uri.toString() + "Macthes " + match);
        switch (match) {
        case MessageUri.MSG_ITEMS:
            c = getLocationDatabase().mDbAdapter.query(GcmDatabase.MessageTable.TABLE_NAME, projection, selection,selectionArgs, sortOrder);
            GCMLog.d(TAG, "query :: Matching MSG_ITEMS :" + GcmDatabase.MessageTable.TABLE_NAME);
            break;
     
        default:
            throw new IllegalArgumentException("Unknown URL ::" + uri);
        }

        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        final int matchedUriId = sURIMatcher.match(uri);
        
        switch (matchedUriId) {
        case MessageUri.MSG_ID:
            count = getLocationDatabase().mDbAdapter.delete(GcmDatabase.MessageTable.TABLE_NAME, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            break;
        default:
            throw new UnsupportedOperationException("Cannot delete that URL: " + uri);
        }
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId = 0;
        int match = sURIMatcher.match(uri);
        Uri insertedUri = null;
        GCMLog.d(TAG, "insert: " + uri.toString() + "Macthes " + match);
        //DatabaseUtils.InsertHelper(db, TABLE_NAME).insert(values);
        switch (match) {
        case MessageUri.MSG_ITEMS:
            rowId = getLocationDatabase().mDbAdapter.insert(GcmDatabase.MessageTable.TABLE_NAME, null, values);
            if (rowId > 0) {
                insertedUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(insertedUri, null);
            }
            break;
        
        default:
            break;
        }

        GCMLog.d(TAG, "inserted entry: " + insertedUri);
        return insertedUri;
    }


    @Override
    public int update(Uri url, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        final int matchedUriId = sURIMatcher.match(url);
        switch (matchedUriId) {
        case MessageUri.MSG_ITEMS:
            count = getLocationDatabase().mDbAdapter.update(GcmDatabase.MessageTable.TABLE_NAME, values, selection, selectionArgs);
            break;
        
        default:
            throw new UnsupportedOperationException("Cannot update URL: " + url);
        }

        getContext().getContentResolver().notifyChange(url, null);
        GCMLog.d(TAG, "updated entry: " + url);
        return count;
    }
}
