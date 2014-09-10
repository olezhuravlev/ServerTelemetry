package pro.got4.servertelemetry;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AppPrefsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.app_prefs);
	}
}
