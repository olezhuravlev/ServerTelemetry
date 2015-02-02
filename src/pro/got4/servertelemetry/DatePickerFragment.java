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
 * Диалог для ввода даты.
 * 
 * @author programmer
 * 
 */
public class DatePickerFragment extends DialogFragment {

	static final String VIEW_ID = "view_id";

	// Идентификатор ЭУ, в который данный диалог должен установить свои данные.
	private int viewID;

	DatePickerDialog datePickerDialog;

	@SuppressLint("SimpleDateFormat")
	private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"dd.MM.yyyy");

	// Значения даты, которые будут установлены в диалог.
	private int year;
	private int monthOfYear;
	private int dayOfMonth;

	// Класс-контейнер для передачи дат.
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

	// Слушатель для установки даты в поле dateBegin.
	public OnDateSetListener listener = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			// Вью нужно найте заново, т.к. конфигурация устройства
			// м.б. изменена за время отображения диалога и того вью, из
			// которого вызывалось событие может уже не существовать.
			Main main = (Main) getActivity();

			Date date = new Date(year - 1900, monthOfYear, dayOfMonth);
			Date otherPeriodDate = null;

			// Требуется отдельно устанавливать значение члена класса и значение
			// в текстовом поле!

			// Установка значения в соответствующее поле активности.
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

			// Установка значений во вью.
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

	// Установка идентификатора ЭУ, в который диалог должен вернуть выбранное
	// значение.
	public void setDestinationViewID(int viewID) {
		this.viewID = viewID;
	}

	// Установка даты.
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

	// Возвращает форматтер даты, используемый диалогом.
	static public SimpleDateFormat getDateFormat() {
		return simpleDateFormat;
	}

	// Возвращает значение текущей даты.
	static public Date getCurrentDate() {

		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR) - 1900;
		int monthOfYear = c.get(Calendar.MONTH);
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

		Date date = new Date(year, monthOfYear, dayOfMonth);

		return date;
	}

	/**
	 * Проверяет корректность и корректирует их, если они некорректны: обе даты
	 * д.б. указаны и date1 всегда должна предшествовать date2.
	 * 
	 * @param date1
	 *            первая дата;
	 * @param date2
	 *            следующая дата;
	 * @param takeFirstDateIfUnordered
	 *            если true, то если date1 > date2, то значению date2 будет
	 *            присвоено значение date1. Если false, то значению date1 будет
	 *            присвоено значение date2.
	 * @return DateContainer, содержащий корректные даты.
	 */
	public DateContainer getCorrectDates(DateContainer dateContainer,
			boolean takeFirstDateIfUnordered) {

		Date date1 = dateContainer.date1;
		Date date2 = dateContainer.date2;

		if (date1.toString().isEmpty() && date2.toString().isEmpty()) {

			// Если обе даты не заполнены, то для них устанавливается значение
			// текущей даты.
			date1 = getCurrentDate();
			date2 = date1;

		} else if (!date1.toString().isEmpty() && date2.toString().isEmpty()) {

			// Заполнена только первая дата. Её значение устанавливается второй
			// дате.
			date2 = date1;

		} else if (date1.toString().isEmpty() && !date2.toString().isEmpty()) {

			// Заполнена только вторая дата. Её значение устанавливается первой
			// дате.
			date1 = date2;

		} else if (!date1.toString().isEmpty() && !date2.toString().isEmpty()) {

			// Заполнены обе даты. Проверяется их корректность относительно друг
			// друга.
			if (date1.after(date2)) {

				// Если установлен флаг того, что нужно брать первую дату для
				// упорядочивания, то её значение присваивается второй дате. И
				// наоборот.
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
