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
 * �������, ����� ������� ������������ �������� � ��.
 * 
 * @author programmer
 * 
 */
public class DatabaseAdapter {

	private final Context mContext;

	private DatabaseHelper mDatabaseHelper;
	private SQLiteDatabase mDatabase;

	// ��� ��.
	private static final String DATABASE_NAME = "data";

	// �������� ������ ��.

	// ������� �����������.
	public static final String DATABASE_TABLE_NAME = "temperature";

	// ������� ������� �����������.
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

	// ��������� ���� ��� �������������� �� �������� �������.
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
			// ���������� �� ����� ����������� � ��� ������, �������
			// ��������� ��������� � ���������.
		}
	}

	/**
	 * ��������� ������������ ��� ������� ����� �� ��� ������ � ������.
	 * 
	 * @return - ������ �� ������ ����, �������� ������������ � �
	 *         ���������������� �������.
	 * @throws SQLException
	 *             - ���� �� �� ����� ���� ������� ��� �������.
	 */
	public DatabaseAdapter open() throws SQLException {

		mDatabaseHelper = new DatabaseHelper(mContext);
		mDatabase = mDatabaseHelper.getWritableDatabase();

		return this;
	}

	/**
	 * ��������� ��.
	 */
	public void close() {

		if (mDatabaseHelper != null) {
			mDatabaseHelper.close();
		}
	}

	/**
	 * ������� ����� ������ � ��. ���� ������ �������, �� ����������
	 * ������������� ������ ��������� ������. ���� ��� - ���������� -1.
	 * 
	 * @param contentValues
	 *            ����� ��������, ���������� ������.
	 * @return ������������� ������ ��� -1
	 */
	public long createNote(ContentValues contentValues) {
		return mDatabase.insert(DATABASE_TABLE_NAME, null, contentValues);
	}

	/**
	 * ���������� ������ ��� ����� ������ ������� �������.
	 * 
	 * @return Cursor ������ ��� ����� ������ ������� �������.
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
	 * ��������� ������ � ��������� �����.
	 * 
	 * @param date
	 *            TimeStamp ������
	 * @param contentValues
	 *            ��������� � �������, ������� �.�. �������� � ������
	 * @return true ���� ������ ���� ������� ���������
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
		// ���� �������� ������ �� �������, ������ ��� ��� �� ���������� � �
		// ����� ��������.
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
	 * ���������� ������ �� ������� SQLiteDatabase.
	 * 
	 * @return
	 */
	public SQLiteDatabase getDatabase() {
		return mDatabase;
	}

	/**
	 * ������� ��������� �������.
	 * 
	 * @param tableName
	 *            - ��� �������.
	 */
	public void truncateTable(String tableName) {
		mDatabase.execSQL("DELETE FROM " + tableName);
	}

	/**
	 * ��������� �� ����������������� �������, ����������� �� ����� ��������.
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
