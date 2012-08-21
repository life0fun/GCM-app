package com.colorcloud.gcm;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.colorcloud.gcm.GcmApp.GCMLog;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * chat fragment attached to main activity.
 */
public class MsgFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<GcmMessage>> {
	private static final String TAG = "GCM_MsgFrag";
	
	GcmApp mApp = null; 
	private static MainActivity mActivity = null;
	
	private ArrayList<GcmMessage> mMessageList = null;   // a list of chat msgs.
    private ArrayAdapter<GcmMessage> mAdapter= null;
    
    // private String mMyAddr;
    
	/**
     * Static factory to create a fragment object from tab click.
     */
    public static MsgFragment newInstance(Activity activity, String msg) {
    	MsgFragment f = new MsgFragment();
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
    	outState.putParcelableArrayList("MSG_LIST", mMessageList);
    	Log.d(TAG, "onSaveInstanceState. msg rows: " + mMessageList.size());
    }
    
    /**
     * no matter your fragment is declared in main activity layout, or dynamically added thru fragment transaction
     * You need to inflate fragment view inside this function. 
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	// inflate the fragment's res layout.
        View contentView = inflater.inflate(R.layout.msg_frag, container, false);  // no care whatever container is.
        
        final EditText inputEditText = (EditText)contentView.findViewById(R.id.edit_input);
        final Button sendBtn = (Button)contentView.findViewById(R.id.btn_send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// send the chat text in current line to the server
				String inputMsg = inputEditText.getText().toString();
				inputEditText.setText("");
				InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);
				GcmMessage row = mApp.mStore.getLastMessage();
				row.mResponse = inputMsg;       // set the input msg
				row.mSender = mApp.mDeviceName; // set the sender name to 'me'
				appendGcmMessage(row);
				mApp.mStore.storeMessage(row);
				GCMLog.d(TAG, "sendButton clicked: stored response.");
			}
        });
        
        String msg = getArguments().getString("initMsg");
        GCMLog.d(TAG, "onCreateView : fragment view created: msg :" + msg);
        
    	if( savedInstanceState != null ){
            mMessageList = savedInstanceState.getParcelableArrayList("MSG_LIST");
            Log.d(TAG, "onCreate : savedInstanceState: restore from saved instance : " + mMessageList.size());
        }else if( mMessageList == null ){
        	// no need to setContentView, just setListAdapter, but listview must be android:id="@android:id/list"
            mMessageList = new ArrayList<GcmMessage>();
            Log.d(TAG, "onCreate : jsonArrayToList : " + mMessageList.size() );
        }else {
        	Log.d(TAG, "onCreate : setRetainInstance good : ");
        }
        
        mAdapter = new GcmMessageAdapter(mActivity, mMessageList);
        
        setListAdapter(mAdapter);  // list fragment data adapter 
        
        GCMLog.d(TAG, "onCreate chat msg fragment: devicename : " + mApp.mDeviceName + " : " + getArguments().getString("initMsg"));
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
        
        // init load, start loading inside onCreateLoader LoaderManager.LoaderCallbacks 
        getLoaderManager().initLoader(0, null, this);

        if( mMessageList.size() > 0 ){
        	getListView().smoothScrollToPosition(mMessageList.size()-1);
        }
        
        setHasOptionsMenu(true);  // need this to enable menu option
        Log.d(TAG, "onActivityCreated: chat fragment displayed ");
    }
    
    /**
     * reset UI with the new data just loaded
     */
    public void resetUI( List<GcmMessage> data ){
        // you can create a new adapter with the new source,
        //mLocations = data;  // change data source for the adapter, wont work.
        //mAdapter = new LocationsAdapter(getActivity(), R.layout.row_loc, mLocations);
        //setListAdapter(mAdapter);

        // or deep copy the new data to old adapter.
        mMessageList.clear();
        for(GcmMessage msg : data){
            mMessageList.add(msg);
        }
        mAdapter.notifyDataSetChanged();
    }
    
