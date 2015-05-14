package nl.imarinelife.lib.divinglog.sightings;

import java.util.ArrayList;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.divinglog.db.dive.Dive;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public abstract class SightingsSender {
	
	private class ReturnValue{
		int diveNr;
		boolean done;
		public ReturnValue(int diveNr, boolean done) {
			super();
			this.diveNr = diveNr;
			this.done = done;
		}
		
		
	}

	public static final String	TAG	= "SightingsSender";
	protected static ArrayList<Integer>  beingSentNow = new ArrayList<Integer>();

	public static boolean isNowBeingSent(int diveNr){
		return (beingSentNow.contains(diveNr));
	}
	
	public void sendSightingsAsynchronously(Dive dive, final Context context) {
		beingSentNow.add(dive.getDiveNr());
		AsyncTask<Dive, Integer, ReturnValue> task = new AsyncTask<Dive, Integer, ReturnValue>() {

			@Override
			protected ReturnValue doInBackground(Dive... params) {
				boolean done = true;
				int diveNr=0;
				if (params != null && params.length > 0) {
					Dive dive = params[0];
					diveNr=dive.getDiveNr();
					try {
						done = sendSightings(dive, context);
						if (done) {
							dive.setSentAlready(true);
							dive.save();
							Dive currentDive = (MainActivity.me != null && MainActivity.me.currentDive != null)
									? MainActivity.me.currentDive
									: dive;
							if (dive.getDiveNr() == currentDive.getDiveNr() && dive != currentDive) {
								// a race condition ís possible here, but not very likely
								MainActivity.me.currentDive.setSentAlready(true);
								MainActivity.me.currentDive.setChanged(true);
							}
						}
					} catch (Exception e) {
						Log.e(TAG,
							"sending Sighting failed",
							e);
						done = false;
					}
				}
				ReturnValue result = new ReturnValue(diveNr, done);
				return result;
			}
			
			@Override
			protected void onPostExecute(ReturnValue result) {
				beingSentNow.remove(Integer.valueOf(result.diveNr));
				
				String text=null;
				if(result.done){
					text = (LibApp.getCurrentResources()
							.getString(R.string.send_succesful));
				}else{
					text = (LibApp.getCurrentResources()
							.getString(R.string.send_failed));
				}
				Toast.makeText(MainActivity.me, text, Toast.LENGTH_LONG).show();
				
			}

		};

		task.execute(dive);
	}

	protected abstract boolean sendSightings(Dive dive, Context context) throws Exception;

}
