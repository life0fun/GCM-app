package com.colorcloud.gcm;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.colorcloud.gcm.GcmApp.GCMLog;

/**
 * chat fragment attached to main activity.
 */
public class RegFragment extends Fragment {
	private static final String TAG = "GCM_RegFrag";
	 
	private static MainActivity mActivity = null;
	GcmApp mApp = null;
	TextView mDisplay;
	
	/**
     * Static factory to create a fragment object from tab click.
     */
    public static RegFragment newInstance(Activity activity, String msg) {
    	RegFragment f = new RegFragment();
    	mActivity = (MainActivity)activity;
    	
        Bundle args = new Bundle();
        args.putString("initMsg", msg);
        f.setArguments(args);
        Log.d(TAG, "newInstance :" + msg);
        return f;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {  // this callback invoked after newInstance done.  
        super.onCreate(savedInstanceState);
        mApp = (GcmApp)mActivity.getApplication();
        
        setRetainInstance(true);   // Tell the framework to try to keep this fragment around during a configuration change.
    }
    
    /**
     * the data you place in the Bundle here will be available in the Bundle given to onCreate(Bundle), etc.
     * only works when your activity is destroyed by android platform. If the user closed the activity, no call of this.
     * http://www.eigo.co.uk/Managing-State-in-an-Android-Activity.aspx
     */
    @Override
    public void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    }
    
    /**
     * no matter your fragment is declared in main activity layout, or dynamically added thru fragment transaction
     * You need to inflate fragment view inside this function. 
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	// inflate the fragment's res layout. 
        // No need this because you detach the fragment in tab unselect.
        //FrameLayout fragRootFrame = (FrameLayout)container.findViewById(R.id.fragrootframe);
        //fragRootFrame.removeAllViews();
        View contentView = inflater.inflate(R.layout.reg_frag, container, false);  // no care whatever container is.
        //fragRootFrame.addView(contentView);
        
        mDisplay = (TextView)contentView.findViewById(R.id.display);
        final String regId = mApp.getGcmRegId();
        showRegistrationId(regId);
        
        GCMLog.d(TAG, "onCreateView : reg fragment view created:");
        return contentView;
    }
    
    @Override 
    public void onDestroyView(){ 
    	super.onDestroyView(); 
    	Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {  // invoked after fragment view created.
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        GCMLog.d(TAG, "onActivityCreated: chat fragment displayed ");
    }
    
    /**
     * the API of on create options menu is diff in activity than in fragment
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options_menu, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.options_clear:
                mDisplay.setText(null);
                return true;
            case R.id.options_regid:
                showRegistrationId(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Displays a toast with the registration ID
     */
    public void showRegistrationId(String msg) {
        String regid = msg;
        if( regid == null ){
            regid = mApp.getString(mApp.REG_ID);
        }
        mDisplay.setText(regid);
        Toast.makeText(mActivity, regid, Toast.LENGTH_SHORT).show();
        GCMLog.d(TAG, "showRegistrationId: " + regid);
    }
}