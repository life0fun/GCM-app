package com.colorcloud.gcm;

import android.content.Context;

import com.colorcloud.gcm.GcmApp.GCMLog;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserService {
    private static final String TAG = "GCM_Parser";
    
    private Context mContext;
    private Gossipy mGossipy;
    
    /**
     * class encapsulate attribute keys stored to cloud
     */
    public static class Gossipy {
        public static String KEY_SENDER = "sender";
        public static String KEY_RECVER = "recver";
        public static String KEY_MSG = "msg";
        public static String KEY_SCORE = "score";
        
        public String sender;
        public String recver;
        public String msg;
        public String score;
        
        public Gossipy(String sender, String recver, String msg, String score) {
            this.sender = sender;
            this.recver = recver;
            this.msg = msg;
            this.score = score;
        }
        
        public Map<String, String> toMap() {
            Map<String, String> m = new HashMap<String, String>();
            m.put(KEY_SENDER, sender);
            m.put(KEY_RECVER, recver);
            m.put(KEY_MSG, msg);
            m.put(KEY_SCORE, score);
            return m;
        }
    }
    
    public ParserService(Context ctx) {
        mContext = ctx;
        initParser();
        
        mGossipy = new Gossipy("hiring manager", "applicant", "good", "90");
        //saveToCloud("Gossipy", mGossipy.toMap());
        getFromCloud("Gossipy", null);
        getFromCloud("whatever", null);
        getFromCloud("Gossi", null);
    }

    
    /**
     * init parser
     * https://parse.com/apps/listen/push_notifications
     */
    public void initParser() {
        // Add your initialization code here
        //Parse.initialize(this, YOUR_APPLICATION_ID, YOUR_CLIENT_KEY);
        Parse.initialize(mContext, "2qF1TwYOVeCRCQn0UclQKlknNJWvrCk3E8JUNIFm", "a4xOGY8C1819eoc5wmDptSkLyRifSBcvBEeQ15U6"); 

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        
        // Optionally enable public read access by default.
        // defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
        
        // subscribe to default bcast channel
        subscribeTo("");
        
        GCMLog.d(TAG, "initParser : done...");
    }
    
    /**
     * subscribe to channel
     */
    public void subscribeTo(String channel){
        GCMLog.d(TAG, "subscribeTo : " + channel);
        // subscribe to the bcast channel upon start
        PushService.subscribe(mContext, channel, MainActivity.class);
    }
    
    /**
     * save object to the cloud
     * string name identify the class name refs to the object stored in the cloud.
     * parser lazily create a class to represent the obj class storeed in the cloud. 
     * ParseObject is a dict map, with a saveInBackground API.
     */
    public void saveToCloud(String objname, Map<String, String> kvpairs) {
        ParseObject storeObj = new ParseObject(objname);   // parse lazily create the class objname for you.
        for(Map.Entry<String, String> entry : kvpairs.entrySet()) {
            storeObj.put(entry.getKey(), entry.getValue());
        }
        storeObj.saveInBackground();
        GCMLog.d(TAG, "saveToCloud : " + storeObj );
    }
    
    /**
     * retrive the object stored.
     * string name identify the class name refs to the object stored in the cloud.
     * parser lazily create a class to represent the obj class storeed in the cloud. 
     * you can get objId by String objectId = gameScore.getObjectId();
     */
    public void getFromCloud( String objKlz, String objId ){
        GCMLog.d(TAG, "getFromCloud :" + objKlz);
        ParseQuery query = new ParseQuery(objKlz);
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List<ParseObject> objlist, ParseException e) {
                if( e == null ){
                    processCloudObjects(objlist);
                }else{
                    handleCloudException(e);
                }
            }
        });
    }
    
    /**
     * get single object with obj id cp/paste from web a4lVeDlIcN
     */
    public void getFromCloud(String objId) {
        String defaultobjId = "a4lVeDlIcN";
    }
    
    /**
     * process the list of object got from cloud
     */
    private void processCloudObjects(List<ParseObject> l) {
        for( ParseObject o : l) {
            String sender = o.getString(Gossipy.KEY_SENDER);
            String recver = o.getString(Gossipy.KEY_RECVER);
            String score = o.getString(Gossipy.KEY_SCORE);
            String msg = o.getString(Gossipy.KEY_MSG);
            GCMLog.d(TAG, " processCloudObjects : " + sender + " : " + recver + " : " + msg + " : " + score);
        }
    }
    
    /**
     * notify connection to cloud exception
     */
    private void handleCloudException(ParseException e) {
        GCMLog.e(TAG, "Cloud Exception : " + e.toString());
    }
}
