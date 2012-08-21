/*
 * (c) COPYRIGHT 2009-2012 MOTOROLA INC.
 * MOTOROLA CONFIDENTIAL PROPRIETARY
 * MOTOROLA Advanced Technology and Software Operations
 *
 * REVISION HISTORY:
 * Author        Date       CR Number         Brief Description
 * ------------- ---------- ----------------- ------------------------------
 * e51141        2010/08/27 IKCTXTAW-19		   Initial version
 */
package com.colorcloud.gcm.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import static com.colorcloud.gcm.Constants.*;

/**
 *<code><pre>
 * CLASS:
 *  implements operations on database
 *
 * RESPONSIBILITIES:
 *  insert, update, delete
 *
 * COLABORATORS:
 *
 * USAGE:
 * 	See each method.
 *
 *</pre></code>
 */
public class GcmDatabase {

    // types
    public static final String AUTO_INCREMENT   = " AUTOINCREMENT";
    public static final String PRIMARY_KEY      = " PRIMARY KEY";
    public static final String NOT_NULL         = " NOT NULL";
    public static final String UNIQUE           = " UNIQUE";
    public static final String UNIQUE_REPLACE   = UNIQUE+" ON CONFLICT REPLACE";

    public static final String TEXT_TYPE            = " TEXT";
    public static final String BLOB_TYPE            = " BLOB";
    public static final String LONG_TYPE            = " LONG";
    public static final String INTEGER_TYPE         = " INTEGER";
    public static final String REAL_TYPE            = " REAL";
    public static final String DATE_TYPE            = LONG_TYPE;
    public static final String CONT                 = ", ";
    public static final String KEY_TYPE             = INTEGER_TYPE;
    public static final String KEY_TYPE_NOT_NULL    = INTEGER_TYPE+NOT_NULL;
    public static final String PKEY_TYPE            = KEY_TYPE + PRIMARY_KEY + AUTO_INCREMENT;
    public static final String DATE_TIME_TYPE       = LONG_TYPE;
    public static final String LAT_LONG_TYPE        = REAL_TYPE;
    public static final String STATE_TYPE           = INTEGER_TYPE;

    
    protected final Context mContext;
    public DbAdapter mDbAdapter;    // an indirect layer to the sqlite db, or in the cloud.

    private static GcmDatabase sSingleton = null;
    public static synchronized GcmDatabase getInstance(Context context) {
        if (sSingleton == null) {
            sSingleton  = new GcmDatabase(context);
        }
        return sSingleton;
    }

    protected GcmDatabase(Context c) {
        mContext = c;
        mDbAdapter = DbAdapter.openForWrite(c, null);
    }
    
    public static class MessageTable extends BaseTable {
        /** This is the name of the table in the database */
        public static final String TABLE_NAME 	= "messages";
        
        /** Currently not used, but could be for joining this table with other tables. */
        public static final String SQL_REF 		= "messages";

        /** Friend name - column name */
        public static class Columns {   // col def pointed to global constant key.
            public final static String _ID		= "_id";
            public final static String COL_SENDER	= SENDER;
            public final static String COL_TIME		= TIME;
            public final static String COL_BODY		= BODY;
            public final static String COL_RESPONSE	= RESPONSE;
            
            // column index matters when used by cursor to tuple!
            final static String[] NAMES = {_ID, COL_SENDER, COL_TIME, COL_BODY, COL_RESPONSE };

            public static String[] getNames() {
                return (String[])NAMES.clone();
            }

            public static final int sSenderIdx = 1;
            public static final int sTimeIdx  = 2;
            public static final int sBodyIdx = 3;
            public static final int sResponseIdx = 4;
        }

        /** SQL statement to create the Friend Table */
        public static final String CREATE_TABLE_SQL =
            "CREATE TABLE " + TABLE_NAME + " (" +
            Columns._ID					    + PKEY_TYPE			+ CONT+
            Columns.COL_SENDER				+ TEXT_TYPE         + CONT+
            Columns.COL_TIME	    		+ DATE_TIME_TYPE	+ CONT+
            Columns.COL_BODY	    		+ TEXT_TYPE			+ CONT+
            Columns.COL_RESPONSE    		+ TEXT_TYPE			+
            ")";

        @Override
        public String getTableName() { return TABLE_NAME; }
        @Override
        public String getFkColName() { return null; }
        
        protected static int[] getColumnNumbers(Cursor cursor, String sqlRef) {
            return BaseTable.getColumnNumbers(cursor, Columns.NAMES);
        }

        public static ContentValues toContentValues(MessageTable.Tuple tuple){
            return toContentValues(tuple,false);
        }
        
        public static ContentValues toContentValues(MessageTable.Tuple tuple, boolean ignoreName) {
            ContentValues args = new ContentValues();

            if (tuple.get_id() > 0) {
                args.put(Columns._ID, tuple.get_id());
            }
            args.put(Columns.COL_SENDER,		tuple.getSender());
            args.put(Columns.COL_TIME,          tuple.getTime());
            args.put(Columns.COL_BODY,			tuple.getBody());
            args.put(Columns.COL_RESPONSE,		tuple.getResponse());
            return args;
        }

        @SuppressWarnings("unchecked")
        public static Tuple toTuple(Cursor cursor) {
            int ix = 0;
            int[] colNumbers = getColumnNumbers(cursor, "");

            Tuple tuple = new Tuple(
                cursor.getLong(colNumbers[ix++]), 			//_id
                cursor.getString(colNumbers[ix++]),  		// sender
                cursor.getLong(colNumbers[ix++]),            // time
                cursor.getString(colNumbers[ix++]),  		// body
                cursor.getString(colNumbers[ix++])  		// response
            );

            if (ix != colNumbers.length) {
                throw new UnsupportedOperationException("colNumbers length = "+
                                                        colNumbers.length+" and ix = "+ix+" do not match");
            }
            return tuple;
        }

        public static class Tuple extends BaseTuple {
            /** name of this MimeType */
            private   String	sender;
            private   long      time;
            private   String	body;
            private   String  	response;

            public Tuple() {
                super();
            }

            public Tuple(long _id, final String sender, final long time, final String body, final String response){
                super();
                this._id = _id;
                this.sender = sender;
                this.time = time;
                this.body = body;
                this.response = response;
            }

            public String getSender() {
                return sender;
            }
            public void setPoi(String sender) {
                this.sender = sender;
            }

            public long getTime() {
                return time;
            }
            public void setTime(long timestamp) {
                this.time = timestamp;
            }

            public String getBody() {
                return body;
            }
            public void setBody(String body) {
                this.body = body;
            }

            public String getResponse() {
                return response;
            }
            public void setResponse(String response) {
                this.response = response;
            }
            
            /** converts this instance to a string.
             *
             * @see com.motorola.BaseTuple.location.ils.db.table.TupleBase#toString()
             */
            public String toString() {
                StringBuilder result = new StringBuilder();
                result.append(TABLE_NAME);

                result.append(", _id="+this._id);
                result.append(", sender="+this.sender);
                result.append(", time="+this.time);
                result.append(", body="+this.body);
                result.append(", response="+this.response);
                return result.toString();
            }
        }       
    }
}


