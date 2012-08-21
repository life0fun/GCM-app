package com.colorcloud.gcm;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.colorcloud.gcm.GcmApp.GCMLog;
import com.google.android.gcm.GCMRegistrar;

/**
 * Service that attempts to register with C2DM from Google
 *
 */
public class GcmService extends Service {

    private static final String TAG = "GCM_Serv";
    
    private static final String CLIENT_ID = "thezenofworld@gmail.com";
    private static final String PROJECT_ID = "https://code.google.com/apis/console/?pli=1#project:686172024995:access";
	
    public static final int MSG_GCM_REGID = 1001;
    public static final int MSG_GET_REGID = 1002;
    public static final int MSG_GCM_MESSAGE = 1003;
    
    public static final int MSG_GCM_NULL = 1002;
    
    private static GcmService _instance = null;
    
    private  WorkHandler mWorkHandler;
    private  MessageHandler mHandler;
    
    GcmApp mApp;
    
    /**
     * singleton initialization
     */
    private void _initialize() {
        if (_instance != null) {
            GCMLog.d(TAG, "_initialize, already initialized, do nothing.");
            return;
        }
        
        _instance = this;
        mWorkHandler = new WorkHandler(TAG);
        mHandler = new MessageHandler(mWorkHandler.getLooper());
        mApp = (GcmApp)getApplication();
        
        mHandler.sendEmptyMessage(MSG_GET_REGID);
    }
    
    public static GcmService getInstance(){
        return _instance;
    }
    
	@Override
	public IBinder onBind(Intent intent) { return null; }

	@Override
	public void onCreate() {
		super.onCreate();
		_initialize();
		GCMLog.d(TAG, "Service created !");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		GCMRegistrar.onDestroy(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    _initialize();
        processIntent(intent);
        return START_STICKY;
	}
	
    private void processIntent(Intent intent){
        if( intent == null){
            return;
        }
        
        String action = intent.getAction();
        GCMLog.d(TAG, "processIntent: " + intent.toString());
    }
	
	/**
	 * reg to GCM intent
	 */
    @Deprecated
	private Intent getGCMRegistrationIntent(String senderProjectId){
	    Intent intentGCM = new Intent("com.google.android.c2dm.intent.REGISTER");
        intentGCM.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
        // this must be the registered gmail account, the comment in the tutorial is wrong
        // sender ID is project ID in api console. Not the gmail id
        intentGCM.putExtra("sender", senderProjectId);
        return intentGCM;
	}
	
	public Handler getHandler() {
        return mHandler;
    }

    /**
     * message handler looper to handle all the msg sent to location manager.
     */
    final class MessageHandler extends Handler {
        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            processMessage(msg);
        }
    }
    
    /**
     * send msg to GcmService
     */
    public static void postMessage(int what, int arg1, Object obj) {
        Message msg = GcmService.getInstance().getHandler().obtainMessage();
        msg.what = what;
        msg.arg1 = arg1;
        msg.obj = obj;
        GcmService.getInstance().getHandler().sendMessage(msg);
    }
    
    /**
     * the main message process loop.
     */
    private void processMessage(android.os.Message msg) {        
        switch (msg.what) {
            case MSG_GET_REGID:
                // get GcmRegistration id upon service start.
                final String regId = ServerUtils.getGcmRegistrationId(this);
                if( regId != null && ! "".equals(regId)){
                    mApp.setGcmRegId(regId);
                }
                break;
            case MSG_GCM_REGID:
                GCMLog.d(TAG, "processMessage: MSG_GCM_REGID " + (String)msg.obj);
                mApp.setGcmRegId((String)msg.obj);
                break;
            case MSG_GCM_MESSAGE:
                final String message = (String)msg.obj;
                GCMLog.d(TAG, "processMessage: MSG_GCM_MESSAGE " + message);
                ServerUtils.showNotification(this, message);
                mApp.mStore.storeMessage(message);
                break;
            default:
                break;
        }
    }
}