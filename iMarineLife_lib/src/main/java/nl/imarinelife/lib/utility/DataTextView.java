package nl.imarinelife.lib.utility;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class DataTextView extends TextView {
	
	private static String TAG  = "DataTextView";
	
	public DataTextView(Context context) {
		super(context);
		Log.d(TAG,"constructing");
	}

	public DataTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.d(TAG,"constructing");
	}

	public DataTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(TAG,"constructing");
}

	private Map<String,String> extraData;
	
	public void setData(String key,String value){
		if(extraData == null){
			extraData = new HashMap<String,String>();
		}
		extraData.put(key, value);
		Log.d(TAG,"setData ["+extraData+"]");
	}
	
	public String getData(String key){
		if(extraData == null) return null;
		Log.d(TAG,"getData ["+key+"]["+extraData+"]");
		return extraData.get(key);
	}
	
	public Map<String,String> getData(){
		Log.d(TAG,"getData ["+extraData+"]");
		return extraData;
	}
}
