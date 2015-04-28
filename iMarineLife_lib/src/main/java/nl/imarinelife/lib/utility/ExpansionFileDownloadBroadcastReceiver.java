package nl.imarinelife.lib.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class ExpansionFileDownloadBroadcastReceiver extends BroadcastReceiver{

	private static final String TAG = "ExpFileDwnldBrdcstRcver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG,"onReceive");
		try {
			ExpansionFileAccessHelper.startDownloadServiceIfRequired(context, intent,
				ExpansionFileAccessHelper.class);

			boolean result = ExpansionFileAccessHelper.expansionFilesDeliveredCorrectly();
			Log.d(TAG,"onReceive succesfully retrieved latest expansionFile["+result+"]");		
        } catch (NameNotFoundException e) {
        	Log.d(TAG,"onReceive failed retrieveing latest expansionFile",e);
            e.printStackTrace();
        }
	}

}
