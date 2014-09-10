package pro.got4.servertelemetry;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.util.Log;

/**
 * ����������� ��������� ������.
 */

// public class DataLoader extends AsyncTaskLoader<Cursor> {
public class DataLoader extends CursorLoader {

	public final static String BROADCAST_ACTION = "pro.got4.servertelemetry";

	private String uriString;
	private Cursor cursorCurrentData;

	// ���������������� �����-�����������.
	private DataObserver mObserver;

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public DataLoader(Context context) {
		// ���������� ����� �������������� ����������� ������������
		// (�����������, ��� ��� �� ������� � LoadManager), ������� ������� ��
		// ���������� ������ �� �������� ��������. � ��������� ������ ���
		// �������� � ������ ����� ��������� ����������. ������ �����
		// ����������� ����������� �������� ������ �� ��������
		// ����������, ������� ����� ���� ������� ������� getContext().
		super(context);
	}

	public void setURIString(String uriString) {
		this.uriString = uriString;
	}

	// �������� ������ ������������� ���� ����� ��� �������� ������ �������
	// startLoading().
	// onStartLoading() �� ���������� �������� ��������, ��� ����� ��������
	// ����������� ������ ������ startLoading().
	// ����� ����������� ��������� ���������� � ����������� �� ���
	// ���������.
	@Override
	public void onStartLoading() {

		Log.d(Main.TAG,
				"DataLoader.onStartLoading(): ���� ������ ����������, �� ��� ��������. �������� ���������� �����������. �������������� ����� ��������, ���� ���������� ��������� ��������.");

		// ���� ������ �� ���������� �������� ����������, �� �� ����� ���������
		// ����������.
		if (cursorCurrentData != null) {
			deliverResult(cursorCurrentData);
		}

		// ������ ����������� ��������� ��������� ������.
		if (mObserver == null) {

			mObserver = new DataObserver(this);

			// ����������� �� ���������� �������.
			IntentFilter filter = new IntentFilter(BROADCAST_ACTION);
			getContext().registerReceiver(mObserver, filter);
		}

		// ����� ����������� ��������� ���������, �� ������ ������� �����
		// onContentChanged() ����������Loader-�, ������� �������� � ����, ���
		// ��������� ����� ������ takeContentChanged() ������ TRUE. � ����
		// ������ �� ���������� ����� ��������.
		if (takeContentChanged() || cursorCurrentData == null) {
			forceLoad();
		}
	}

	@Override
	public void forceLoad() {

		Log.d(Main.TAG, "DataLoader.forceLoad()");

		// �������������� ���������� ����������� ��������.
		// � ������� �� startLoading() ���� ����� ���������� ����������
		// ����������� ������ � ������������� �������� �����
		// (��� �������������� ������ ����� ���������� onForceLoad()).
		// � �������� �� ������ ������ �������� ���� ����� ������ �����
		// ��������� ��������, �.�. ����� isStarted() ���������� TRUE.

		// �������� � ������ ������� onForceLoad() (������� ������ ����������� �
		// �������� ������ ��������).

		super.forceLoad();
	}

