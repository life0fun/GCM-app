package com.colorcloud.gcm.dbhelper;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.format.DateUtils;
import android.util.Log;

/** This class is the wrapper layer on top of actual db. 
 *  writing testable code requires indirect layers so we can mock or swap real db layer
 *  without affecting app logic.
 *<code><pre>
 * CLASS:
 * 	Every class that abstracts a database table uses this class to open the database.
 *  Implements DbSyntax to perform SQL operations.
 *
 * RESPONSIBILITIES:
 * 	Opening the database and the adapter
 * 	Closing the database and the adapter
 * 	Instantiating the adapter
 *
 * COLABORATORS:
 *  None.
 *
 * USAGE:
 * 	See FriendTable.java
 *
 *</pre></code>
 */
public class DbAdapter {

    private final static String TAG = "LSAPP_DBA";

    /** will instantiate at open(). Make sure to close with close() */
    protected GcmDatabaseHelper mDbHelper = null;
    /** will instantiate at open(). Make sure to close with close() */
    protected SQLiteDatabase mDb = null;   // SQLiteDb should be hide by DbAdapter.

    // instance variables
    @SuppressWarnings("unused")
    private Context mContext;   // give this class a context...not used for now. but Will be used in some cases later.

    private DbAdapter() { }

    /** Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param context -  the Context within which to work
     */
    private DbAdapter(Context context) {
        this.mContext = context;
    }

    /** Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure.
     *
     * NOTE - YOU MUST CLOSE THE ADAPTER RETURNED WHEN FINISHED
     *
     * @param context - context
     * @return this (self reference, allowing this to be chained in an initialization
     * 				call)
     * @throws SQLException - if the database could be neither opened or created
     */
    public static DbAdapter openForWrite(Context context, final String openFrom) throws SQLException {
        DbAdapter adapter = new DbAdapter(context);
        adapter.mDbHelper = GcmDatabaseHelper.getInstance(context);
        adapter.mDb = adapter.mDbHelper.getWritableDatabase();
        return adapter;
    }

    /** Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure.
     *
     * NOTE - YOU MUST CLOSE THE ADAPTER RETURNED WHEN FINISHED
     *
     * @param context - context
     * @return this (self reference, allowing this to be chained in an initialization
     * 				call)
     * @throws SQLException - if the database could be neither opened or created
     */
    public static DbAdapter openForRead(Context context, final String openFrom) throws SQLException {
        DbAdapter adapter = new DbAdapter(context);
        adapter.mDbHelper = GcmDatabaseHelper.getInstance(context);
        adapter.mDb = adapter.mDbHelper.getReadableDatabase();
        return adapter;
    }

    /** Closes the database and the adapter.
     */
    public void close(final String closeFrom) {
        if (mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
        }
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }


    /** Accessor method to get to the database - getter.
     *  DbAdapter is an indirect layer between app and sqlite db, loose couple, testable code and easy mock.
     * @return SQLiteDatabase instance opened.
     */
    public SQLiteDatabase getDb() {
        return mDb;
    }

    /**
     * purge old records (more than 100 days) from database.
     * return 1 if success, 0 otherwise
     */
    private int purgeStaleDbRecords() {
        int ret=1;   // no error
        long lately = System.currentTimeMillis() - (150*DateUtils.DAY_IN_MILLIS);  // stale = older than 150 days.
        final String oldRecords = "( " +  GcmDatabase.MessageTable.Columns.COL_TIME + " <= " + lately + " )";

        mDb.beginTransaction();
        try {
            ret = mDb.delete(GcmDatabase.MessageTable.TABLE_NAME, oldRecords, null);
            if (ret == 0) {
                // Craig's formular, oldest record = min(starttime) and start from there within 10 days.
                final String deleteOldest = "delete from loctime where starttime <= (select min(starttime) + 10*24*60*60*1000 from loctime)";
                mDb.execSQL(deleteOldest);
                mDb.setTransactionSuccessful();
                ret = 1;
            }
        } catch (Exception e) {
            Log.e(TAG, "purgeDBStaleRecords Exception:" + e.toString());
            // I should really close the db, drop the db, and restart.
            ret = 0;
        } finally {
            mDb.endTransaction();
        }
        return ret;
    }
    
    /**
     * query table using sqlite database, or mock, or cloud
     */
    public Cursor query(String table, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String groupby = null;
        Cursor c = null;
        
        qb.setTables(table);
        //qb.appendWhere(LocationDatabase.LocTimeTable.Columns.LAT + "!= 0 AND " + LocationDatabase.LocTimeTable.Columns.LGT + "!= 0 ");
        c = qb.query(mDb, projection, selection, selectionArgs, groupby, null, sortOrder, null);
        
        return c;
    }
    
    /**
     * insert to table, no retry
     */
    public long insert(String table, String nullColumnHack, ContentValues values ){
        long rowid = -1;
        try{
            rowid = mDb.insert(table, nullColumnHack, values);
        } catch( SQLiteException e){
            Log.e(TAG, "insert DB exception:" + e.toString());
        }
        return rowid;
    }

    /**
     * insert into database, wrap all the exception handlings
     */
    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues values, int conflictAlgorithm) {
        // NoSQL style. one record only, no need for db.beginTransaction()
        long rowId = -1;
        int retries = 2;  // try at most 2 twice
        do {
            try {
                retries--;
                rowId = mDb.insertWithOnConflict(table, nullColumnHack, values, conflictAlgorithm);
                retries = 0; // done
            } catch (SQLiteFullException e) {
                purgeStaleDbRecords();  // reset tries
            } catch (Exception e) {
                Log.e(TAG, "insert DB exception:" + e.toString());
            }
        } while (retries > 0);

        return rowId;
    }
    
    /**
     * delete
     * @param table, selection and selectionArgs
     */
    public int delete(String table, String selection, String[] selectionArgs) {
        return mDb.delete(table, selection, selectionArgs);
    }

    /**
     * update database entry, wrap all exception handlings.
     */
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        int count = 0;
        int retries = 1;
        do {
            try {
                retries--;
                count = mDb.update(table, values, whereClause, whereArgs);
                retries = 0; // done
            } catch (SQLiteFullException e) {
                purgeStaleDbRecords();  // reset tries
            } catch (Exception e) {
                Log.e(TAG, "update DB exception:" + e.toString());
            }
        } while (retries > 0);

        return count;
    }
}
