package pro.got4.servertelemetry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class Splash extends FragmentActivity {

	final String RETAINED_FRAGMENT_TAG = "retained_rragment_tag";

	private RetainedFragment mRetainedFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setTheme(android.R.style.Theme_Wallpaper);
		setContentView(R.layout.splash);

		FragmentManager fm = getSupportFragmentManager();

		mRetainedFragment = (RetainedFragment) fm
				.findFragmentByTag(RETAINED_FRAGMENT_TAG);

		if (mRetainedFragment == null) {
			mRetainedFragment = new RetainedFragment();
			fm.beginTransaction().add(mRetainedFragment, RETAINED_FRAGMENT_TAG)
					.commit();
		}
	}

	class RetainedFragment extends Fragment {

		private Task task;

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);

			setRetainInstance(true);

			task = new Task();
			task.execute();
		}

		class Task extends AsyncTask<Void, Void, Void> {

			@Override
			protected Void doInBackground(Void... params) {

				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(getActivity());
				boolean firstStart = sp.getBoolean(Main.FIRST_START_FIELDNAME,
						true);
				if (firstStart) {

					// Если это первый запуск приложения, то загружаем
					// демо-данные.
					Main.dbAdapter = new DatabaseAdapter(getActivity());
					Main.dbAdapter.open();

					DatabaseAdapter.fillDBWithDemoData(getActivity());
					sp.edit().putBoolean(Main.FIRST_START_FIELDNAME, false)
							.commit();
					Main.dbAdapter.close();
					Main.dbAdapter = null;
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {

				super.onPostExecute(result);

				Intent intent = new Intent(getActivity(), Main.class);
				startActivity(intent);
				finish();
			}
		}
	}
}
