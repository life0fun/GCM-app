/*
 * @(#)TableBase.java
 *
 * (c) COPYRIGHT 2009-2011 MOTOROLA INC.
 * MOTOROLA CONFIDENTIAL PROPRIETARY
 * MOTOROLA Advanced Technology and Software Operations
 *
 * REVISION HISTORY:
 * Author        Date       CR Number         Brief Description
 * ------------- ---------- ----------------- ------------------------------
 * ACD100        2009/07/27 NA				  Initial version
 * ACD100 		 2009/11/20 NA				  Conversion for Endive
 *
 */
package com.colorcloud.gcm.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.provider.BaseColumns;
import android.util.Log;


/**This class is a base class for any table in the system.
 *
 *<code><pre>
 * CLASS:
 *  implements DbSyntax to support SQL statement assembly.
 *
 * RESPONSIBILITIES:
 * 	Insert, update, delete, fetch, convert to Tuple.
 *
 * COLABORATORS:
 * 	TupleBase - encapsulates a generic row in the table.
 *
 * USAGE:
 * 	See each method.
 *
 *</pre></code>
 */
public abstract class BaseTable {

    private static final String TAG = "TableBase";

    /** gets the table name */
    public abstract String getTableName();
    /** gets the foreign key row _id column name */
    public abstract String getFkColName();
    /** requires sub classes to implement toContentValues() */
    public static <T extends BaseTuple> ContentValues toContentValues(T _tuple) {
        return null;
    };
    /** requires sub classes to implement toTuple() */
    public static <T extends BaseTuple> T toTuple(Cursor cursor) {
        return null;
    };
    /** requires sub classes to implement getColumnNumbers */
    protected static int[] getColumnNumbers(Cursor cursor, String sqlRef) {
        return null;
    };

    public static final String CONTENT_TYPE_PREFIX = "vnd.motorola.cursor.dir/vnd.motorola.";
    public static final String CONTENT_ITEM_TYPE_PREFIX = "vnd.motorola.cursor.item/vnd.motorola.";

    public static interface Columns extends BaseColumns {
    }

    /** Basic constructor  - hide */
    public BaseTable() {
        super();
    }


    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of categories.
     */
    public String getContentType() {
        return CONTENT_TYPE_PREFIX+getTableName();
    }

    /**
     * The MIME type of a {@link #CONTENT_URI} type of a single item.
     */
    public String getContentItemType() {

        return CONTENT_ITEM_TYPE_PREFIX+getTableName();
    }


    /** gets a where clause for fetching by row id.
     *
     * @param _id - row (_id) to fetch
     * @return - where clause string to find a given row id (_id).
     */
    public static String get_idWhereClause(long _id) {
        return " WHERE " +Columns._ID+" = "+_id;
    }


