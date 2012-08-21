package com.colorcloud.gcm.dbhelper;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.colorcloud.gcm.GcmApp.GCMLog;

/** This class extends the SQLiteOpenHelper.class.
 *
 *<pre>
 * CLASS:
 * 	Every class that abstracts a table or view uses this class to open the database.
 *
 * RESPONSIBILITIES:
 * 	Opening the database
 * 	Upgrading the database
 * 	Dropping and recreating the database
 *
 *
 * COLABORATORS:
 * Database helper for Location. Designed as a singleton to make sure that all
 * {@link android.content.ContentProvider} users get the same reference.
 * Provides handy methods for maintaining package and accuracy fix.
 */
public class GcmDatabaseHelper extends SQLiteOpenHelper {

    private final static String TAG = "GCM_DB";

    // database
    public static final String DATABASE_NAME = "gcm.db";
    public static final int DATABASE_VERSION = 1;   //  init

    private static GcmDatabaseHelper sSingleton = null;

    protected SQLiteDatabase mDb = null;

    public static synchronized GcmDatabaseHelper getInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new GcmDatabaseHelper(context);
        }
        return sSingleton;
    }


    /* Private constructor, callers except unit tests should obtain an instance through
    * {@link #getInstance(android.content.Context)} instead.
    */
    protected GcmDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);   // this one set the current db version.
    }

    /** handles the onCreate operation - which is only called when the
     * database does not exist.
     *
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     *
     * @param db - SQLiteDatabase instance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create tables upon creation
        try {
            db.beginTransaction();
            execSql(db, GcmDatabase.MessageTable.CREATE_TABLE_SQL);
            
            db.setMaximumSize(5*1024*1024);
            db.setTransactionSuccessful();
            GCMLog.d(TAG, "onCreate : creating tables...");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }


    /** This method performs a SQLiteDatabase.execSQL(String sql) operation.
     *
     * @param db - database instance
     * @param sql - sql code.
     */
    protected void execSql(SQLiteDatabase db, String sql) {
        Log.i(TAG, sql);
        db.execSQL(sql);
    }


    /** This method handles database upgrades from a SQL perspective. That is,
     * columns can be added to the database, columns can be initialized, etc.
     *
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     *
     * @param db - SQLiteDatabase instance being upgraded
     * @param oldVersion - version number being upgraded from
     * @param newVersion - version number being upgraded to
     *
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        GCMLog.d(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which may destroy old data");

        if (!tableExist(db)) {
            GCMLog.d(TAG, "Tables do not exist, create tables rather than upgrading");
            onCreate(db);
            return;
        }

        try {
            db.beginTransaction();
            if (newVersion > oldVersion) {
                switch (oldVersion) {
                case 1:
                    break;
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        GCMLog.d(TAG, "Upgrading database with added columns version: " + oldVersion);
    }

    /**
     * Override on downgrade, default will throw exception, which is bad.
     * Drop all tables and recreate them manually using the sql defined in current software binary.
     */
    @Override
    public void onDowngrade(final SQLiteDatabase db, int oldVersion, final int newVersion) {
        if( oldVersion < newVersion ){
            try{
                GCMLog.i(TAG, "onDowngrade :" + newVersion + " : " + oldVersion);
                db.execSQL("DROP TABLE IF EXISTS " + GcmDatabase.MessageTable.CREATE_TABLE_SQL);
                onCreate(db);
            }catch(SQLException e){
                GCMLog.e(TAG, "onDowngrade : " + e.toString());
                throw e;
            }
        }
    }

    /**
     * check whether tables exist or not. When upgrade from stable 5 build, db exists with version 8, but tables do not exist.
     * @param db
     * @return true if table exist, false otherwise.
     */
    private boolean tableExist(SQLiteDatabase db) {
        boolean exist = false;
        String tblexist = " select name  from sqlite_master where name= '" +  GcmDatabase.MessageTable.TABLE_NAME + "'" ;
        Cursor c = db.rawQuery(tblexist, null);
        try {
            if (c != null && c.moveToFirst()) {
                exist = true;
                GCMLog.d(TAG, "tableExist: tables exist");
            }
        } catch (Exception e) {
            GCMLog.e(TAG, "tableExist: " + e.toString());
        } finally {
            if (c != null)
                c.close();
        }
        return exist;
    }

    /** handles the drop and recreate operation. This is never called, but
     * is here to allow for support of drop and recreate if it is necessary.
     *
     * @param db - SQLiteDatabase instance
     */
    @SuppressWarnings("unused")
    private void dropAndRecreate(SQLiteDatabase db) {
        // tables
        //db.execSQL("DROP TABLE IF EXISTS " + StateTable.TABLE_NAME);
        //db.execSQL("DROP TABLE IF EXISTS " + MimeTypeTable.TABLE_NAME);
        onCreate(db);
    }
}