    /**
     * add a chat message to the list, return the format the message as " sender_addr : msg "
     */
    public void appendGcmMessage(GcmMessage row) {
    	GCMLog.d(TAG, "appendGcmMessage: chat fragment append msg: " + row.mBody + " ; " + row.mResponse);
    	mMessageList.add(row);
    	getListView().smoothScrollToPosition(mMessageList.size()-1);
    	mAdapter.notifyDataSetChanged();  // notify the attached observer and views to refresh.
    	return;
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
                return true;
            case R.id.options_regid:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     *  Not used here!
     */
    @Deprecated
    private void jsonArrayToList(JSONArray jsonarray, List<GcmMessage> list) {
    	try{
    		for(int i=0;i<jsonarray.length();i++){
    		    GcmMessage row = GcmMessage.parseMesssageRow(jsonarray.getJSONObject(i));
    		    GCMLog.d(TAG, "jsonArrayToList: row : " + row.mBody);
    			list.add(row);
    		}
    	}catch(JSONException e){
    		GCMLog.e(TAG, "jsonArrayToList: " + e.toString());
    	}
    }
    
    /**
     * chat message adapter from list adapter.
     * Responsible for how to show data to list fragment list view.
     */
    final class GcmMessageAdapter extends ArrayAdapter<GcmMessage> {

    	public static final int VIEW_TYPE_MYMSG = 0;
    	public static final int VIEW_TYPE_INMSG = 1;
    	public static final int VIEW_TYPE_COUNT = 2;    // msg sent by me, or all incoming msgs
    	private LayoutInflater mInflater;
    	
		public GcmMessageAdapter(Context context, List<GcmMessage> objects){
			super(context, 0, objects);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
		
		@Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }
		
		@Override
        public int getItemViewType(int position) {
		    GcmMessage item = this.getItem(position);
			if ( item.mSender.equals(mApp.mDeviceName )){
				return VIEW_TYPE_MYMSG;
			}
			return VIEW_TYPE_INMSG;			
		}
		
		/**
		 * assemble each row view in the list view.
		 * http://dl.google.com/googleio/2010/android-world-of-listview-android.pdf
		 */
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;  // old view to re-use if possible. Useful for Heterogeneous list with diff item view type.
			GcmMessage item = this.getItem(position);
			boolean mymsg = false;
			
			if ( getItemViewType(position) == VIEW_TYPE_MYMSG){
				if( view == null ){
	            	view = mInflater.inflate(R.layout.gcm_row_mymsg, null);  // inflate chat row as list view row.
	            }
				mymsg = true;
				// view.setBackgroundResource(R.color.my_msg_background);
			} else {
				if( view == null ){
	            	view = mInflater.inflate(R.layout.gcm_row_inmsg, null);  // inflate chat row as list view row.
	            }
				// view.setBackgroundResource(R.color.in_msg_background);
			}
			
            TextView sender = (TextView)view.findViewById(R.id.sender);
            sender.setText(item.mSender);
            
            TextView msgRow = (TextView)view.findViewById(R.id.msg_row);
            if( mymsg ){
                msgRow.setText(item.mResponse);
            	msgRow.setBackgroundResource(R.color.my_msg_background);	
            }else{
                msgRow.setText(item.mBody);
            	msgRow.setBackgroundResource(R.color.in_msg_background);
            }
            
            TextView time = (TextView)view.findViewById(R.id.time);
            time.setText(item.mTime);
            
            Log.d(TAG, "getView : " + item.mSender + " " + item.mBody + " " + item.mTime);
            return view;
		}
    }

    /**
     * callback when loader created for the fragment.
     * start the async loader to populate the list.
     * For endless list, re-load upon onScroll event. UI with Footer View.
     */
    @Override
    public Loader<List<GcmMessage>> onCreateLoader(int id, Bundle args) {
        GCMLog.d(TAG, "onCreateLoader :  onCreateLoader new loader");
        return new MessageLoader(mApp, 0);
    }

    @Override
    public void onLoadFinished(Loader<List<GcmMessage>> loader, List<GcmMessage> data) {
        resetUI(data);
        GCMLog.d(TAG, " LoaderManager : onLoadFinished : set adapter data : ");
    }

    @Override
    public void onLoaderReset(Loader<List<GcmMessage>> arg0) { }
    
    /**
     * Async Task for loading
     */
    static class MessageLoader extends AsyncTaskLoader<List<GcmMessage>> {
        long mOffset = 0;
        GcmApp mApp = null;
        
        public MessageLoader(Context ctx, long offset) {
            super(ctx);
            mApp = (GcmApp)ctx;
            mOffset = 0;
        }

        @Override
        public void onStartLoading() {
            GCMLog.d(TAG, "onStartLoading ...");
            forceLoad();
        }
        
        @Override
        public List<GcmMessage> loadInBackground() {
            GCMLog.d(TAG, "loadInBackground ...");
            return loadMessages(mOffset);
        }
        
        /**
         * query content provider and return the result set
         */
        private List<GcmMessage> loadMessages(long offset) {
            GCMLog.d(TAG, "loadMessages ..." + offset);
            List<GcmMessage> msglist = mApp.mStore.getMessages(0, offset, 100);  // past=0, all message
            return msglist;
        }
    }
}