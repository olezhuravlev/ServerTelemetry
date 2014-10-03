package pro.got4.servertelemetry;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class AppPrefsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Log.d(Main.TAG, "AppPrefsActivity.onCreate()");

		super.onCreate(savedInstanceState);

		// ВНИМАНИЕ!
		// Идентификатор элемента ListPreference, расположенного в файле
		// R.xml.app_prefs, должен совпадать с идентификатором, заданным в поле
		// Main.CURRENT_THEME_ID_FIELDNAME!
		addPreferencesFromResource(R.xml.app_prefs);
	}
}
