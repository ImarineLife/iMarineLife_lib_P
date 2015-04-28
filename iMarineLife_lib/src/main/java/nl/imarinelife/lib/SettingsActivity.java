package nl.imarinelife.lib;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

@SuppressLint("NewApi")
public class SettingsActivity extends PreferenceActivity {

	private final static String	TAG	= "SettingsAcitivity";
	public static SettingsActivity me = null;

	public SettingsActivity() {
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.iMarineLifePreferenceTheme);
		super.onCreate(savedInstanceState);
		me=this;
		Log.d(TAG,
			"onCreate current personalDefaultChoice["+Preferences.getPersonalDefaultValue()+"]");
		if (android.os.Build.VERSION.SDK_INT < 11) {
			Log.d(TAG,
				"onCreate<11");
			addPreferencesFromResource(R.xml.pref_general);
		} else {
			getFragmentManager().beginTransaction().replace(android.R.id.content,
				new SettingsFragment()).commit();
		}
	}

	public static class SettingsFragment extends PreferenceFragment { 

		private String	TAG	= "SettingsFragment";

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Log.d(TAG,
				"onCreate>=11");
			addPreferencesFromResource(R.xml.pref_general);			
		}
	}
}