    /** inserts a row into the given subclass table.
     *
     * @param context - context
     * @param values - values to insert, must include an _id column.
     * @return - new record inserted key value
     */
    public long insert(Context context, ContentValues values) {

        long result = -1;
        DbAdapter dbAdapter = DbAdapter.openForWrite(context, TAG+".0");

        try {
            result = dbAdapter.getDb().insertOrThrow(this.getTableName(), null, values);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dbAdapter != null)
                dbAdapter.close(TAG+".0");
        }
        return result;
    }


    /** inserts a tuple into the given subclass table.
     *
     * @param <T> - tuple type
     * @param context - context
     * @param tuple - subclass tuple
     * @return -1 if not added or primary key (_id) of added record
     */
    public <T extends BaseTuple> long insert(Context context, T tuple) {

        long result = 0;
        DbAdapter dbAdapter = DbAdapter.openForWrite(context, TAG+".1");
        try {
            result = insert(dbAdapter.getDb(), tuple);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();

        } finally {
            if (dbAdapter != null)
                dbAdapter.close(TAG+".1");
        }
        return result;
    }


    /** inserts a table record.
     *
     * Command line example:
     * 		insert into Friend(Name, Phone) values("Fred", "8472221111");
     *
     * @param mDb - instance of SQLiteDatabase.
     * @param tuple - tuple to add to the db.
     *
     * @return -1 if not added or primary key (_id) of added record
     */
    @Deprecated   // this is abstract class, why do we need instance base ?
    public <T extends BaseTuple> long insert(SQLiteDatabase mDb, T tuple) {
        // insert Friend
        long key = mDb.insertOrThrow(this.getTableName(), null, toContentValues(tuple));
        tuple.set_id(key);
        return key;
    }


    /** fetches all the records in the table.
     *
     * @param mDb - database instance
     * @param tableName - table name
     * @param orderById - if true, will order by row _id, else random order
     *
     * @return - cursor, CALLER MUST CLOSE - confirmed all.
     */
    public static Cursor fetchAll(SQLiteDatabase mDb, String tableName, boolean orderById) {

        String[] selectionArgs = null;
        String sql = " select * from "+tableName;
        if (orderById)
            sql = sql.concat(" order by "+Columns._ID+" desc");
        Cursor cursor = mDb.rawQuery(sql, selectionArgs);
        Log.i(TAG, "fetchAll sql="+ sql+",  getCount()="+cursor.getCount());
        return cursor;
    }


    /** Fetches a series of records matching the whereClause.
     *
     * @param context - context
     * @param whereClause - The whereClause should not include the word "where".
     * @param orderByClause - null or an order by clause
     * @return - cursor of records found.
     */
    public Cursor fetchWhere(Context context, String whereClause, String orderByClause) {

        Cursor result = null;
        DbAdapter dbAdapter = DbAdapter.openForRead(context, TAG+".6");
        try {
            result = fetchWhere(dbAdapter.getDb(), whereClause, orderByClause);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (dbAdapter != null)
                dbAdapter.close(TAG+".6");
        }
        return result;
    }

    /** fetches all records meeting the whereClause condition.
    *
    * @param mDb- database instance
    * @param whereClause - where clause
    * @param orderByClause - order by clause
    *
    * @return - cursor, CALLER MUST CLOSE - confirmed all
    */
   public Cursor fetchWhere(SQLiteDatabase mDb,
                            String whereClause, String orderByClause) {

       String[] selectionArgs = null;
       String sql = " select * from "+getTableName();

       // prefix where clause with "WHERE" if missing.
       if (whereClause.indexOf("WHERE") < 0)
           whereClause = " WHERE "+whereClause;

       // add whereClause to sql.
       sql = sql.concat(whereClause);

       // concat ORDER BY clause, if none, adds order by primary key
       if (orderByClause != null && orderByClause.length() > 0)
           sql = sql.concat((orderByClause.indexOf(" order by ") > -1 ?
                             orderByClause: " order by "+orderByClause) );
       else
           sql = sql.concat(" order by " +Columns._ID);

       Cursor cursor = mDb.rawQuery(sql, selectionArgs);
       if (cursor != null) {
           cursor.moveToFirst(); // THIS IS REQUIRED OR ELSE AN EMPTY CURSOR IS RETURNED, WHEN IT SHOULD NOT BE!!!
           Log.i(TAG, "fetchWhere sql="+ sql+",  getCount()="+cursor.getCount());
       }
       return cursor;
   }

    
    /** fetches 1 record using the primary key.
     *
     * @param <T> - return type
     * @param context - context
     * @param key - primary key value
     *
     * @return - record found or null
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseTuple> T fetch1(Context context, long key) {

        T tuple = null;
        DbAdapter dbAdapter = DbAdapter.openForRead(context, TAG+".2");
        try {
            tuple = (T) fetch1(context, dbAdapter.getDb(), Columns._ID, key+"");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dbAdapter != null)
                dbAdapter.close(TAG+".2");
        }
        return tuple;
    }


    /** fetches 1 record using the primary key.
     *
     * @param <T> - return type
     * @param mDb - database instance
     * @param key - primary key value
     *
     * @return - record found or null
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseTuple> T fetch1(Context context, SQLiteDatabase mDb, long keyValue) {

        return (T) fetch1(context, mDb, Columns._ID, keyValue+"");
    }


    /** fetches 1 record in the table using a supplied key column and match value.
     *
     * @param context - context
     * @param lookupColumn - string containing column name to match with matchValue.
     * @param matchValue - match value to match within the key column
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseTuple> T fetch1(	final Context context,
                                           final String lookupColumn,
                                           final String matchValue) {

        T tuple = null;
        DbAdapter dbAdapter = DbAdapter.openForRead(context, TAG+".3");
        try {
            tuple = (T) fetch1(context, dbAdapter.getDb(), lookupColumn, lookupColumn);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (dbAdapter != null)
                dbAdapter.close(TAG+".3");
        }
        return tuple;
    }


    /** fetches 1 record in the table using a supplied key column and match value.
     *
     * @param context - context
     * @param mDb - database instance
     * @param lookupColumn - string containing column name to match with matchValue.
     * @param matchValue - match value to match within the key column - must use % if need LIKE type match,
     * 				otherwise EQUALS will be used.
     * @return - null if not found or instance of the class for which this tuple is based upon.
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseTuple> T fetch1(
        Context context,
        SQLiteDatabase mDb,
        final String lookupColumn,
        final String matchValue) {

        Cursor cursor = null;
        T result = null;
        try {
            String matchType = (matchValue.indexOf("%") > -1? " like " : " equals ");
            cursor = fetchWhere(mDb, lookupColumn +matchType+ "'"+matchValue+"'", null);
            if (cursor.moveToFirst())
                result = (T)toTuple(cursor);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (cursor != null)
                cursor.close();
        }
        return result;
    }


    /** updates a record from the tuple primary key using values in the tuple.
     *
     * @param <T> - type of TupleBase
     * @param context - context
     * @param tuple - tuple to update in the database
     *
     * @return - number of records updated, should be 1 if found or 0 if tuple
     * primary key not found in the database.
     */
    public <T extends BaseTuple> int update(Context context, T tuple) {

        int result = 0;
        DbAdapter dbAdapter = DbAdapter.openForWrite(context, TAG+".4");
        try {
            result = update(dbAdapter.getDb(), tuple);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();

        } finally {
            if (dbAdapter != null)
                dbAdapter.close(TAG+".4");
        }
        return result;
    }


    /** update the tuple, using the tuple instance and the primary key of the tuple.
     *
     * @param mDb - SQLiteDatabase instance
     * @param tuple - Tuple to be updated
     *
     * @return - number of records updated, should be 1 if found or 0 if
     *  tuple primary key not found in the database.
     */
    public <T extends BaseTuple> int update(SQLiteDatabase mDb, T tuple) {

        String whereClause = Columns._ID + " equals " + tuple.get_id();
        String whereArgs[] = null;

        int result = mDb.update(
                         getTableName(),
                         toContentValues(tuple),
                         whereClause,
                         whereArgs
                     );
        return result;
    }


    /** Deletes 1 record in the table using the primary key.
     *
     * @param context - context
     * @param _id - primary key (_id)
     *
     * @return - 1 if found and deleted, or 0 if not.
     */
    public int delete(Context context, long _id) {

        int result = 0;
        DbAdapter dbAdapter = DbAdapter.openForWrite(context, TAG+".5");
        try {
            result = deleteTuple(dbAdapter.getDb(), _id);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (dbAdapter != null)
                dbAdapter.close(TAG+".5");
        }
        return result;
    }


    /** Delete one tuple based on _id (primary key)
     *
     * @param mDb - SQLiteDatabase instance
     * @param _id id (primary key) of tuple to delete
     *
     * @return - 1 if found and deleted, or 0 if not.
     */
    public int deleteTuple(SQLiteDatabase mDb, long _id) {

        String whereClause = Columns._ID + " equals " + _id;
        String whereArgs[] = null;
        int deleted = mDb.delete(getTableName(), whereClause, whereArgs);

        return deleted;
    }


    /** gets the row id of the primary key either via the foreign key
     * or the standard _id key value. This is important for inheritance.
     *
     * @param cursor - result cursor.
     * @return - string value of the column name used to retrieve the key.
     */
    protected String get_idColName(Cursor cursor) {

        String result = null;
        try {
            // check foreign key column name, if not found, throws IllegalArgumentException
            result = getFkColName();
            cursor.getColumnIndexOrThrow(getFkColName());

        } catch (IllegalArgumentException e) {

            // use standard _id column name, which exists in every table
            result = Columns._ID;
            cursor.getColumnIndexOrThrow(Columns._ID);
        }
        return result;
    }


    /** gets the column number array for a given set of column names.
     *
     * @param cursor - cursor to fetch column numbers.
     * @param colNames - array of column names to be used to get column numbers.
     *
     * @return - integer array of column numbers
     */
    public static int[] getColumnNumbers(Cursor cursor, String[] colNames) {

        int[] result = new int[colNames.length];
        for (int i=0; i< result.length; i++) {
            result[i] = cursor.getColumnIndexOrThrow (colNames[i]);
        }
        return result;
    }


    /** delete a series of records using a where clause
     *
     * @param context - context
     * @param whereClause - standard where clause
     * @return 1 if found & deleted, else 0;
     */
    public int massDelete(Context context, String whereClause) {

        DbAdapter dbAdapter = DbAdapter.openForWrite(context, TAG+".6");

        int result = 0;
        if (dbAdapter != null) {
            dbAdapter.getDb().beginTransaction();

            try {
                result = deleteWhere(dbAdapter.getDb(), whereClause);
                dbAdapter.getDb().setTransactionSuccessful();

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                dbAdapter.getDb().endTransaction();
                dbAdapter.close(TAG+".6");
            }
        }

        return result;
    }


    /** delete a series of records using a where clause.
     *
     * @param mDb - database instance
     * @param whereClause - standard where clause
     * @return 1 if found & deleted, else 0;
     */
    public int deleteWhere(SQLiteDatabase mDb, String whereClause) {

        String whereArgs[] = null;
        if (whereClause.startsWith(" WHERE "))
            whereClause = whereClause.replace("WHERE", "");

        int relateRecordsDeleted =
            mDb.delete(
                getTableName(),
                whereClause,
                whereArgs);

        return relateRecordsDeleted;
    }




    /** returns the index of a field in a list of fields.  Be careful when using this
     * as the column order is not predictable when querying a database.
     *
     * @param array - array of column names.
     * @param f - column name within the array being searched for.
     * @return - index if found or -1 if not found.
     */
    public static int indexOf(final String[] array, final String f) {

        boolean found = false;
        int ix = -1;
        if (array != null)
            while (!found && ix++<array.length) {
                found = (array[ix].equals(f));
            }
        return ix;
    }





    /**This class is a base class for any tuple abstraction in the system.
     *
     *<code><pre>
     * CLASS:
     *	extends ParcelableBase for parceling of tuple instances.
     *
     * RESPONSIBILITIES:
     * 	create Tuple instance from fields or from parcel.
     * 	support get and set for row id (_id).
     *
     * COLABORATORS:
     * 	None
     *
     * USAGE:
     * 	See each method.
     *</pre></code>
     */
    public static class BaseTuple {

        /** primary key of the tuple */
        protected long 	_id;


        /** basic constructor */
        protected BaseTuple() {
            super();
        }


        /** getter - _id.
         *
         * @return the _id
         */
        public long get_id() {
            return _id;
        }


        /** setter - _id.
         *
         * @param _id the _id to set
         */
        public void set_id(long _id) {
            this._id = _id;
        }

        /** This function will set the primary key value to zero
         * which will cause the various Table insert function to not insert the
         * _id value as one of the ContentValues. Therefore, those insert functions
         * will generate a new primary key.
         */
        public void setIdToGenerateNewKeyOnInsert() {
            this._id = 0;
        }

        /** toString - _id.
         * @return _id - _id in string form.
         */
        public String toString() {
            return " _id="+_id;
        }

    }

}