	// ���������� � ������� ������ ��� ���������� ������� �������� � ��������
	// ���������� ��������. ���������� �� ������ ���������� ��������� ��������,
	// �� ������ ���������� ��� �� ����� ������, ������� � ����� ������
	// ���������� ������� deliverResult(D) � ������ ���������� ������������.
	// ���� ���������� ������� ��������� ����������� � ������ ����������, �� ���
	// ����� ����������� deliverResult(D) � ������� ��� ���. ����� ������������
	// ������, ��� ����� ������ ������������ ��������� �������� �������
	// isLoadInBackgroundCanceled() � ��������� ����� ��� ���������� ������.
	// ��������� ����� ����� ����������� cancelLoadInBackground() �����
	// ��������� �������� �������� ������ ������ isLoadInBackgroundCanceled().
	// ����� �������� ��������, ���� ����� ����� ��� ����������� ���������, ���
	// ��������� OperationCanceledException. � ������ ������ ��������� �������
	// onCanceled(D) ����� ��������� ������� ����� ������ � �������������
	// �����������, � ������ ��� �������.
	@Override
	public Cursor loadInBackground() {

		Log.d(Main.TAG,
				"DataLoader.loadInBackground(): ��������� XML-������, �������� ��� �������� ��, �������� ������ � ����������, �������� � ����������� ������� �� ��� ������.");

		// ���������� ����������� ��������. ����� ���������� � ������� ������ �
		// ������ ������������ ����� ����� ������, ������� �.�. ����������
		// �������. ����� ������� ��������� ������ � ��������� ��������� �
		// ������.

		Cursor cursor = null;

		int length = 0;
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuider = dbFactory.newDocumentBuilder();

			Document doc = dBuider.parse(uriString);
			doc.getDocumentElement().normalize();

			NodeList pointNodes = doc.getElementsByTagName("point");
			length = pointNodes.getLength();

			if (Main.dbAdapter != null) {
				Main.dbAdapter.open();
				Main.dbAdapter.getDatabase().beginTransaction();

				for (int i = 0; i < length; i++) {

					if (isAbandoned())
						break;

					if (isReset())
						break;

					Node pointNode = pointNodes.item(i);
					if (pointNode.getNodeType() == Node.ELEMENT_NODE) {

						NamedNodeMap pointAttr = pointNode.getAttributes();
						Node dateNode = pointAttr.getNamedItem("date");

						// ��������� �������� � ���� �����.
						String dateValue = dateNode.getTextContent(); // 2014-01-01
																		// 12:00:00
						String tempValue = pointNode.getTextContent(); // 10.5

						// ���������� ��������.
						Date date;
						try {
							date = dateFormatter.parse(dateValue);
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}

						Float temperature;
						try {
							temperature = Float.valueOf(tempValue);
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}

						// ���������� � ��.
						if (Main.dbAdapter != null) {

							ContentValues contentValues = new ContentValues();
							contentValues.put(DatabaseAdapter.VALUE_FIELD_NAME,
									temperature);
							Main.dbAdapter.updateNote(date, contentValues);
						}

					}
				}

				Main.dbAdapter.getDatabase().setTransactionSuccessful();
				Main.dbAdapter.getDatabase().endTransaction();

				// ���������� ������ �� ������ ������ ������ ����� �����������
				// ��.
				Log.d(Main.TAG,
						"DataLoader.loadInBackground(): �������� ������� �� ��� ������!");
				cursor = Main.dbAdapter
						.fetchAllNotes(DatabaseAdapter.DATE_FIELD_NAME + " ASC");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return cursor;
	}

	// ���������� ��������� �������� ������������������� ���������.
	@Override
	public void deliverResult(Cursor cursorNewData) {

		Log.d(Main.TAG,
				"DataLoader.deliverResult(): ���� ��������� �������, �� ������������ �������� � �����. ��������� ����� cursorData ������ �� ����� ������. ���� ��������� �������, �� �������� ������.");

		// ����� ���������� ��������� ������������������� ���������.
		// ������ ��������� ������ ����������. ����������� ������ ���� ������ ��
		// ��������� ������ ��������.

		// ����������, ����� ���������� ����� ������ ��� �������� �������.
		// ���������� �������� �� ������������������� ��������� (�.�.
		// LoaderManager), ������� �������� ���� ��������� ������� ����� �����
		// onLoadFinished.

		// ���� ��������� ��� �������, �� ������������ ��������� � ������������
		// ������.
		if (isReset()) {
			releaseResources(cursorNewData);
			return;
		}

		// ���������� ������ �� ������ ������, ����� ��� �� ���� ����������
		// ��������� ������. �� ������ �������� ��� ������ �� ��� ���, ���� ��
		// ����� �������� ����� ������.
		Cursor oldData = cursorCurrentData;
		cursorCurrentData = cursorNewData;

		// ���� ��������� � ���������� ���������, ���������� �������� ������
		// �������.
		// ��� �������� ����� �����������.

		// deliverResult() ���������� ��������� �������� �������������������
		// ���������.
		// ������ ��������� ������ ����������. ����������� ������ ���� ������ ��
		// ��������� ������ ��������.
		if (isStarted()) {
			super.deliverResult(cursorNewData);
		}

		// ��������� ������ ������, �.�. ��� ������ �� �����.
		// ����� releaseResources(Cursor) ����, �.�. � ����������� ��������
		// ������ ��������� ���������.
		if (oldData != null && oldData != cursorNewData) {
			releaseResources(oldData);
		}

	}

	// �������� ������ ����������� ��� ������� ��� ����, ����� ����������
	// ��������� ������ ����������. ��� ������� ���������� �������� ��
	// ��������, � ��� ��������� ������ ������ stopLoading().
	// ������� ������ ���������� �� �������� ������ ��������.
	@Override
	public void onStopLoading() {

		Log.d(Main.TAG, "DataLoader.onStopLoading(): ������ ������� ��������.");

		// Attempt to cancel the current load task. Must be called on the main
		// thread of the process.
		//
		// Cancellation is not an immediate operation, since the load is
		// performed in a background thread. If there is currently a load in
		// progress, this method requests that the load be canceled, and notes
		// this is the case; once the background thread has completed its work
		// its remaining state will be cleared. If another load request comes in
		// during this time, it will be held until the canceled load is
		// complete.
		//
		// Returns false if the task could not be canceled, typically because it
		// has already completed normally, or because startLoading() hasn't been
		// called; returns true otherwise. When true is returned, the task is
		// still running and the Loader.OnLoadCanceledListener will be called
		// when the task completes.

		// ��������� ��������� � ������������� ���������, ������� ��� �������
		// ���������� �������� ������� �������� (���� ��� ����������).
		cancelLoad();

		// �� ��������� ����������� ��� ���������. ���������� � �������������
		// ��������� ������ �� ��� ����������� �������� ������ �� �������
		// ���������, ������� ���� ��������� ����� ������� �����, �� �� �����
		// �����, ��� ����� ��������� ����� ��������.
	}

	// �������� ������ ������������� ���� ����� ��� ����������� ������
	// ������ ���������� ��� ������ reset(). ������� ���������� ��������
	// �� ��������, � ��� ��������� ������ reset(). ����� ������ ����������
	// �� �������� ������ ��������.
	@Override
	public void onReset() {

		Log.d(Main.TAG,
				"DataLoader.onReset(): ����� onStopLoading(), ������������ �������, ������������ �����������.");

		// �����������, ��� ��������� ��� ����������.
		onStopLoading();

		// ��������� ������������, ������� ��� ������� ���������� ������������
		// ���������.
		if (mObserver != null) {

			Log.d(Main.TAG, "DataLoader.onReset(): ������������ �����������!");
			getContext().unregisterReceiver(mObserver);

			mObserver = null;
		}
	}

	/**
	 * ��������������� �����, �������������� ������������ ��������,
	 * ��������������� � ������� ����������� ������� ������.
	 */
	private void releaseResources(Cursor cursor) {

		Log.d(Main.TAG, "DataLoader.releaseResources(): ������������ �������!");

		// ��� �������� ������ ������ ������ �� �����.
		// ��, ��������, ��� ������� ��� ������� ������� ��� �����.
		// ��� �������, ��������������� � ����������� �.�. ����������� �����.
		// ����������� �������, ��������������� � �������.
		if (cursor != null) {

			Log.d(Main.TAG,
					"DataLoader.releaseResources(): ������������ �������!");
			releaseResources(cursor);

			cursor = null;
		}
	}
}
