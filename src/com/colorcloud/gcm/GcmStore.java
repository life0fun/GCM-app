package com.colorcloud.gcm;

import static com.colorcloud.gcm.Constants.BODY;
import static com.colorcloud.gcm.Constants.RESPONSE;
import static com.colorcloud.gcm.Constants.SENDER;
import static com.colorcloud.gcm.Constants.TIME;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.colorcloud.gcm.GcmApp.GCMLog;
import com.colorcloud.gcm.dbhelper.GcmDatabase;
import com.colorcloud.gcm.dbhelper.GcmDatabase.MessageTable.Tuple;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * For testable code, all the collaborator of the class needs to be DepInjed in for easy mock.
 * when testing this module, you can control the return value from mocked collaborator so you can test edge situations.
 * In the same principle, the customer of this class needs to DepInj this object for easy mock to test
 * the logic inside the its own.  
 */

public class GcmStore {
    private static final String TAG = "GCM_Store";
    
    private Context mContext;   // DI context as the basis for all operations
    
    public GcmStore( Context ctx, ContentProvider prov) {
        mContext = ctx;
        // ideally, should DepInj the provider itself.
        // do not look for things indirectly thru service locator !
    }
    
    
    /**
     * store the msg into content provider.
     * content provider specified by Uri.
     */
    public Uri storeMessage(String msg) {
        Uri msguri = GcmContentProvider.MSG_CONTENT_URI;
        long nowtime = System.currentTimeMillis();
        ContentValues msgval = new ContentValues();
        msgval.put(SENDER, "gcm");
        msgval.put(TIME, nowtime);
        msgval.put(BODY, msg);
        msgval.put(RESPONSE, "readden");
        
        Uri entry = mContext.getContentResolver().insert(msguri, msgval);
        GCMLog.d(TAG, "storeMessage : " + entry);
        return entry;
    }
    
    public Uri storeMessage(GcmMessage msg){
        Uri msguri = GcmContentProvider.MSG_CONTENT_URI;
        long nowtime = System.currentTimeMillis();
        ContentValues msgval = new ContentValues();
        msgval.put(SENDER, msg.mSender);
        msgval.put(TIME, nowtime);
        msgval.put(BODY, msg.mBody);
        msgval.put(RESPONSE, msg.mResponse);
        
        Uri entry = mContext.getContentResolver().insert(msguri, msgval);
        GCMLog.d(TAG, "storeMessage : " + entry);
        return entry;
    }
        
    /**
     * query and return a list of msgs
     */
    public List<GcmMessage> getMessages(long past, long pgstart, int pgamt) {
        List<GcmMessage> msglist = new ArrayList<GcmMessage>();
        Uri msguri = GcmContentProvider.MSG_CONTENT_URI;
        String where = "( " +  TIME + " >= " + past + " )";
        String orderby = "_ID DESC";    // lastest entry on top.
        long offset = 0;
        int cnt = 0;

        Cursor c = mContext.getContentResolver().query(msguri, null, where, null, orderby);
        try{
            if( c != null && c.moveToFirst()) {
                do{
                    if(offset >= pgstart && cnt < pgamt ){
                        Tuple t = GcmDatabase.MessageTable.toTuple(c);
                        GCMLog.d(TAG, "getMessages : " + t.toString());
                        String time = new SimpleDateFormat("MM-dd HH:mm").format(new Date(t.getTime()));
                        GcmMessage msg = new GcmMessage(t.getSender(), time, t.getBody(), t.getResponse());
                        msglist.add(msg);
                        cnt += 1;
                    }
                    offset += 1;
                }while(c.moveToNext());
            }
        }catch(Exception e){
            GCMLog.e(TAG, e.toString());
        }finally{
            if( c != null) c.close();
        }
        return msglist;
    }
    
    /**
     * get the latest msg
     */
    public GcmMessage getLastMessage() {
        Uri msguri = GcmContentProvider.MSG_CONTENT_URI;
        String orderby = "_ID DESC";    // lastest entry on top.
        
        GcmMessage msg = null;
        Cursor c = mContext.getContentResolver().query(msguri, null, null, null, orderby);
        try{
            if( c != null && c.moveToFirst()) {
                do{
                    Tuple t = GcmDatabase.MessageTable.toTuple(c);
                    String time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date(t.getTime()));
                    msg = new GcmMessage(t.getSender(), time, t.getBody(), t.getResponse());
                }while(c.moveToNext());
            }
        }catch(Exception e){
            GCMLog.e(TAG, e.toString());
        }finally{
            if( c != null) c.close();
        }
        return msg;
    }
}
