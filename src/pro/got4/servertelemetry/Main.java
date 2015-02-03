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
	 * Идентификаторы страниц, отображаемых вьюпейджером.
	 */
	public static final int PAGE_PREFS_INDEX = 0;
	public static final int PAGE_GRAPH_INDEX = 1;
	public static final int PAGE_TABLE_INDEX = 2;
	public static final int PREFERENCE_MENU_ITEM_ID = 0;

	/**
	 * Количество страниц, отображаемых вьюпейджером.
	 */
	public static final int PAGE_COUNT = 3;

	/**
	 * Идентификатор менеджера загрузок.
	 */
	public static final int LOADER_ID = 1;

	/**
	 * Идентификаторы таблиц и полей БД.
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
	 * Строковой идентификатор текущей используемой темы оформления.
	 */
	String currentTheme;

	/**
	 * Вьюпейджер, используемый для отображения страниц.
	 */
	private ViewPager pager;

	/**
	 * Адаптер, используемый вьюпейджером.
	 */
	private MyFragmentPagerAdapter adapter;

	/**
	 * Параметры, используемые на уровне приложения.
	 */
	public String sourceServerPathString;
	public String dateBeginString;
	public String dateEndString;

	/**
	 * Ссылка на график.
	 */
	public static LineGraph lineGraph;

	/**
	 * Фрагмент, содержащий вью графика.
	 */
	public static View graphFragmentView;

	// Данные, отображаемые графиком.
	public static ArrayList<Date[]> graph_axis_x;
	public static ArrayList<double[]> graph_axis_y;

	/**
	 * Флаг необходимости обновить график.
	 */
	public static boolean updateGraph;

	/**
	 * Флаг необходимости обновить таблицу.
	 */
	public static boolean updateTable;

	/**
	 * Ссылка на адаптер БД.
	 */
	public static DatabaseAdapter dbAdapter;

	/**
	 * Адаптер для списка.
	 * 
	 */
	public static SimpleCursorAdapter scAdapter;

	/**
	 * Курсор набора отображаемых данных.
	 * 
	 */
	public static Cursor cursorCurrentData;

	/**
	 * Курсор набора отображаемых данных в обратной сортировке (используется для
	 * отображения списка).
	 * 
	 */
	public static Cursor cursorCurrentDataBackward;

	/**
	 * Интерфейс, который должен поддерживаться фрагментом для его обновления.
	 */
	public interface FragmentUpdateListener {
		public void update(Bundle bundle);
	}

	// ///////////////////////////
	// ЖИЗНЕННЫЙ ЦИКЛ АКТИВНОСТИ

	// Это тот Bundle, который сохранен в onSaveInstanceState.
	public void onCreate(Bundle savedInstanceState) {

		// Получение данных, сохраненных при предыдущем запуске приложения.
		// Возможны следующие ситуации восстановления данных:
		// а) первый запуск приложения;
		// б) перезапуск приложения (например, для установки новой темы);
		// в) запуск приложения при смене конфигурации (например, при повороте
		// экрана);
		// г) запуск приложения, которое находилось в фоновом режиме, потом было
		// убито системой (что вызвало срабатывание события
		// onSaveInstanceState(Bundle)), а потом было восстановлено.

		// При запуске системы производится считывание сохранённого состояния в
		// следующем порядке:
		// 1) Из SharedPreferences. Это данные, сохраненные при закрытии
		// приложения пользователем в событии onDestroy();
		// 2) Из extras. Это данные, сохраненные в экстрах активности при
		// принудительном перезапуске приложения из метода
		// onResume();
		// 3) Из Bundle. Это данные, сохраненные при закрытии приложения
		// системой в методе onSaveInstanceState(Bundle).

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

		// Если текущая тема не указана, то по умолчанию используется первая
		// тема из списка.
		if (currentTheme.isEmpty()) {
			String[] themes = getResources().getStringArray(
					R.array.ThemesIDList_Str);
			currentTheme = themes[0];
		}

		// Получение темы из настроек. Если не указана, то устанавливается тема
		// по умолчанию.
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		String themeInPrefs = sp.getString(Main.CURRENT_THEME_ID_FIELDNAME, "");
		if (themeInPrefs.isEmpty()) {

			themeInPrefs = currentTheme;

			SharedPreferences.Editor editor = sp.edit();
			editor.putString(Main.CURRENT_THEME_ID_FIELDNAME, currentTheme);

			editor.apply();

		} else {

			// Если в настройках тема указана, то она устанавливается в качестве
			// текущей.
			currentTheme = themeInPrefs;
		}

		// Установка темы. Следует делать ДО вызова метода суперкласса!
		setTheme(getThemeID(currentTheme));
		
		super.onCreate(savedInstanceState);
		
		// Установка вью.
		setContentView(R.layout.main);

		// Присвоение вьюпейджеру адаптера.
		pager = (ViewPager) findViewById(R.id.pager);

		adapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);

		// Экземпляр класса графика.
		Main.lineGraph = new LineGraph();
	}

	@Override
	public void onStart() {

		super.onStart();

		// Инициализация загрузчика.
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

		// Если тема, полученная из настроек не совпадает с текущей темой,
		// значит пауза активности была связана с заходом в экран настроек и
		// текущую тему нужно поменять.
		// А т.к. текущую тему возможно изменить только перед установкой любых
		// вью контекста (перед вызовом setContentView(View) или inflate(int,
		// ViewGroup)), то приложение нужно перезагрузить и установка темы
		// будет произведена в событии onCreate.
		// После вызова finish() сохранение текущих данных производится в
		// событии onDestroy().
		if (themeInPrefs.isEmpty()) {

			themeInPrefs = currentTheme;

			SharedPreferences.Editor editor = sp.edit();
			editor.putString(Main.CURRENT_THEME_ID_FIELDNAME, currentTheme);
			editor.apply();
		}

		if (!themeInPrefs.equals(currentTheme)) {

			// Сообщение пользователю.
			Toast.makeText(this,
					"Установка темы оформления \"" + themeInPrefs + "\"",
					Toast.LENGTH_SHORT).show();

			finish();

			// Сохранение данных, которые будут восстановлены в onCreate при
			// перезапуске приложения.
			// Перезапуск нужен, чтобы установилась тема.
			// При вызове finish() событие onSaveInstanceState(Bundle) не
			// вызывается (т.е. в Bundle значения сохранить не получится), а
			// событие onDestroy() вызывается позже (т.е. в SharedPrefs
			// сохранение произойдет позже)!
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

		// Инициализация адаптера БД, позволяющего записывать и считывать данные
		// из БД.
		// Инициализировать его необходимо здесь, т.к. когда пользователь
		// возвращается из меню настроек в активность, то onCreate() не
		// вызывается.
		// Адаптер нужно инициализировать первым, т.к. он используется в
		// процедуре onLoadFinished, запускаемой после вызова
		// Main.loaderManager.initLoader.
		if (Main.dbAdapter == null) {
			Main.dbAdapter = new DatabaseAdapter(this);
			Main.dbAdapter.open(); // TODO: Не рекомендуется вызывать из UI!
		}
	}

	// Вызывается, чтобы получить состояние экземпляра из активности перед
	// уничтожением таким образом, чтобы это состояние могло быть восстановлено
	// в onCreate(Bundle) или в onRestoreInstanceState(Bundle) (Bundle,
	// сформированное в этом методе будет передано в оба метода).
	// Метод вызывается до того, как активность будет уничтожена и когда
	// она будет запущена через некоторое время, активность сможет восстановить
	// своё состояние. Например, если активность Б запущена из активности А, и в
	// какой-то момент времени активность А уничтожена ради высвобождения
	// ресурсов, активность А будет иметь возможность сохранить текущее
	// состояние своего пользовательского интерфейса через этот метод. Поэтому
	// когда пользователь вернется в активность А, состояние пользовательского
	// интерфейса м.б. восстановлено через onCreate(Bundle) или
	// onRestoreInstanceState(Bundle).
	// Не путайте этот метод с вызовами методов жизненного цикла активности,
	// такими, как метод onPause(), которые всегда вызываются когда активность
	// уходит на задний фон или уничтожается, или метод onStop(), который
	// вызывается перед уничтожением.
	// Один пример, когда вызывается onPause() и onStop(), но не
	// вызывается onSaveInstanceState(Bundle) - это когда пользователь
	// возвращается из активности Б в активность А: нет необходимости вызывать
	// onSaveInstanceState(Bundle) в активности Б т.к. данный экземпляр никогда
	// не будет восстановлен, поэтому система избегает этого вызова.
	// Еще пример когда будет вызван onPause(), но не будет вызван
	// onSaveInstanceState(Bundle) - это когда активность Б запущена на переднем
	// плане перед активностью А: система может избежать вызова
	// onSaveInstanceState(Bundle) в активности А, если она не уничтожалась в
	// течение жизненного цикла активности Б с того момента как состояние
	// интерфейса пользователя активности А остается неизменным.
	//
	// !!!
	// Реализация по-умолчанию заботится о большинстве состояний
	// пользовательского интерфейса путем вызова onSaveInstanceState() для
	// каждого вью в иерархии, имеющего идентификатор и путем сохранения
	// идентификатора вью, имеющего текущий фокус (все из которых восстановлены
	// реализацией по-умолчанию метода onRestoreInstanceState(Bundle)). Если вы
	// перегрузите этот метод чтобы сохранить дополнительную информацию, не
	// охваченную каждым индивидуальным вью, вы скорее всего захотите вызвать
	// реализацию по-умолчанию, в противном случае будьте готовы сами
	// сохранять все состояния каждого вью.
	//
	// В случае вызова этот метод срабатывает перед onStop(). Нет гарантий,
	// произойдет он до или после onPause().
	// Параметры:
	// outState - Bundle в котором нужно размещать сохраненное состояние.
	@Override
	public void onSaveInstanceState(Bundle outState) {

		// Если не вызывать, то не сохраняется:
		// - текущая страница ViewPager;
		// - текущая строка списка.
		super.onSaveInstanceState(outState);

		// Сохранение полей класса Main, которые м.б. восстановлены в onCreate.
		outState.putString(SOURCE_SERVER_PATH_FIELDNAME, sourceServerPathString);
		outState.putString(DATE_BEGIN_FIELDNAME, dateBeginString);
		outState.putString(DATE_END_FIELDNAME, dateEndString);
		outState.putString(Main.CURRENT_THEME_ID_FIELDNAME, currentTheme);
	}

	@Override
	public void onPause() {

		super.onPause();

		// Сохранение данных следует делать здесь, а не в onDestroy(), поскольку
		// onDestroy() м.б. и не вызван, если система убьёт процесс. Метод
		// onPause() будет вызван в любом случае, т.к. только после него система
		// получает возможность убить процесс.
		savePreferences();

		if (dbAdapter != null) {
			dbAdapter.close();
			dbAdapter = null;
		}
	}

	// КОНЕЦ ЖИЗНЕННОГО ЦИКЛА
	// ///////////////////////
	// ///////////////////////

	/**
	 * Адаптер, отвечающий за предоставление фрагментов ViewPager-y.
	 * 
	 * @author programmer
	 * 
	 */
	private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/**
		 * Возвращает количество страниц адаптера.
		 */
		@Override
		public int getCount() {
			return PAGE_COUNT;
		}

		/**
		 * Возвращает фрагмент, соответствующий указанной позиции страницы.
		 * 
		 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
		 */
		@Override
		public Fragment getItem(int position) {

			Bundle bundle = new Bundle();
			bundle.putInt(ARGUMENT_PAGE_NUMBER_FIELDNAME, position);

			// Получение экземпляра фрагмента путем вызова статического
			// метода.
			Fragment pageFragment = null;
			if (position == Main.PAGE_PREFS_INDEX) {
				pageFragment = new Prefs();
			} else {
				pageFragment = PageFragment.newInstance(bundle);
			}

			return pageFragment;
		}

		/**
		 * Возвращает заголовок страницы, соответствующий указанной позиции.
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

		// Событие вызывается после вызова метода адаптера
		// notifyDataSetChanged()
		// Причем object - это обновляемый фрагмент.
		//
		// Вызывается когда родительский вью пытается определить, что позиция
		// элемента была изменена.
		//
		// Реализация по умолчанию предполагает что элементы никогда не меняют
		// позицию и всегда возвращает POSITION_UNCHANGED.
		//
		// Параметры:
		// Object - объект, представляющий элемент, предварительно возвращенный
		// вызовом instantiateItem(View, int).
		//
		// Возвращает:
		// индекс новой позиции элемента от 0 до getCount() - 1;
		// POSITION_UNCHANGED - если позиция объекта не изменена;
		// POSITION_NONE - если элемент больше не представлен в адаптере.
		@Override
		public int getItemPosition(Object object) {

			if (object instanceof PageFragment) {

				// Получаем bundle фрагмента, где хранится его позиция.
				PageFragment pageFragment = (PageFragment) object;
				Bundle bundle = pageFragment.getArguments();
				int position = bundle
						.getInt(Main.ARGUMENT_PAGE_NUMBER_FIELDNAME);

				if (position == Main.PAGE_PREFS_INDEX) {

					// Лишняя, т.к. обновление производится пересозданием
					// фрагмента.
					pageFragment.update(null);

				} else if (position == Main.PAGE_GRAPH_INDEX && updateGraph) {

					// Возвращение POSITION_NONE приводит к пересозданию
					// фрагмента.
					return PagerAdapter.POSITION_NONE;

				} else if (position == Main.PAGE_TABLE_INDEX && updateTable) {

					// Возвращение POSITION_NONE приводит к пересозданию
					// фрагмента.
					return PagerAdapter.POSITION_NONE;
				}
			}

			return super.getItemPosition(object); // По сути возвращает
													// POSITION_UNCHANGED.
		}
	}

	// Когда вы пытаетесь получить доступ к загрузчику (например, через
	// initLoader()), он проверяет, существует ли загрузчик с указанным
	// идентификатором. Если такового нет, то вызывается метод onCreateLoader()
	// интерфейса LoaderManager.LoaderCallbacks. Это место, где вы создаете
	// новый загрузчик. Обычно это будет CursorLoader, но вы можете реализовать
	// свой собственный субкласс загрузчика.
	// В этом примере вызов метода onCreateLoader() создает CursorLoader. Вы
	// обязаны создать CursorLoader используя его конструктор, который требует
	// полный набор информации, необходимой для выполнения запроса к
	// ContentProvider. В частности, ему необходимо:
	// uri - URI для получения содержимого;
	// projection - список колонок, которые нужно вернуть. Передача null вернет
	// все колонки, но это неэффективно;
	// selection - фильтр, определяющий, какие строки возвращать,
	// форматированный как конструкция SQL WHERE (исключая сам WHERE). Передача
	// null вернет все строки для указанного URI;
	// selectionArgs - вы можете включить знаки вопроса в выборку, которые будут
	// заменены значениями из selectionArgs в том порядке, в котором они
	// появляются в выборке. Эти значения обязательно являются строками;
	// sortOrder - указание, как упорядочивать строки, форматированное как
	// конструкция SQL ORDER (исключая сам ORDER). Передача null приведет к
	// использованию сортировки по умолчанию, при которой данные м.б.
	// неупорядоченны.
	@SuppressLint("SimpleDateFormat")
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

		// Метод-фабрика, просто возвращающий экземпляр нового загрузчика.
		// Метод вызывается LoaderManager-ом при первом создании загрузчика.
		DataLoader dataLoader = new DataLoader(this);
		if (bundle != null) {

			// Сборка URI-строки на основе переданных аргументов. Пример:
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

			// Дата начала имеет нулевое время, а дата окончания - "23:59:59".
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
	 * Метод вызывается автоматически когда Loader закончил загрузку. Обычно
	 * этот метод используется для обновления UI приложения с использованием
	 * загруженных данных. Клиент может (и имеет право) предполагать, что новые
	 * данные будут возвращены в этот метод каждый раз, когда доступны новые
	 * данные. Помните, что это работа Loader-а отслеживать источник данных и
	 * выполнять новую асинхронную загрузку. LoaderManager получит результаты
	 * загрузки как только она будет завершена и затем перешлет результат в
	 * метод onLoadFinished объекта обратного вызова для его использования
	 * клиентом.
	 * 
	 * Вызывается, когда предварительно созданный загрузчик закончил свою
	 * загрузку. Заметьте, что обычно приложению не разрешено выполнять
	 * транзакции фрагментов во время вызова этого события, которые смогут
	 * произойти после сохранения состояния активности.
	 * 
	 * Эта функция гарантированно вызывается перед освобождением последних
	 * данных, предоставленных загрузчику. В этот момент вам следует удалить все
	 * использования старых данных (т.к. они будут скоро освобождены), но не
	 * следует выполнять собственное освобождение этих данных, т.к. ими владеет
	 * загрузчик и сам позаботится об этом. Загрузчик позаботится об управлении
	 * своими данными, поэтому вам не нужно этого делать.
	 * 
	 * // В частности: - загрузчик будет отслеживать изменения данных и сообщать
	 * их вам через новые вызовы этого события. Вам не следует отслеживать
	 * данные самостоятельно. Например, если этими данными является Cursor и вы
	 * размещаете его в CursorAdapter, используйте конструктор CursorAdapter-а
	 * не передавая ни FLAG_AUTO_REQUERY, ни FLAG_REGISTER_CONTENT_OBSERVER
	 * (т.е. используйте 0 для аргумента флагов). Это предотвратит CursorAdapter
	 * от исполнения его собственного наблюдения за Cursor, в котором нет
	 * необходимости потому что когда изменения происходят вы получите новый
	 * Cursor при следующем вызове этого события; - загрузчик освободит данные
	 * как только узнает, что приложение больше их не использует. Например, если
	 * данные являются Cursor из CursorLoader, то вам не следует вызывать метод
	 * close() самостоятельно. Если Cursor размещен в CursorAdapter, вам следует
	 * использовать метод swapCursor() для того, чтобы старый Cursor не
	 * закрывался.
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

		// Уведомление адаптера вьюпейджера, что состав данных изменился.
		// Нужно вызывать до создания графика, чтобы он отрисовался!

		// При смене ориентации экрана этот метод вызывается, но
		// почему-то это не приводит к последующему вызову getItemPosition!

		// Этот метод д.б. вызван приложением если данные, отображаемые
		// адаптером, были изменены и ассоциированные вью д.б. обновлены.
		// После вызова этого метода будет вызвана getItemPosition(Object
		// object).
		adapter.notifyDataSetChanged();

		// Если курсор получен, то можно получить и курсор для обратного обхода.
		if (Main.cursorCurrentData != null) {
			if (Main.dbAdapter != null) {
				Main.cursorCurrentDataBackward = Main.dbAdapter
						.fetchAllNotes(DatabaseAdapter.DATE_FIELD_NAME
								+ " DESC");
			}
		}

		Date[] x_values = new Date[rowsInDatabase];
		double[] y_values = new double[rowsInDatabase];

		// Копирование содержимого записей, представленных курсором в массивы.
		// Для вывода подписей к точкам библиотека AChartEngine требует,
		// чтобы даты были расположены строго по возрастанию!
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

			// Уведомление открытых страниц (фрагментов) об обновлении (закрытые
			// фрагменты сами обновятся при их создании в onCreateView()).
			// После вызова этого метода сработает событие
			// getItemPosition(Object
			// object) в параметре которого будет передан обновляемый фрагмент.

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

			// Сообщение пользователю.
			Toast.makeText(
					this,
					getResources().getString(R.string.rowsInDatabase) + " "
							+ rowsInDatabase, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Настройка и отображение графика.
	 */
	public static void setGraph(Context context) {

		if (!Main.updateGraph)
			return;

		if (Main.graphFragmentView == null)
			return;

		if (Main.lineGraph == null)
			return;

		// Настройка графика.
		String[] series = new String[] { context.getResources().getString(
				R.string.graphValuesName1) };

		Main.lineGraph.adjust(context, series, Main.graph_axis_x,
				Main.graph_axis_y);

		// Получение вью графика. Должно вызываться после adjust(), чтобы
		// сформировался Main.lineGraph.renderer.
		GraphicalView graphicalView = ChartFactory.getTimeChartView(
				Main.lineGraph.context, Main.lineGraph.dataset,
				Main.lineGraph.renderer, "HH:mm");

		// Установка оформления. Должна вызываться после getTimeChartView(),
		// т.к. в противном случае значения оформления сбрасываются к исходным.
		Main.lineGraph.renderer.setApplyBackgroundColor(true);
		Main.lineGraph.renderer.setMarginsColor(Color.TRANSPARENT);

		LinearLayout linearLayout = (LinearLayout) graphFragmentView
				.findViewById(R.id.graph);

		// Очистка всех существующих вью и добавление только что полученного
		// вью графика в иерархию.
		linearLayout.removeAllViews();

		// Добавление вью графика в иерархию.
		linearLayout.addView(graphicalView, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		Main.updateGraph = false;

	}

	// Наличие метода требуется интерфейсом LoaderManager.LoaderCallbacks.
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Вызывается, когда предварительно созданный загрузчик сбрасывается и
		// поэтому его данные становятся недоступны. Приложение в этой точке
		// должно удалить любые имеющиеся ссылки на данные загрузчика.
	}

	/**
	 * Создание меню. Вызывается только один раз при первом создании меню.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuItem item = menu.add(Menu.NONE, PREFERENCE_MENU_ITEM_ID, Menu.NONE,
				R.string.PreferenceMenuTitle);

		item.setIntent(new Intent(this, AppPrefsActivity.class));

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Возвращает дату, отформатированную для использования в URI.<br>
	 * Например, из "01.02.2014" будет возвращено "20140201".
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
	 * Сохранение параметров для восстановления при последующем запуске
	 * приложения.
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
	 * Возвращает числовой идентификатор темы на основе строкового
	 * идентификатора.
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
	 * Проверка наличия подключения к сети.
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
