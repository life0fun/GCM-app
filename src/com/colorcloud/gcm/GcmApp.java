
package com.colorcloud.gcm;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;
import com.parse.PushService;

public class GcmApp extends Application {
    private static final String TAG = "GCM_App";
    
    public static final String REG_ID = "reg_id";
    private SharedPreferences mPref;

    GcmStore mStore;
    private String mGcmRegId = null;
    String mDeviceName;
    ParserService mParser;
    
    /**
     * my server's URL for client to post msg
     */
    static final String SERVER_URL = "http://www.colorcloud.com/";

    /**
     * Google API Project id registered to use GCM.
     * https://code.google.com/apis/console/#project:4815162342
     */
    static final String SENDER_ID = "686172024995";

    /**
     * Intent used to display a message in the main activity
     */
    static final String DISPLAY_MESSAGE_ACTION = "com.colorcloud.gcm.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    static final String EXTRA_MESSAGE = "message";
    
    /**
     * save the reg Id into pref
     */
    public void setGcmRegId(String id) { mGcmRegId = id; setString(REG_ID, id); }
    public String getGcmRegId() { return mGcmRegId; }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mStore = new GcmStore((Context)this, null);
        mPref = getSharedPreferences("gcm", 0);
        //SharedPreferences prefs = getSharedPreferences("gcm", MODE_WORLD_WRITEABLE );
        mDeviceName = "me";
        
        mParser = new ParserService(this);
    }
    
    /**
     * Saves the regId in a shared preference
     * adb shell cat /data/data/com.colorcloud.gcm/shared_prefs/gcm.xml
     */
    public void setString(String k, String v) {
        Editor edit = mPref.edit();
        edit.putString(k, v);
        edit.commit();
    }
    
    /**
     * get the saved pref value
     */
    public String getString(String k){
        String v = mPref.getString(k, null);
        return v;
    }
   
    /**
     * my logger class
     */
    public static class GCMLog {
        public static void i(String tag, String msg) {
            Log.i(tag, msg);
        }
        public static void d(String tag, String msg) {
            Log.d(tag, msg);
        }
        public static void e(String tag, String msg) {
            Log.e(tag, msg);
        }
    }
}
