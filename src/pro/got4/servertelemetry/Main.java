package pro.got4.servertelemetry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Main extends FragmentActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	/**
	 * �������������� �������, ������������ ������������.
	 */
	public static final int PAGE_PREFS_INDEX = 0;
	public static final int PAGE_GRAPH_INDEX = 1;
	public static final int PAGE_TABLE_INDEX = 2;
	public static final int PREFERENCE_MENU_ITEM_ID = 0;

	/**
	 * ���������� �������, ������������ ������������.
	 */
	public static final int PAGE_COUNT = 3;

	/**
	 * ������������� ��������� ��������.
	 */
	public static final int LOADER_ID = 1;

	/**
	 * �������������� ������ � ����� ��.
	 */
	public static final String ARGUMENT_PAGE_NUMBER_FIELDNAME = "arg_page_number";
	public static final String SOURCE_SERVER_PATH_FIELDNAME = "source_server_path";
	public static final String DATE_BEGIN_FIELDNAME = "date_begin";
	public static final String DATE_END_FIELDNAME = "date_end";
	public static final String DATE_PATTERN_FIELDNAME = "date_pattern";
	public static final String CURRENT_THEME_ID_FIELDNAME = "currentTheme_id";
	public static final String BUNDLE_FIELDNAME = "bundle";
	public static final String RESTART_FLAG_FIELDNAME = "restart_flag";
	public static final String FIRST_START_FIELDNAME = "first_start";
	/**
	 * ��������� ������������� ������� ������������ ���� ����������.
	 */
	String currentTheme;

	/**
	 * ����������, ������������ ��� ����������� �������.
	 */
	private ViewPager pager;

	/**
	 * �������, ������������ ������������.
	 */
	private MyFragmentPagerAdapter adapter;

	/**
	 * ���������, ������������ �� ������ ����������.
	 */
	public String sourceServerPathString;
	public String dateBeginString;
	public String dateEndString;

	/**
	 * ������ �� ������.
	 */
	public static LineGraph lineGraph;

	/**
	 * ��������, ���������� ��� �������.
	 */
	public static View graphFragmentView;

	// ������, ������������ ��������.
	public static ArrayList<Date[]> graph_axis_x;
	public static ArrayList<double[]> graph_axis_y;

	/**
	 * ���� ������������� �������� ������.
	 */
	public static boolean updateGraph;

	/**
	 * ���� ������������� �������� �������.
	 */
	public static boolean updateTable;

	/**
	 * ������ �� ������� ��.
	 */
	public static DatabaseAdapter dbAdapter;

	/**
	 * ������� ��� ������.
	 * 
	 */
	public static SimpleCursorAdapter scAdapter;

	/**
	 * ������ ������ ������������ ������.
	 * 
	 */
	public static Cursor cursorCurrentData;

	/**
	 * ������ ������ ������������ ������ � �������� ���������� (������������ ���
	 * ����������� ������).
	 * 
	 */
	public static Cursor cursorCurrentDataBackward;

	/**
	 * ���������, ������� ������ �������������� ���������� ��� ��� ����������.
	 */
	public interface FragmentUpdateListener {
		public void update(Bundle bundle);
	}

	// ///////////////////////////
	// ��������� ���� ����������

	// ��� ��� Bundle, ������� �������� � onSaveInstanceState.
	public void onCreate(Bundle savedInstanceState) {

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
		currentTheme = sharedPrefs.getString(Main.CURRENT_THEME_ID_FIELDNAME,
				"");

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
				currentTheme = savedState
						.getString(Main.CURRENT_THEME_ID_FIELDNAME);
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
					.getString(Main.CURRENT_THEME_ID_FIELDNAME);
		}

		// ���� ������� ���� �� �������, �� �� ��������� ������������ ������
		// ���� �� ������.
		if (currentTheme.isEmpty()) {
			String[] themes = getResources().getStringArray(
					R.array.ThemesIDList_Str);
			currentTheme = themes[0];
		}

		// ��������� ���� �� ��������. ���� �� �������, �� ��������������� ����
		// �� ���������.
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		String themeInPrefs = sp.getString(Main.CURRENT_THEME_ID_FIELDNAME, "");
		if (themeInPrefs.isEmpty()) {

			themeInPrefs = currentTheme;

			SharedPreferences.Editor editor = sp.edit();
			editor.putString(Main.CURRENT_THEME_ID_FIELDNAME, currentTheme);

			editor.apply();

		} else {

			// ���� � ���������� ���� �������, �� ��� ��������������� � ��������
			// �������.
			currentTheme = themeInPrefs;
		}

		// ��������� ����. ������� ������ �� ������ ������ �����������!
		setTheme(getThemeID(currentTheme));
		
		super.onCreate(savedInstanceState);
		
		// ��������� ���.
		setContentView(R.layout.main);

		// ���������� ����������� ��������.
		pager = (ViewPager) findViewById(R.id.pager);

		adapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);

		// ��������� ������ �������.
		Main.lineGraph = new LineGraph();
	}

	@Override
	public void onStart() {

		super.onStart();

		// ������������� ����������.
		getSupportLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onResume() {

		super.onResume();

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		String themeInPrefs = sp.getString(Main.CURRENT_THEME_ID_FIELDNAME, "");

		// ���� ����, ���������� �� �������� �� ��������� � ������� �����,
		// ������ ����� ���������� ���� ������� � ������� � ����� �������� �
		// ������� ���� ����� ��������.
		// � �.�. ������� ���� �������� �������� ������ ����� ���������� �����
		// ��� ��������� (����� ������� setContentView(View) ��� inflate(int,
		// ViewGroup)), �� ���������� ����� ������������� � ��������� ����
		// ����� ����������� � ������� onCreate.
		// ����� ������ finish() ���������� ������� ������ ������������ �
		// ������� onDestroy().
		if (themeInPrefs.isEmpty()) {

			themeInPrefs = currentTheme;

			SharedPreferences.Editor editor = sp.edit();
			editor.putString(Main.CURRENT_THEME_ID_FIELDNAME, currentTheme);
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
			savedInstanceState.putString(Main.CURRENT_THEME_ID_FIELDNAME,
					currentTheme);

			Intent intent = getIntent();
			intent.putExtra(BUNDLE_FIELDNAME, savedInstanceState);

			startActivity(intent);
		}

		// ������������� �������� ��, ������������ ���������� � ��������� ������
		// �� ��.
		// ���������������� ��� ���������� �����, �.�. ����� ������������
		// ������������ �� ���� �������� � ����������, �� onCreate() ��
		// ����������.
		// ������� ����� ���������������� ������, �.�. �� ������������ �
		// ��������� onLoadFinished, ����������� ����� ������
		// Main.loaderManager.initLoader.
		if (Main.dbAdapter == null) {
			Main.dbAdapter = new DatabaseAdapter(this);
			Main.dbAdapter.open(); // TODO: �� ������������� �������� �� UI!
		}
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
		outState.putString(Main.CURRENT_THEME_ID_FIELDNAME, currentTheme);
	}

	@Override
	public void onPause() {

		super.onPause();

		// ���������� ������ ������� ������ �����, � �� � onDestroy(), ���������
		// onDestroy() �.�. � �� ������, ���� ������� ����� �������. �����
		// onPause() ����� ������ � ����� ������, �.�. ������ ����� ���� �������
		// �������� ����������� ����� �������.
		savePreferences();

		if (dbAdapter != null) {
			dbAdapter.close();
			dbAdapter = null;
		}
	}

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

		/**
		 * ���������� ���������� ������� ��������.
		 */
		@Override
		public int getCount() {
			return PAGE_COUNT;
		}

		/**
		 * ���������� ��������, ��������������� ��������� ������� ��������.
		 * 
		 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
		 */
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

		/**
		 * ���������� ��������� ��������, ��������������� ��������� �������.
		 */
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

	/**
	 * ����� ���������� ������������� ����� Loader �������� ��������. ������
	 * ���� ����� ������������ ��� ���������� UI ���������� � ��������������
	 * ����������� ������. ������ ����� (� ����� �����) ������������, ��� �����
	 * ������ ����� ���������� � ���� ����� ������ ���, ����� �������� �����
	 * ������. �������, ��� ��� ������ Loader-� ����������� �������� ������ �
	 * ��������� ����� ����������� ��������. LoaderManager ������� ����������
	 * �������� ��� ������ ��� ����� ��������� � ����� �������� ��������� �
	 * ����� onLoadFinished ������� ��������� ������ ��� ��� �������������
	 * ��������.
	 * 
	 * ����������, ����� �������������� ��������� ��������� �������� ����
	 * ��������. ��������, ��� ������ ���������� �� ��������� ���������
	 * ���������� ���������� �� ����� ������ ����� �������, ������� ������
	 * ��������� ����� ���������� ��������� ����������.
	 * 
	 * ��� ������� �������������� ���������� ����� ������������� ���������
	 * ������, ��������������� ����������. � ���� ������ ��� ������� ������� ���
	 * ������������� ������ ������ (�.�. ��� ����� ����� �����������), �� ��
	 * ������� ��������� ����������� ������������ ���� ������, �.�. ��� �������
	 * ��������� � ��� ����������� �� ����. ��������� ����������� �� ����������
	 * ������ �������, ������� ��� �� ����� ����� ������.
	 * 
	 * // � ���������: - ��������� ����� ����������� ��������� ������ � ��������
	 * �� ��� ����� ����� ������ ����� �������. ��� �� ������� �����������
	 * ������ ��������������. ��������, ���� ����� ������� �������� Cursor � ��
	 * ���������� ��� � CursorAdapter, ����������� ����������� CursorAdapter-�
	 * �� ��������� �� FLAG_AUTO_REQUERY, �� FLAG_REGISTER_CONTENT_OBSERVER
	 * (�.�. ����������� 0 ��� ��������� ������). ��� ������������ CursorAdapter
	 * �� ���������� ��� ������������ ���������� �� Cursor, � ������� ���
	 * ������������� ������ ��� ����� ��������� ���������� �� �������� �����
	 * Cursor ��� ��������� ������ ����� �������; - ��������� ��������� ������
	 * ��� ������ ������, ��� ���������� ������ �� �� ����������. ��������, ����
	 * ������ �������� Cursor �� CursorLoader, �� ��� �� ������� �������� �����
	 * close() ��������������. ���� Cursor �������� � CursorAdapter, ��� �������
	 * ������������ ����� swapCursor() ��� ����, ����� ������ Cursor ��
	 * ����������.
	 * 
	 * @param loader
	 *            The Loader that has finished.
	 * @param cursorOfReceivedData
	 *            The data generated by the Loader.
	 */
	@Override
	public void onLoadFinished(Loader<Cursor> loader,
			Cursor cursorOfReceivedData) {

		if (Main.dbAdapter != null && cursorOfReceivedData == null) {
			cursorOfReceivedData = Main.dbAdapter
					.fetchAllNotes(DatabaseAdapter.DATE_FIELD_NAME + " ASC");
		}

		int rowsInDatabase = 0;
		if (cursorOfReceivedData != null) {
			rowsInDatabase = cursorOfReceivedData.getCount();
			;
		}

		Main.cursorCurrentData = cursorOfReceivedData;

		// ����������� �������� �����������, ��� ������ ������ ���������.
		// ����� �������� �� �������� �������, ����� �� �����������!

		// ��� ����� ���������� ������ ���� ����� ����������, ��
		// ������-�� ��� �� �������� � ������������ ������ getItemPosition!

		// ���� ����� �.�. ������ ����������� ���� ������, ������������
		// ���������, ���� �������� � ��������������� ��� �.�. ���������.
		// ����� ������ ����� ������ ����� ������� getItemPosition(Object
		// object).
		adapter.notifyDataSetChanged();

		// ���� ������ �������, �� ����� �������� � ������ ��� ��������� ������.
		if (Main.cursorCurrentData != null) {
			if (Main.dbAdapter != null) {
				Main.cursorCurrentDataBackward = Main.dbAdapter
						.fetchAllNotes(DatabaseAdapter.DATE_FIELD_NAME
								+ " DESC");
			}
		}

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

			Main.graph_axis_x = new ArrayList<Date[]>();
			Main.graph_axis_x.add(x_values);

			Main.graph_axis_y = new ArrayList<double[]>();
			Main.graph_axis_y.add(y_values);

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

			Main.updateGraph = true;
			Main.updateTable = true;

			Main.setGraph(this);

			// ��������� ������������.
			Toast.makeText(
					this,
					getResources().getString(R.string.rowsInDatabase) + " "
							+ rowsInDatabase, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * ��������� � ����������� �������.
	 */
	public static void setGraph(Context context) {

		if (!Main.updateGraph)
			return;

		if (Main.graphFragmentView == null)
			return;

		if (Main.lineGraph == null)
			return;

		// ��������� �������.
		String[] series = new String[] { context.getResources().getString(
				R.string.graphValuesName1) };

		Main.lineGraph.adjust(context, series, Main.graph_axis_x,
				Main.graph_axis_y);

		// ��������� ��� �������. ������ ���������� ����� adjust(), �����
		// ������������� Main.lineGraph.renderer.
		GraphicalView graphicalView = ChartFactory.getTimeChartView(
				Main.lineGraph.context, Main.lineGraph.dataset,
				Main.lineGraph.renderer, "HH:mm");

		// ��������� ����������. ������ ���������� ����� getTimeChartView(),
		// �.�. � ��������� ������ �������� ���������� ������������ � ��������.
		Main.lineGraph.renderer.setApplyBackgroundColor(true);
		Main.lineGraph.renderer.setMarginsColor(Color.TRANSPARENT);

		LinearLayout linearLayout = (LinearLayout) graphFragmentView
				.findViewById(R.id.graph);

		// ������� ���� ������������ ��� � ���������� ������ ��� �����������
		// ��� ������� � ��������.
		linearLayout.removeAllViews();

		// ���������� ��� ������� � ��������.
		linearLayout.addView(graphicalView, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		Main.updateGraph = false;

	}

	// ������� ������ ��������� ����������� LoaderManager.LoaderCallbacks.
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// ����������, ����� �������������� ��������� ��������� ������������ �
		// ������� ��� ������ ���������� ����������. ���������� � ���� �����
		// ������ ������� ����� ��������� ������ �� ������ ����������.
	}

	/**
	 * �������� ����. ���������� ������ ���� ��� ��� ������ �������� ����.
	 */
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
	 * ���������� ���������� ��� �������������� ��� ����������� �������
	 * ����������.
	 */
	private void savePreferences() {

		SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);

		Editor editor = sharedPrefs.edit();

		editor.putString(SOURCE_SERVER_PATH_FIELDNAME, sourceServerPathString);
		editor.putString(DATE_BEGIN_FIELDNAME, dateBeginString);
		editor.putString(DATE_END_FIELDNAME, dateEndString);
		editor.putString(Main.CURRENT_THEME_ID_FIELDNAME, currentTheme);

		editor.apply();
	}

	/**
	 * ���������� �������� ������������� ���� �� ������ ����������
	 * ��������������.
	 * 
	 * @param themeIDString
	 * @return
	 */
	public static int getThemeID(String themeIDString) {

		int themeID = android.R.style.Theme_Black;

		if (themeIDString.equals("Light")) {
			themeID = android.R.style.Theme_Light;
		} else if (themeIDString.equals("Black")) {
			themeID = android.R.style.Theme_Black;
		} else if (themeIDString.equals("Translucent")) {
			themeID = android.R.style.Theme_Wallpaper;
		}

		return themeID;
	}

	/**
	 * �������� ������� ����������� � ����.
	 * 
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {

		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();

		if (activeNetworkInfo == null) {
			return false;
		} else {
			return activeNetworkInfo.isConnected();
		}
	}
}
