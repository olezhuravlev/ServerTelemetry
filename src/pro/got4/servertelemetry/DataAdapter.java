package pro.got4.servertelemetry;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Адаптер для связи списка с БД.
 * 
 * @author programmer
 * 
 */
public class DataAdapter extends ResourceCursorAdapter {

	public DataAdapter(Context context, Cursor cursor) {

		super(context, R.layout.list_item, cursor,
				ResourceCursorAdapter.FLAG_AUTO_REQUERY);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		// Получение инфлятора из данного контекста.
		LayoutInflater li = LayoutInflater.from(context);

		return li.inflate(R.layout.list_item, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		TextView date = (TextView) view.findViewById(R.id.date);
		TextView comment = (TextView) view.findViewById(R.id.comment);
		TextView temperature = (TextView) view.findViewById(R.id.temperature);

		int dateColumnIndex = cursor
				.getColumnIndex(DatabaseAdapter.DATE_FIELD_NAME);
		long dateLong = cursor.getLong(dateColumnIndex);
		Date dt = new Date(dateLong);
		date.setText(dt.toLocaleString());

		comment.setText("");

		int valueColumnIndex = cursor
				.getColumnIndex(DatabaseAdapter.VALUE_FIELD_NAME);
		Float value = cursor.getFloat(valueColumnIndex);
		temperature.setText(value.toString());

	}
}
