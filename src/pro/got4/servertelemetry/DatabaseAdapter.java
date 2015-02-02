package pro.got4.servertelemetry;

import java.io.InputStream;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Адаптер, через который производятся операции с БД.
 * 
 * @author programmer
 * 
 */
public class DatabaseAdapter {

	private final Context mContext;

	private DatabaseHelper mDatabaseHelper;
	private SQLiteDatabase mDatabase;

	// Имя БД.
	private static final String DATABASE_NAME = "data";

	// Описание таблиц БД.

	// Таблица температуры.
	public static final String DATABASE_TABLE_NAME = "temperature";

	// Колонки таблицы температуры.
	public static final String KEY_ROWID_NAME = "_id";
	public static final String KEY_ROWID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
	public static final String DATE_FIELD_NAME = "date";
	public static final String DATE_FIELD_TYPE = "INTEGER not null";
	public static final String VALUE_FIELD_NAME = "value";
	public static final String VALUE_FIELD_TYPE = "REAL not null";

	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table "
			+ DATABASE_TABLE_NAME + " (" + KEY_ROWID_NAME + " "
			+ KEY_ROWID_TYPE + ", " + DATE_FIELD_NAME + " " + DATE_FIELD_TYPE
			+ ", " + VALUE_FIELD_NAME + " " + VALUE_FIELD_TYPE + ");";

	// Форматтер даты для преобразования из входного формата.
	// private static SimpleDateFormat dateFormatter = new SimpleDateFormat(
	// "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

	public DatabaseAdapter(Context context) {
		mContext = context;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Обновление БД будет реализовано в той версии, которая
			// потребует изменения её структуры.
		}
	}

	/**
	 * Открывает существующую или создает новую БД для чтения и записи.
	 * 
	 * @return - ссылку на самого себя, позволяя использовать её в
	 *         последовательных вызовах.
	 * @throws SQLException
	 *             - если БД не может быть открыта или создана.
	 */
	public DatabaseAdapter open() throws SQLException {

		mDatabaseHelper = new DatabaseHelper(mContext);
		mDatabase = mDatabaseHelper.getWritableDatabase();

		return this;
	}

	/**
	 * Закрывает БД.
	 */
	public void close() {

		if (mDatabaseHelper != null) {
			mDatabaseHelper.close();
		}
	}

	/**
	 * Создает новую запись в БД. Если запись создана, то возвращает
	 * идентификатор строки созданной записи. Если нет - возвращает -1.
	 * 
	 * @param contentValues
	 *            набор значений, подлежащих записи.
	 * @return идентификатор строки или -1
	 */
	public long createNote(ContentValues contentValues) {
		return mDatabase.insert(DATABASE_TABLE_NAME, null, contentValues);
	}

	/**
	 * Возвращает курсор для всего набора записей таблицы.
	 * 
	 * @return Cursor курсор для всего набора записей таблицы.
	 */
	public Cursor fetchAllNotes(String orderBy) {

		Cursor cursor = null;

		if (mDatabase != null) {

			cursor = mDatabase.query(DATABASE_TABLE_NAME, new String[] {
					KEY_ROWID_NAME, DATE_FIELD_NAME, VALUE_FIELD_NAME }, null,
					null, null, null, orderBy);
		}

		return cursor;
	}

	/**
	 * Обновляет запись с указанной датой.
	 * 
	 * @param date
	 *            TimeStamp записи
	 * @param contentValues
	 *            контейнер с данными, которые д.б. помещены в запись
	 * @return true если запись была успешно обновлена
	 */
	public int updateNote(Date date, ContentValues contentValues) {

		// Convenience method for updating rows in the database.
		//
		// Parameters
		// table the table to update in
		// values a map from column names to new column values. null is a valid
		// value that will be translated to NULL.
		// whereClause the optional WHERE clause to apply when updating. Passing
		// null will update all rows.
		// whereArgs You may include ?s in the where clause, which will be
		// replaced by the values from whereArgs. The values will be bound as
		// Strings.
		//
		// Returns
		// the number of rows affected
		if (mDatabase == null) {
			return 0;
		}

		int rowsAffected = mDatabase.update(DATABASE_TABLE_NAME, contentValues,
				DATE_FIELD_NAME + "=" + date.getTime(), null);
		// Если обновить строку не удалось, значит она еще не существует и её
		// нужно добавить.
		if (rowsAffected == 0) {

			ContentValues contentValuesTimeStamp = new ContentValues(
					contentValues);
			long timeStamp = date.getTime();
			contentValuesTimeStamp.put(DatabaseAdapter.DATE_FIELD_NAME,
					timeStamp);

			long result = mDatabase.insert(DATABASE_TABLE_NAME, null,
					contentValuesTimeStamp);
			if (result == -1) {
				rowsAffected = 0;
			} else {
				rowsAffected = 1;
			}

		}

		return rowsAffected;
	}

	/**
	 * Возвращает ссылку на текущую SQLiteDatabase.
	 * 
	 * @return
	 */
	public SQLiteDatabase getDatabase() {
		return mDatabase;
	}

	/**
	 * Очищает указанную таблицу.
	 * 
	 * @param tableName
	 *            - имя таблицы.
	 */
	public void truncateTable(String tableName) {
		mDatabase.execSQL("DELETE FROM " + tableName);
	}

	/**
	 * Заполняет БД демонстрационными данными, полученными из файла ресурсов.
	 */
	public static void fillDBWithDemoData(Context context) {

		// "create table temperature (
		// _id INTEGER PRIMARY KEY AUTOINCREMENT,
		// date INTEGER not null,
		// value REAL not null"

		try {

			InputStream is = context.getAssets().open("demo_data.xml");

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document domDoc = dBuilder.parse(is);
			domDoc.getDocumentElement().normalize();

			NodeList pointNodes = domDoc.getElementsByTagName("point");

			int rowsTotal = pointNodes.getLength();

			for (int i = 0; i < rowsTotal; i++) {

				Node pointNode = pointNodes.item(i);
				NamedNodeMap pointAttrs = pointNode.getAttributes();
				Node dateNode = pointAttrs.getNamedItem("date");

				String date = dateNode.getTextContent();
				Date gmt = DataLoader.dateFormatter.parse(date);

				String val = pointNode.getTextContent();
				float temperature = Float.parseFloat(val);

				ContentValues contentValues = new ContentValues();
				contentValues
						.put(DatabaseAdapter.VALUE_FIELD_NAME, temperature);
				int rowsAffected = Main.dbAdapter
						.updateNote(gmt, contentValues);

				if (rowsAffected == 0)
					break;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
