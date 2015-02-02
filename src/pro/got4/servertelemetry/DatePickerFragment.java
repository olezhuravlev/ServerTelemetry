package pro.got4.servertelemetry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;

/**
 * ������ ��� ����� ����.
 * 
 * @author programmer
 * 
 */
public class DatePickerFragment extends DialogFragment {

	static final String VIEW_ID = "view_id";

	// ������������� ��, � ������� ������ ������ ������ ���������� ���� ������.
	private int viewID;

	DatePickerDialog datePickerDialog;

	@SuppressLint("SimpleDateFormat")
	private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"dd.MM.yyyy");

	// �������� ����, ������� ����� ����������� � ������.
	private int year;
	private int monthOfYear;
	private int dayOfMonth;

	// �����-��������� ��� �������� ���.
	public static class DateContainer {

		public Date date1;
		public Date date2;

		public DateContainer() {
			date1 = getCurrentDate();
			date2 = date1;
		}

		public DateContainer(Date date1, Date date2) {
			this.date1 = date1;
			this.date2 = date2;
		}
	}

	// ��������� ��� ��������� ���� � ���� dateBegin.
	public OnDateSetListener listener = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			// ��� ����� ����� ������, �.�. ������������ ����������
			// �.�. �������� �� ����� ����������� ������� � ���� ���, ��
			// �������� ���������� ������� ����� ��� �� ������������.
			Main main = (Main) getActivity();

			Date date = new Date(year - 1900, monthOfYear, dayOfMonth);
			Date otherPeriodDate = null;

			// ��������� �������� ������������� �������� ����� ������ � ��������
			// � ��������� ����!

			// ��������� �������� � ��������������� ���� ����������.
			if (viewID == R.id.dateBegin) {

				try {
					otherPeriodDate = (Date) simpleDateFormat
							.parse(main.dateEndString);
				} catch (ParseException e1) {
					otherPeriodDate = date;
				}

				DateContainer correctDateContainer = getCorrectDates(
						new DateContainer(date, otherPeriodDate), true);

				main.dateBeginString = simpleDateFormat
						.format(correctDateContainer.date1);
				main.dateEndString = simpleDateFormat
						.format(correctDateContainer.date2);

			} else if (viewID == R.id.dateEnd) {

				try {
					otherPeriodDate = (Date) simpleDateFormat
							.parse(main.dateBeginString);
				} catch (ParseException e1) {
					otherPeriodDate = date;
				}

				DateContainer correctDateContainer = getCorrectDates(
						new DateContainer(otherPeriodDate, date), false);

				main.dateBeginString = simpleDateFormat
						.format(correctDateContainer.date1);
				main.dateEndString = simpleDateFormat
						.format(correctDateContainer.date2);
			}

			// ��������� �������� �� ���.
			EditText dateBeginEditText = (EditText) main
					.findViewById(R.id.dateBegin);
			dateBeginEditText.setText(main.dateBeginString);

			EditText dateEndEditText = (EditText) main
					.findViewById(R.id.dateEnd);
			dateEndEditText.setText(main.dateEndString);
		}

	};

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		if (savedInstanceState != null) {
			viewID = savedInstanceState.getInt(VIEW_ID);
		}

		datePickerDialog = new DatePickerDialog(getActivity(), listener, year,
				monthOfYear, dayOfMonth);

		return datePickerDialog;
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);

		bundle.putInt(VIEW_ID, viewID);
	}

	// ��������� �������������� ��, � ������� ������ ������ ������� ���������
	// ��������.
	public void setDestinationViewID(int viewID) {
		this.viewID = viewID;
	}

	// ��������� ����.
	public void updateDate(int year, int monthOfYear, int dayOfMonth) {

		if (year == 0 || monthOfYear < 0 || dayOfMonth == 0) {
			return;
		}

		this.year = year;
		this.monthOfYear = monthOfYear;
		this.dayOfMonth = dayOfMonth;

		if (datePickerDialog != null) {
			datePickerDialog.updateDate(this.year, this.monthOfYear,
					this.dayOfMonth);
		}
	}

	// ���������� ��������� ����, ������������ ��������.
	static public SimpleDateFormat getDateFormat() {
		return simpleDateFormat;
	}

	// ���������� �������� ������� ����.
	static public Date getCurrentDate() {

		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR) - 1900;
		int monthOfYear = c.get(Calendar.MONTH);
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

		Date date = new Date(year, monthOfYear, dayOfMonth);

		return date;
	}

	/**
	 * ��������� ������������ � ������������ ��, ���� ��� �����������: ��� ����
	 * �.�. ������� � date1 ������ ������ �������������� date2.
	 * 
	 * @param date1
	 *            ������ ����;
	 * @param date2
	 *            ��������� ����;
	 * @param takeFirstDateIfUnordered
	 *            ���� true, �� ���� date1 > date2, �� �������� date2 �����
	 *            ��������� �������� date1. ���� false, �� �������� date1 �����
	 *            ��������� �������� date2.
	 * @return DateContainer, ���������� ���������� ����.
	 */
	public DateContainer getCorrectDates(DateContainer dateContainer,
			boolean takeFirstDateIfUnordered) {

		Date date1 = dateContainer.date1;
		Date date2 = dateContainer.date2;

		if (date1.toString().isEmpty() && date2.toString().isEmpty()) {

			// ���� ��� ���� �� ���������, �� ��� ��� ��������������� ��������
			// ������� ����.
			date1 = getCurrentDate();
			date2 = date1;

		} else if (!date1.toString().isEmpty() && date2.toString().isEmpty()) {

			// ��������� ������ ������ ����. Ÿ �������� ��������������� ������
			// ����.
			date2 = date1;

		} else if (date1.toString().isEmpty() && !date2.toString().isEmpty()) {

			// ��������� ������ ������ ����. Ÿ �������� ��������������� ������
			// ����.
			date1 = date2;

		} else if (!date1.toString().isEmpty() && !date2.toString().isEmpty()) {

			// ��������� ��� ����. ����������� �� ������������ ������������ ����
			// �����.
			if (date1.after(date2)) {

				// ���� ���������� ���� ����, ��� ����� ����� ������ ���� ���
				// ��������������, �� � �������� ������������� ������ ����. �
				// ��������.
				if (takeFirstDateIfUnordered == true) {
					date2 = date1;
				} else {
					date1 = date2;
				}
			}
		}

		return new DateContainer(date1, date2);
	}
}
