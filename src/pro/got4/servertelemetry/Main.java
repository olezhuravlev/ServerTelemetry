package pro.got4.servertelemetry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Main extends FragmentActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	public static final String TAG = "myLogs";

	static final int LOADER_ID = 1;

	static final int PAGE_PREFS_INDEX = 0;
	static final int PAGE_GRAPH_INDEX = 1;
	static final int PAGE_TABLE_INDEX = 2;
	static final int PREFERENCE_MENU_ITEM_ID = 0;

	// ��������� ������������� ������� ������������ ����.
	String currentTheme;
	int storedRecordsMaxAmount;

	static final int PAGE_COUNT = 3;

	static final String ARGUMENT_PAGE_NUMBER_FIELDNAME = "arg_page_number";
	static final String SOURCE_SERVER_PATH_FIELDNAME = "source_server_path";
	static final String DATE_BEGIN_FIELDNAME = "date_begin";
	static final String DATE_END_FIELDNAME = "date_end";
	static final String DATE_PATTERN_FIELDNAME = "date_pattern";
	static final String CURRENT_THEME_ID_FIELDNAME = "currentTheme_id";
	static final String BUNDLE_FIELDNAME = "bundle";
	static final String RESTART_FLAG_FIELDNAME = "restart_flag";
	static final String STORED_RECORDS_MAX_AMOUNT = "stored_records_max_amount";

	private ViewPager pager;

	public static LoaderManager loaderManager;

	// public static TreeMap<Date, Float> data;
	public static ArrayList<Date[]> graph_axis_x;
	public static ArrayList<double[]> graph_axis_y;

	// ������, �������� ����� ��������.
	public String sourceServerPathString;
	public String dateBeginString;
	public String dateEndString;

	public static LineGraph lineGraph;

	private MyFragmentPagerAdapter adapter;

	// ��������, ���������� ��� �������.
	public static View graphFragmentView;

	// ���, ���������� ������.
	public static GraphicalView graphicalView = null;

	/**
	 * ���� ������������� �������� ������.
	 */
	public static boolean updateGraph;

	/**
	 * ���� ������������� �������� �������.
	 */
	public static boolean updateTable;

	public static Activity mainActivity = null;

	/**
	 * ������ �� ������� ��.
	 * 
	 */
	public static DatabaseAdapter dbAdapter;

	// ������� ��� ������.
	public static SimpleCursorAdapter scAdapter;

	// ������ ������ ������������ ������.
	public static Cursor cursorCurrentData;

	// ������ ������ ������������ ������ � �������� ���������� (������������ ���
	// ����������� ������).
	public static Cursor cursorCurrentDataBackward;

	public Main() {
		mainActivity = this;
	}

	// ���������, ������� ������ �������������� ���������� ��� ��� ����������.
	public interface FragmentUpdateListener {
		public void update(Bundle bundle);
	}

	// ///////////////////////
	// ///////////////////////
	// ��������� ����

	// ��� ��� Bundle, ������� �������� � onSaveInstanceState.
	public void onCreate(Bundle savedInstanceState) {

		Log.d(Main.TAG,
				"Main.onCreate(): �������������� ����������� ��������, ��������� ����, ��������� ���, ���������� �������� ��� �����������.");

		super.onCreate(savedInstanceState);

		// ��������� ������, ����������� ��� ���������� ������� ����������.
		// �������� ��������� �������� �������������� ������:
		// �) ������ ������ ����������;
		// �) ���������� ���������� (��������, ��� ��������� ����� ����);
		// �) ������ ���������� ��� ����� ������������ (��������, ��� ��������
		// ������);
		// �) ������ ����������, ������� ���������� � ������� ������, ����� ����
		// ����� �������� (��� ������� ������������ �������
		// onSaveInstanceState(Bundle)), � ����� ���� �������������.

		// ��� ������� ������� ������������ ���������� ����������� ��������� �
		// ��������� �������:
		// 1) �� SharedPreferences. ��� ������, ����������� ��� ��������
		// ���������� ������������� � ������� onDestroy();
		// 2) �� extras. ��� ������, ����������� � ������� ���������� ���
		// �������������� ����������� ���������� �� ������
		// onResume();
		// 3) �� Bundle. ��� ������, ����������� ��� �������� ����������
		// �������� � ������ onSaveInstanceState(Bundle).

		// 1)
		SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);

		sourceServerPathString = sharedPrefs.getString(
				SOURCE_SERVER_PATH_FIELDNAME, "");
		dateBeginString = sharedPrefs.getString(DATE_BEGIN_FIELDNAME, "");
		dateEndString = sharedPrefs.getString(DATE_END_FIELDNAME, "");
		currentTheme = sharedPrefs.getString(CURRENT_THEME_ID_FIELDNAME, "");
		String storedRecordsMaxAmount_String = sharedPrefs.getString(
				STORED_RECORDS_MAX_AMOUNT, "");
		if (storedRecordsMaxAmount_String.isEmpty()) {
			storedRecordsMaxAmount = 100;
		} else {
			storedRecordsMaxAmount = Integer
					.valueOf(storedRecordsMaxAmount_String);
		}

		// 2)
		Bundle extras = getIntent().getExtras();
		if (extras != null) {

			Object object = extras.get(BUNDLE_FIELDNAME);
			if (object != null) {

				Bundle savedState = (Bundle) object;

				sourceServerPathString = savedState
						.getString(SOURCE_SERVER_PATH_FIELDNAME);
				dateBeginString = savedState.getString(DATE_BEGIN_FIELDNAME);
				dateEndString = savedState.getString(DATE_END_FIELDNAME);
				currentTheme = savedState.getString(CURRENT_THEME_ID_FIELDNAME);
			}
		}

		// 3)
		if (savedInstanceState != null) {

			sourceServerPathString = savedInstanceState
					.getString(SOURCE_SERVER_PATH_FIELDNAME);
			dateBeginString = savedInstanceState
					.getString(DATE_BEGIN_FIELDNAME);
			dateEndString = savedInstanceState.getString(DATE_END_FIELDNAME);
			currentTheme = savedInstanceState
					.getString(CURRENT_THEME_ID_FIELDNAME);
		}

		// ��������� ����.
		// ���� ������� ���� �� �������, �� ��������������� ���� �� ���������.
		if (currentTheme.isEmpty()) {
			String[] themes = getResources().getStringArray(
					R.array.ThemesIDList_Str);
			currentTheme = themes[0];
		}

		// ��������� ���� �� ��������. ���� �� �������, �� ��������������� ����
		// �� ���������.
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		String themeInPrefs = sp.getString("currentTheme", "");
		if (themeInPrefs.isEmpty()) {

			themeInPrefs = currentTheme;

			SharedPreferences.Editor editor = sp.edit();
			editor.putString("currentTheme", currentTheme);

			editor.apply();

		} else {

			// ���� � ���������� ���� �������, �� ��� ��������������� � ��������
			// �������.
			currentTheme = themeInPrefs;
		}

		// ��������� ����.
		int themeID = getThemeID(currentTheme);
		setTheme(themeID);

		// ��������� ���.
		setContentView(R.layout.main);

		// ���������� ����������� ��������.
		pager = (ViewPager) findViewById(R.id.pager);

		adapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);

		// // ��������� ��������� ������� ����������� - ���� ��������������.
		// pager.setOnPageChangeListener(new OnPageChangeListener() {
		//
		// @Override
		// public void onPageSelected(int position) {
		// }
		//
		// @Override
		// public void onPageScrolled(int position, float positionOffset,
		// int positionOffsetPixels) {
		// }
		//
		// @Override
		// public void onPageScrollStateChanged(int state) {
		// }
		// });

	}

	// @Override
	// public void onRestart() {
	//
	// Log.d(Main.TAG, "Main.onRestart()");
	//
	// super.onRestart();
	// }

	@Override
	public void onStart() {

		Log.d(Main.TAG,
				"Main.onStart(): ������������� ��������� ��������, �������� � �������� �������� ��.");

		super.onStart();

		// ������������� �������� ��, ������������ ���������� � ��������� ������
		// �� ��.
		// ���������������� ��� ���������� �����, �.�. ����� ������������
		// ������������ �� ���� �������� � ����������, �� onCreate() ��
		// ����������.
		// ������� ����� ���������������� ������, �.�. �� ������������ �
		// ��������� onLoadFinished, ����������� ����� ������
		// Main.loaderManager.initLoader.
		Main.dbAdapter = new DatabaseAdapter(this);
		Main.dbAdapter.open(); // TODO: �� �������� �� UI!

		// ������������� ����������.
		Main.loaderManager = getSupportLoaderManager();
		Main.loaderManager.initLoader(LOADER_ID, null, this);

	}

	@Override
	public void onResume() {

		Log.d(Main.TAG, "Main.onResume(): ����������, ���� ���������� ����.");

		super.onResume();

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		String themeInPrefs = sp.getString("currentTheme", "");

		// ���� ����, ���������� �� �������� �� ��������� � ������� �����,
		// ������ ����� ���������� ���� ������� � ������� � ����� �������� �
		// ������� ���� ����� ��������.
		// � �.�. ������� ���� �������� �������� ������ ����� ���������� �����
		// ��� ��������� (����� ������� setContentView(View) ��� inflate(int,
		// ViewGroup)), �� ���������� �������������, ��������� ����
		// ������������ � ������� onCreate.
		// ����� ������ finish() ���������� ������� ������ ������������ �
		// ������� onDestroy().
		if (themeInPrefs.isEmpty()) {

			themeInPrefs = currentTheme;

			SharedPreferences.Editor editor = sp.edit();
			editor.putString("currentTheme", currentTheme);
			editor.apply();
		}

		if (!themeInPrefs.equals(currentTheme)) {

			// ��������� ������������.
			Toast.makeText(this,
					"��������� ���� ���������� \"" + themeInPrefs + "\"",
					Toast.LENGTH_SHORT).show();

			finish();

			// ���������� ������, ������� ����� ������������� � onCreate ���
			// ����������� ����������.
			// ���������� �����, ����� ������������ ����.
			// ��� ������ finish() ������� onSaveInstanceState(Bundle) ��
			// ���������� (�.�. � Bundle �������� ��������� �� ���������), �
			// ������� onDestroy() ���������� ����� (�.�. � SharedPrefs
			// ���������� ���������� �����)!
			Bundle savedInstanceState = new Bundle();
			savedInstanceState.putString(SOURCE_SERVER_PATH_FIELDNAME,
					sourceServerPathString);
			savedInstanceState.putString(DATE_BEGIN_FIELDNAME, dateBeginString);
			savedInstanceState.putString(DATE_END_FIELDNAME, dateEndString);
			savedInstanceState.putString(CURRENT_THEME_ID_FIELDNAME,
					currentTheme);

			Intent intent = getIntent();
			intent.putExtra(BUNDLE_FIELDNAME, savedInstanceState);

			startActivity(intent);
		}
	}

	@Override
	public void onPause() {

		Log.d(Main.TAG,
				"Main.onPause(): ���������� ��������, �������� �������� ��.");

		super.onPause();

		// ���������� ������ ������� ������ �����, � �� � onDestroy(), ���������
		// onDestroy() �.�. � �� ������, ���� ������� ����� �������. �����
		// onPause() ����� ������ � ����� ������, �.�. ������ ����� ���� �������
		// �������� ����������� ����� �������.
		savePreferences();

		Log.d(Main.TAG, "Main.onPause(): �������� �������� ��!!!");

		dbAdapter.close();
		dbAdapter = null;

	}

	// @Override
	// public void onStop() {
	//
	// Log.d(Main.TAG, "Main.onStop()");
	//
	// super.onStop();
	// }
	//
	// @Override
	// public void onDestroy() {
	//
	// Log.d(Main.TAG, "Main.onDestroy()");
	//
	// super.onDestroy();
	// }

	// ����� ���������� �����
	// ///////////////////////
	// ///////////////////////

	/**
	 * �������, ���������� �� �������������� ���������� ViewPager-y.
	 * 
	 * @author programmer
	 * 
	 */
	private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			Bundle bundle = new Bundle();
			bundle.putInt(ARGUMENT_PAGE_NUMBER_FIELDNAME, position);

			// ��������� ���������� ��������� ����� ������ ������������
			// ������.
			Fragment pageFragment = null;
			if (position == Main.PAGE_PREFS_INDEX) {
				pageFragment = new Prefs();
			} else {
				pageFragment = PageFragment.newInstance(bundle);
			}

			return pageFragment;

		}

		@Override
		public int getCount() {
			return PAGE_COUNT;
		}

		@Override
		public CharSequence getPageTitle(int position) {

			Resources resources = getResources();

			String title = "";
			switch (position) {
			case Main.PAGE_PREFS_INDEX: {
				title = resources.getString(R.string.prefs);
				break;
			}
			case Main.PAGE_GRAPH_INDEX: {
				title = resources.getString(R.string.graph);
				break;
			}
			case Main.PAGE_TABLE_INDEX: {
				title = resources.getString(R.string.table);
				break;
			}
			}

			return title;
		}

		// ������� ���������� ����� ������ ������ ��������
		// notifyDataSetChanged()
		// ������ object - ��� ����������� ��������.
		//
		// ���������� ����� ������������ ��� �������� ����������, ��� �������
		// �������� ���� ��������.
		//
		// ���������� �� ��������� ������������ ��� �������� ������� �� ������
		// ������� � ������ ���������� POSITION_UNCHANGED.
		//
		// ���������:
		// Object - ������, �������������� �������, �������������� ������������
		// ������� instantiateItem(View, int).
		//
		// ����������:
		// ������ ����� ������� �������� �� 0 �� getCount() - 1;
		// POSITION_UNCHANGED - ���� ������� ������� �� ��������;
		// POSITION_NONE - ���� ������� ������ �� ����������� � ��������.
		@Override
		public int getItemPosition(Object object) {

			if (object instanceof PageFragment) {

				// �������� bundle ���������, ��� �������� ��� �������.
				PageFragment pageFragment = (PageFragment) object;
				Bundle bundle = pageFragment.getArguments();
				int position = bundle
						.getInt(Main.ARGUMENT_PAGE_NUMBER_FIELDNAME);

				if (position == Main.PAGE_PREFS_INDEX) {

					// ������, �.�. ���������� ������������ �������������
					// ���������.
					pageFragment.update(null);

				} else if (position == Main.PAGE_GRAPH_INDEX && updateGraph) {

					// ����������� POSITION_NONE �������� � ������������
					// ���������.
					return PagerAdapter.POSITION_NONE;

				} else if (position == Main.PAGE_TABLE_INDEX && updateTable) {

					// ����������� POSITION_NONE �������� � ������������
					// ���������.
					return PagerAdapter.POSITION_NONE;
				}
			}

			return super.getItemPosition(object); // �� ���� ����������
													// POSITION_UNCHANGED.
		}
	}

	// ����� �� ��������� �������� ������ � ���������� (��������, �����
	// initLoader()), �� ���������, ���������� �� ��������� � ���������
	// ���������������. ���� �������� ���, �� ���������� ����� onCreateLoader()
	// ���������� LoaderManager.LoaderCallbacks. ��� �����, ��� �� ��������
	// ����� ���������. ������ ��� ����� CursorLoader, �� �� ������ �����������
	// ���� ����������� �������� ����������.
	// � ���� ������� ����� ������ onCreateLoader() ������� CursorLoader. ��
	// ������� ������� CursorLoader ��������� ��� �����������, ������� �������
	// ������ ����� ����������, ����������� ��� ���������� ������� �
	// ContentProvider. � ���������, ��� ����������:
	// uri - URI ��� ��������� �����������;
	// projection - ������ �������, ������� ����� �������. �������� null ������
	// ��� �������, �� ��� ������������;
	// selection - ������, ������������, ����� ������ ����������,
	// ��������������� ��� ����������� SQL WHERE (�������� ��� WHERE). ��������
	// null ������ ��� ������ ��� ���������� URI;
	// selectionArgs - �� ������ �������� ����� ������� � �������, ������� �����
	// �������� ���������� �� selectionArgs � ��� �������, � ������� ���
	// ���������� � �������. ��� �������� ����������� �������� ��������;
	// sortOrder - ��������, ��� ������������� ������, ��������������� ���
	// ����������� SQL ORDER (�������� ��� ORDER). �������� null �������� �
	// ������������� ���������� �� ���������, ��� ������� ������ �.�.
	// ��������������.
	@SuppressLint("SimpleDateFormat")
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

		Log.d(Main.TAG,
				"Main.onCreateLoader(): �������� ���������� ������������ ����������.");

		// �����-�������, ������ ������������ ��������� ������ ����������.
		// ����� ���������� LoaderManager-�� ��� ������ �������� ����������.
		DataLoader dataLoader = new DataLoader(this);
		if (bundle != null) {

			// ������ URI-������ �� ������ ���������� ����������. ������:
			// "http://www.got4.esy.es/?dateBegin=20140101120000&dateEnd=20140101120003";
			// "http://192.168.54.201/test3.php";
			String uri = bundle.getString(SOURCE_SERVER_PATH_FIELDNAME);
			String dateBeginString = bundle.getString(DATE_BEGIN_FIELDNAME);
			String dateEndString = bundle.getString(DATE_END_FIELDNAME);
			String dateFormatPatternString = bundle
					.getString(DATE_PATTERN_FIELDNAME);

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					dateFormatPatternString);

			String dateBeginURIFormattedString = getURIFormattedString(
					dateBeginString, simpleDateFormat);

			String dateEndURIFormattedString = getURIFormattedString(
					dateEndString, simpleDateFormat);

			// ���� ������ ����� ������� �����, � ���� ��������� - "23:59:59".
			dateBeginURIFormattedString = dateBeginURIFormattedString
					.concat("000000");
			dateEndURIFormattedString = dateEndURIFormattedString
					.concat("235959");

			dataLoader.setURIString(uri.concat("/?dateBegin=")
					.concat(dateBeginURIFormattedString).concat("&dateEnd=")
					.concat(dateEndURIFormattedString));
		}

		return dataLoader;
	}

	// ����� ���������� ������������� ����� Loader �������� ��������. ������
	// ���� ����� ������������ ��� ���������� UI ���������� � ��������������
	// ����������� ������. ������ ����� (� ����� �����) ������������, ���
	// ����� ������ ����� ���������� � ���� ����� ������ ���, ����� ��������
	// ����� ������. �������, ��� ��� ������ Loader-� ����������� ��������
	// ������ � ��������� ����� ����������� ��������. LoaderManager �������
	// ���������� �������� ��� ������ ��� ����� ��������� � ����� ��������
	// ��������� � ����� onLoadFinished ������� ��������� ������ ��� ���
	// ������������� ��������.

	// ����������, ����� �������������� ��������� ��������� �������� ����
	// ��������. ��������, ��� ������ ���������� �� ��������� ���������
	// ���������� ���������� �� ����� ������ ����� �������, ������� ������
	// ��������� ����� ���������� ��������� ����������.

	// ��� ������� �������������� ���������� ����� ������������� ���������
	// ������, ��������������� ����������. � ���� ������ ��� ������� �������
	// ��� ������������� ������ ������ (�.�. ��� ����� ����� �����������),
	// �� �� ������� ��������� ����������� ������������ ���� ������, �.�.
	// ��� ������� ��������� � ��� ����������� �� ����. ���������
	// ����������� �� ���������� ������ �������, ������� ��� �� ����� �����
	// ������.

	// � ���������:
	// - ��������� ����� ����������� ��������� ������ � �������� �� ���
	// ����� ����� ������ ����� �������. ��� �� ������� ����������� ������
	// ��������������. ��������, ���� ����� ������� �������� Cursor � ��
	// ���������� ��� � CursorAdapter, ����������� �����������
	// CursorAdapter-� �� ��������� �� FLAG_AUTO_REQUERY, ��
	// FLAG_REGISTER_CONTENT_OBSERVER (�.�. ����������� 0 ��� ���������
	// ������). ��� ������������ CursorAdapter �� ���������� ���
	// ������������ ���������� �� Cursor, � ������� ��� ������������� ������
	// ��� ����� ��������� ���������� �� �������� ����� Cursor ��� ���������
	// ������ ����� �������;
	// - ��������� ��������� ������ ��� ������ ������, ��� ���������� ������
	// �� �� ����������. ��������, ���� ������ �������� Cursor ��
	// CursorLoader, �� ��� �� ������� �������� ����� close()
	// ��������������. ���� Cursor �������� � CursorAdapter, ��� �������
	// ������������ ����� swapCursor() ��� ����, ����� ������ Cursor ��
	// ����������.

	// Parameters
	// loader The Loader that has finished.
	// cursorOfReceivedData The data generated by the Loader.
	@Override
	public void onLoadFinished(Loader<Cursor> loader,
			Cursor cursorOfReceivedData) {

		Log.d(Main.TAG,
				"Main.onLoadFinished(): ��������� ������ �� �������, ��������� � ���������� �������, ����������� FragmentPagerAdapter.");

		// ����������� �������� �����������, ��� ������ ������ ���������.
		// ����� �������� �� �������� �������, ����� �� �����������!
		// TODO: ��� ����� ���������� ������ ���� ����� ����������, ��
		// ������-�� ��� �� �������� � ������������ ������ getItemPosition!
		adapter.notifyDataSetChanged();

		if (cursorOfReceivedData == null) {
			cursorOfReceivedData = Main.dbAdapter
					.fetchAllNotes(DatabaseAdapter.DATE_FIELD_NAME + " ASC");
		}

		int rowsInDatabase = 0;
		if (cursorOfReceivedData != null) {
			rowsInDatabase = cursorOfReceivedData.getCount();
			;
		}

		Main.cursorCurrentData = cursorOfReceivedData;

		// ���� ������ �������, �� ����� �������� � ������ ��� ��������� ������.
		if (Main.cursorCurrentData != null) {

			Log.d(Main.TAG,
					"DataLoader.onLoadFinished(): �������� ��������� ������� �� ��� ������!");
			Main.cursorCurrentDataBackward = Main.dbAdapter
					.fetchAllNotes(DatabaseAdapter.DATE_FIELD_NAME + " DESC");
			;

			// startManagingCursor(Main.cursorCurrentData);
			// startManagingCursor(Main.cursorCurrentDataBackward);
		}

		// ��������� �������.
		String[] series = new String[] { getResources().getString(
				R.string.graphValuesName1) };

		Date[] x_values = new Date[rowsInDatabase];
		double[] y_values = new double[rowsInDatabase];

		// ����������� ����������� �������, �������������� �������� � �������.
		// ��� ������ �������� � ������ ���������� AChartEngine �������,
		// ����� ���� ���� ����������� ������ �� �����������!
		if (Main.cursorCurrentData != null) {

			cursorCurrentData.moveToFirst();
			if (cursorCurrentData.isFirst()) {

				int dateColumnIndex = cursorCurrentData
						.getColumnIndexOrThrow(DatabaseAdapter.DATE_FIELD_NAME);
				int valueColumnIndex = cursorCurrentData
						.getColumnIndexOrThrow(DatabaseAdapter.VALUE_FIELD_NAME);

				int i = 0;
				do {

					long dateTimeStamp = cursorCurrentData
							.getLong(dateColumnIndex);
					Float value = cursorCurrentData.getFloat(valueColumnIndex);

					Date dateOfValue = new Date();
					dateOfValue.setTime(dateTimeStamp);

					x_values[i] = dateOfValue;
					y_values[i] = value;

					++i;

				} while (cursorCurrentData.moveToNext());

			}

			graph_axis_x = new ArrayList<Date[]>();
			graph_axis_x.add(x_values);

			graph_axis_y = new ArrayList<double[]>();
			graph_axis_y.add(y_values);

			Main.lineGraph = new LineGraph();
			Main.lineGraph.adjust(this, series, graph_axis_x, graph_axis_y);

			// ����������� �������� ������� (����������) �� ���������� (��������
			// ��������� ���� ��������� ��� �� �������� � onCreateView()).
			// ����� ������ ����� ������ ��������� �������
			// getItemPosition(Object
			// object) � ��������� �������� ����� ������� ����������� ��������.

			// http://stackoverflow.com/questions/7263291/viewpager-pageradapter-not-updating-the-view/8024557#8024557
			// The notifyDataSetChanged() method on the PagerAdapter will only
			// notify the ViewPager that the underlying pages have changed. For
			// example, if you have created/deleted pages dynamically (adding or
			// removing items from your list) the ViewPager should take care of
			// that. In this case I think that the ViewPager determines if a new
			// view should be deleted or instantiated using the
			// getItemPosition()
			// and getCount() methods.
			//
			// I think that ViewPager, after a notifyDataSetChanged() call takes
			// it's child views and checks their position with the
			// getItemPosition(). If for a child view this method returns
			// POSITION_NONE, the ViewPager understands that the view has been
			// deleted, calling the destroyItem(), and removing this view.
			// In this way, overriding getItemPosition() to always return
			// POSITION_NONE is completely wrong if you only want to update the
			// content of the pages, because the previously created views will
			// be
			// destroyed and new ones will be created every time you call
			// notifyDatasetChanged(). It may seem to be not so wrong just for a
			// few
			// TextViews, but when you have complex views, like ListViews
			// populated
			// from a database, this can be a real problem and a waste of
			// resources.

			updateGraph = true;
			updateTable = true;

			// ���� ����� �.�. ������ ����������� ���� ������, ������������
			// ���������, ���� �������� � ��������������� ��� �.�. ���������.
			// ����� ������ ����� ������ ����� ������� getItemPosition(Object
			// object).

			Main.graphicalView = ChartFactory.getTimeChartView(
					lineGraph.context, lineGraph.dataset, lineGraph.renderer,
					"HH:mm");

			if (graphFragmentView != null) {
				LinearLayout linearLayout = (LinearLayout) graphFragmentView
						.findViewById(R.id.graph);
				linearLayout.removeAllViews();
				linearLayout.addView(Main.graphicalView, new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			}

			lineGraph.renderer.setApplyBackgroundColor(true);
			lineGraph.renderer.setMarginsColor(Color.TRANSPARENT);

			// ��������� ������������.
			Toast.makeText(
					this,
					getResources().getString(R.string.rowsInDatabase) + " "
							+ rowsInDatabase, Toast.LENGTH_SHORT).show();
		}
	}

	// ���������� ������������ ��� ��������� ���� �������� ����� ����������
	// ��
	// ������� ���������� ������������ ����-�����. � ��������� ���������
	// ���������� ����� ���� ����������� ��� ��������� ���������
	// ���������������
	// ���������. ����� ���������� ����� ���������� ���������� ��� ������
	// �������� ��� ����� ������ ���������� 0 ��� ������ ���� ���������, ���
	// ������� ����� a.equals(b) ���������� ������. �������������, �����
	// ���������� ������������ ��������� Serializable.
	// private class DecendingComparator implements Comparator<Date>,
	// Serializable {
	//
	// // �������������, ������������ ��� ������������.
	// private static final long serialVersionUID = 1703654321884837005L;
	//
	// @SuppressWarnings("unused")
	// // private HashMap<Date, Float> mapToSort;
	// private Cursor mapToSort;
	//
	// // public DecendingComparator(HashMap<Date, Float> mapToSort) {
	// public DecendingComparator(Cursor mapToSort) {
	// this.mapToSort = mapToSort;
	// }
	//
	// @Override
	// public int compare(Date lhs, Date rhs) {
	//
	// if (lhs.before(rhs)) {
	// return 1;
	// } else if (lhs.after(rhs)) {
	// return -1;
	// } else {
	// return 0;
	// }
	// }
	// }

	// ������� ������ ��������� ����������� LoaderManager.LoaderCallbacks.
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

		// Log.d(Main.TAG, "Main.onLoaderReset()");

		// ����������, ����� �������������� ��������� ��������� ������������ �
		// ������� ��� ������ ���������� ����������. ���������� � ���� �����
		// ������ ������� ����� ��������� ������ �� ������ ����������.
	}

	// ����������, ����� �������� ��������� ���������� �� ���������� �����
	// ������������ ����� �������, ����� ��� ��������� ����� ���� �������������
	// � onCreate(Bundle) ��� � onRestoreInstanceState(Bundle) (Bundle,
	// �������������� � ���� ������ ����� �������� � ��� ������).
	// ����� ���������� �� ����, ��� ���������� ����� ���������� � �����
	// ��� ����� �������� ����� ��������� �����, ���������� ������ ������������
	// ��� ���������. ��������, ���� ���������� � �������� �� ���������� �, � �
	// �����-�� ������ ������� ���������� � ���������� ���� �������������
	// ��������, ���������� � ����� ����� ����������� ��������� �������
	// ��������� ������ ����������������� ���������� ����� ���� �����. �������
	// ����� ������������ �������� � ���������� �, ��������� �����������������
	// ���������� �.�. ������������� ����� onCreate(Bundle) ���
	// onRestoreInstanceState(Bundle).
	// �� ������� ���� ����� � �������� ������� ���������� ����� ����������,
	// ������, ��� ����� onPause(), ������� ������ ���������� ����� ����������
	// ������ �� ������ ��� ��� ������������, ��� ����� onStop(), �������
	// ���������� ����� ������������.
	// ���� ������, ����� ���������� onPause() � onStop(), �� ��
	// ���������� onSaveInstanceState(Bundle) - ��� ����� ������������
	// ������������ �� ���������� � � ���������� �: ��� ������������� ��������
	// onSaveInstanceState(Bundle) � ���������� � �.�. ������ ��������� �������
	// �� ����� ������������, ������� ������� �������� ����� ������.
	// ��� ������ ����� ����� ������ onPause(), �� �� ����� ������
	// onSaveInstanceState(Bundle) - ��� ����� ���������� � �������� �� ��������
	// ����� ����� ����������� �: ������� ����� �������� ������
	// onSaveInstanceState(Bundle) � ���������� �, ���� ��� �� ������������ �
	// ������� ���������� ����� ���������� � � ���� ������� ��� ���������
	// ���������� ������������ ���������� � �������� ����������.
	//
	// !!!
	// ���������� ��-��������� ��������� � ����������� ���������
	// ����������������� ���������� ����� ������ onSaveInstanceState() ���
	// ������� ��� � ��������, �������� ������������� � ����� ����������
	// �������������� ���, �������� ������� ����� (��� �� ������� �������������
	// ����������� ��-��������� ������ onRestoreInstanceState(Bundle)). ���� ��
	// ����������� ���� ����� ����� ��������� �������������� ����������, ��
	// ���������� ������ �������������� ���, �� ������ ����� �������� �������
	// ���������� ��-���������, � ��������� ������ ������ ������ ����
	// ��������� ��� ��������� ������� ���.
	//
	// � ������ ������ ���� ����� ����������� ����� onStop(). ��� ��������,
	// ���������� �� �� ��� ����� onPause().
	// ���������:
	// outState - Bundle � ������� ����� ��������� ����������� ���������.
	@Override
	public void onSaveInstanceState(Bundle outState) {

		// ���� �� ��������, �� �� �����������:
		// - ������� �������� ViewPager;
		// - ������� ������ ������.
		super.onSaveInstanceState(outState);

		// ���������� ����� ������ Main, ������� �.�. ������������� � onCreate.
		outState.putString(SOURCE_SERVER_PATH_FIELDNAME, sourceServerPathString);
		outState.putString(DATE_BEGIN_FIELDNAME, dateBeginString);
		outState.putString(DATE_END_FIELDNAME, dateEndString);
		outState.putString(CURRENT_THEME_ID_FIELDNAME, currentTheme);
	}

	// ���������� ������ ���� ��� ��� ������ �������� ����.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuItem item = menu.add(Menu.NONE, PREFERENCE_MENU_ITEM_ID, Menu.NONE,
				R.string.PreferenceMenuTitle);

		item.setIntent(new Intent(this, AppPrefsActivity.class));

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * ���������� ����, ����������������� ��� ������������� � URI.<br>
	 * ��������, �� "01.02.2014" ����� ���������� "20140201".
	 * 
	 * @param dateString
	 * @param simpleDateFormat
	 * @return
	 */

	String getURIFormattedString(String dateString,
			SimpleDateFormat simpleDateFormat) {

		String yearString;
		String monthOfYearString;
		String dayOfMonthString;
		Date date;
		try {
			date = simpleDateFormat.parse(dateString);
			int year = date.getYear() + 1900;
			int monthOfYear = date.getMonth() + 1;
			int dayOfMonth = date.getDate();

			yearString = Integer.toString(year);

			monthOfYearString = Integer.toString(monthOfYear);
			if (monthOfYearString.length() < 2) {
				monthOfYearString = "0" + monthOfYearString;
			}

			dayOfMonthString = Integer.toString(dayOfMonth);
			if (dayOfMonthString.length() < 2) {
				dayOfMonthString = "0" + dayOfMonthString;
			}

		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}

		return yearString.concat(monthOfYearString).concat(dayOfMonthString);
	}

	/**
	 * ���������� ��������� ��� �������������� ��� ����������� �������
	 * ����������.
	 */
	private void savePreferences() {

		SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);

		Editor editor = sharedPrefs.edit();

		editor.putString(SOURCE_SERVER_PATH_FIELDNAME, sourceServerPathString);
		editor.putString(DATE_BEGIN_FIELDNAME, dateBeginString);
		editor.putString(DATE_END_FIELDNAME, dateEndString);
		editor.putString(CURRENT_THEME_ID_FIELDNAME, currentTheme);

		editor.apply();
	}

	// ��������� ������������ �������������� ���� �� ������ � ��������
	// ��������������.
	private int getThemeID(String themeIDString) {

		int themeID = android.R.style.Theme_Light;

		if (themeIDString.equals("Light")) {
			themeID = android.R.style.Theme_Light;
		} else if (themeIDString.equals("Black")) {
			themeID = android.R.style.Theme_Black;
		} else if (themeIDString.equals("Translucent")) {
			themeID = android.R.style.Theme_Wallpaper;
		}

		return themeID;
	}
}
