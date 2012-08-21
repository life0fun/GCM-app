package com.colorcloud.gcm;

import android.os.Parcel;
import android.os.Parcelable;

import com.colorcloud.gcm.GcmApp.GCMLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.colorcloud.gcm.Constants.*;


public class GcmMessage implements Parcelable {
	private final static String TAG = "PTP_MSG";

	public String mSender;
	public String mBody;
	public String mTime;
	public String mResponse;
	public static final String mDel = "^&^";
	
	// for easy mock
	public GcmMessage() { }
	
	public GcmMessage(String sender, String time, String body, String response ){
		mTime = time;
		if( time == null ){
			Date now = new Date();
			//SimpleDateFormat timingFormat = new SimpleDateFormat("mm/dd hh:mm");
			//mTime = new SimpleDateFormat("dd/MM HH:mm").format(now);
			mTime = new SimpleDateFormat("h:mm a").format(now);
		} 
		mSender = sender;
		mBody = body;
		mResponse = response;
	}
	
	public GcmMessage(Parcel in) {
        readFromParcel(in);
    }
	
	public String toString() {
		return mSender + mDel + mBody + mDel + mResponse + mDel + mTime;
	}
	
	
	public static JSONObject getAsJSONObject(GcmMessage msgrow) {
		JSONObject jsonobj = new JSONObject();
		try{
			jsonobj.put(SENDER, msgrow.mSender);
			jsonobj.put(TIME, msgrow.mTime);
			jsonobj.put(BODY, msgrow.mBody);
			jsonobj.put(RESPONSE, msgrow.mBody);
		}catch(JSONException e){
			GCMLog.e(TAG, "getAsJSONObject : " + e.toString());
		}
		return jsonobj;
	}
	
	/**
	 * convert json object to message row.
	 */
	public static GcmMessage parseMesssageRow(JSONObject jsonobj) {
		GcmMessage row = null;
		if( jsonobj != null ){
			try{
				row = new GcmMessage(jsonobj.getString(SENDER), jsonobj.getString(TIME), jsonobj.getString(BODY), jsonobj.getString(RESPONSE)); 
			}catch(JSONException e){
				GCMLog.e(TAG, "parseMessageRow: " + e.toString());
			}
		}
		return row;
	}
	
	/**
	 * convert a json string representation of messagerow into messageRow object.
	 */
	public static GcmMessage parseMessageRow(String jsonMsg){
		JSONObject jsonobj = JSONUtils.getJsonObject(jsonMsg);
		GCMLog.d(TAG, "parseMessageRow : " + jsonobj.toString());
		return parseMesssageRow(jsonobj);
	}

	public static final Parcelable.Creator<GcmMessage> CREATOR = new Parcelable.Creator<GcmMessage>() {
        public GcmMessage createFromParcel(Parcel in) {
            return new GcmMessage(in);
        }
 
        public GcmMessage[] newArray(int size) {
            return new GcmMessage[size];
        }
    };
    
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mSender);
		dest.writeString(mBody);
		dest.writeString(mTime);
	}
	
	public void readFromParcel(Parcel in) {
		mSender = in.readString();
		mBody = in.readString();
		mTime = in.readString();
    }
}
