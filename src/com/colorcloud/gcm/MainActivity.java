package com.colorcloud.gcm;

import static com.colorcloud.gcm.GcmApp.DISPLAY_MESSAGE_ACTION;
import static com.colorcloud.gcm.GcmApp.EXTRA_MESSAGE;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.colorcloud.gcm.GcmApp.GCMLog;

import java.util.List;

/**
 * Main UI of the app
 */
public class MainActivity extends Activity implements ActionBar.TabListener {
    private static final String TAG = "GCM_Act";
    
    private static final String Regtab = "Registration";
    private static final String Msgtab = "Messages";
    
    GcmApp mApp;
    
    RegFragment mRegFrag = null;
    MsgFragment mMsgFrag = null;
    Fragment mCurFrag = null;
    List<Fragment> mFrags;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //The following two options trigger the collapsing of the main action bar view.
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        actionBar.addTab(actionBar.newTab().setText(Regtab).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(Msgtab).setTabListener(this));
       
        mApp = (GcmApp)getApplication();
        
        registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
        
        startGcmService();   // start gcm service, if not being started yet.
    }

    @Override public void onResume() { super.onResume(); }
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) { }
   
    /**
     * Your layout must include a ViewGroup in which you place each Fragment associated with a tab. 
     * Be sure the ViewGroup has a resource ID so you can reference it from your tab-swapping code. 
     * Alternatively, if the tab content will fill the activity layout (excluding the action bar), 
     * then your activity doesn't need a layout at all (you don't even need to call setContentView()). 
     * Instead, you can place each fragment in the default root ViewGroup, which you can refer to with the android.R.id.content.
     */
    //@Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        GCMLog.d(TAG, "onTabSelected  " + tab.getText());
        if(tab.getText().equals(Regtab)){
            if( mRegFrag == null ){
                mRegFrag = RegFragment.newInstance(this, "init");
                ft.add(R.id.fragrootframe, mRegFrag, "tag_regfrag");
                GCMLog.d(TAG, "onTabSelected: add regfrag");
            }else {
                ft.attach(mRegFrag);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                GCMLog.d(TAG, "onTabSelected: replace with reg frag");
            }
            setCurFrag(mRegFrag);
        }else if( tab.getText().equals(Msgtab)){
            if( mMsgFrag == null ){
                mMsgFrag = MsgFragment.newInstance(this, "init");
                ft.add(R.id.fragrootframe, mMsgFrag, "tag_msgfrag");
                GCMLog.d(TAG, "onTabSelected: add msgfrag");
            } else {
                ft.attach(mMsgFrag);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                GCMLog.d(TAG, "onTablSelected: replace with msg frag");
            }
            setCurFrag(mMsgFrag);
        }
    }
    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) { 
        if( tab.getText().equals(Regtab)) {
            ft.detach(mRegFrag);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            GCMLog.d(TAG, "onTablSelected: un-select reg frag");
        } else if ( tab.getText().equals(Msgtab)){
            ft.detach(mMsgFrag);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            GCMLog.d(TAG, "onTablSelected: un-select msg frag");
        }
    }
    
    private void setCurFrag(Fragment f) { mCurFrag = f; }
    private Fragment getCurFrag() { return mCurFrag; }

    /**
     * the API of on create options menu is diff in activity than in fragment
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            /*
             * Typically, an application registers automatically, so options
             * below are disabled. Uncomment them if you want to manually
             * register or unregister the device (you will also need to
             * uncomment the equivalent options on options_menu.xml).
             */
            /*
            case R.id.options_register:
                GCMRegistrar.register(this, SENDER_ID);
                return true;
            case R.id.options_unregister:
                GCMRegistrar.unregister(this);
                return true;
             */
            case R.id.options_clear:
                return true;
            case R.id.options_regid:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mHandleMessageReceiver);
        super.onDestroy();
    }

    /**
     * start the service upon activity created
     */
    private void startGcmService() {
        Intent serviceIntent = new Intent(this, GcmService.class);
        ComponentName component = startService(serviceIntent);  // start
    }
    
    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
    
    /**
     * only called from gcm intent recvr upon registered to gcm and got reg Id.
     */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getExtras().getString(EXTRA_MESSAGE);
            showMessage(msg);
        }
    };
    
    /**
     * show the recvd msg
     * @param  must be final var so var converted to constant for pseudo java closure.
     */
    private void showMessage(final String msg) {   
        runOnUiThread(new Runnable() {    // new Runnable/Callable equals create a new function. 
            @Override public void run() {   
                GCMLog.d(TAG, "showMessage : " + msg);
                if( mRegFrag != null ){        
                    mRegFrag.showRegistrationId(msg);
                }
            }
        });
    }
}