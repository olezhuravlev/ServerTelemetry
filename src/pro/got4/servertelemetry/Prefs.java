package pro.got4.servertelemetry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * ��������, �������������� �������� ��������.
 * 
 * @author programmer
 * 
 */
public class Prefs extends Fragment implements
		GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener,
		OnClickListener, AddressDialog.AddressDialogListener {

	private boolean timerOn = false;
	private int tapsMade = 0;
	private int tapsNeeded = 5;
	private long timeDelayMills = 5000;

	private Button buttGetData;
	private Button dateButt;
	private EditText sourceServerPath;
	private EditText dateBeginEditText;
	private EditText dateEndEditText;

	DialogFragment dialogFragment;
	GestureDetectorCompat gDetector;
	Handler timerHandler = new Handler();

	public static final int ADDRESS_DIALOG = 0;
	public static final String ADDRESS_DIALOG_REQUEST_CODE = "address_dialog_request_code";

	Runnable timerRunnable = new Runnable() {

		@Override
		public void run() {

			// �� ������������ ������� ������� ������� ������������.
			tapsMade = 0;
			timerOn = false;
			timerHandler.removeCallbacks(timerRunnable);
		}

	};

	/**
	 * ��������� ��������� � ��������� �����.
	 */
	public TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			// ��������� �������� � ��������������� ���� ����������.
			Main main = (Main) getActivity();
			main.sourceServerPathString = s.toString();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);

		Main main = (Main) getActivity();

		View fragmentView = inflater.inflate(R.layout.prefs, container, false);

		sourceServerPath = (EditText) fragmentView
				.findViewById(R.id.sourceServerPath);
		sourceServerPath.setText(main.sourceServerPathString);
		sourceServerPath.addTextChangedListener(textWatcher);

		sourceServerPath.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				sourceServerPath.setFocusable(true);
				sourceServerPath.setFocusableInTouchMode(true);

				gDetector.onTouchEvent(event);

				return false;
			}
		});

		// ������������ ������ ��������� ��� ����, ����� ��� ���������
		// ������������ ������ �� ����������� ��������������� �� ���� ���� �
		// ��������������� ����������� ����������� ����������.
		sourceServerPath.setFocusable(false);
		sourceServerPath.setFocusableInTouchMode(false);

		dateBeginEditText = (EditText) fragmentView
				.findViewById(R.id.dateBegin);
		dateBeginEditText.setText(main.dateBeginString);
		dateBeginEditText.setOnClickListener(this);

		dateEndEditText = (EditText) fragmentView.findViewById(R.id.dateEnd);
		dateEndEditText.setText(main.dateEndString);
		dateEndEditText.setOnClickListener(this);

		dateButt = (Button) fragmentView.findViewById(R.id.dateButt);
		dateButt.setOnClickListener(this);

		buttGetData = (Button) fragmentView.findViewById(R.id.buttGetData);
		buttGetData.setOnClickListener(this);

		this.gDetector = new GestureDetectorCompat(getActivity(), this);

		return fragmentView;
	}

	@Override
	public void onClick(final View v) {

		switch (v.getId()) {
		case R.id.sourceServerPath: {
			break;
		}
		case R.id.buttGetData: {

			Main main = (Main) getActivity();
			Bundle args = new Bundle();

			SimpleDateFormat simpleDateFormat = DatePickerFragment
					.getDateFormat();

			args.putString(Main.SOURCE_SERVER_PATH_FIELDNAME,
					main.sourceServerPathString);
			args.putString(Main.DATE_BEGIN_FIELDNAME, main.dateBeginString);
			args.putString(Main.DATE_END_FIELDNAME, main.dateEndString);
			args.putString(Main.DATE_PATTERN_FIELDNAME,
					simpleDateFormat.toPattern());

			// ������� �������.
			Main.dbAdapter.truncateTable(DatabaseAdapter.DATABASE_TABLE_NAME);

			// ��������� ����� ��� ������������� ������������ ��������� � ����
			// ���������, ������������ �������� ����� ��� ����, � (����
			// ����������/�������� ��������) ��������� ��������. ���� ���������
			// � ���� �� ��������������� ��� �������������� �������, �� ��
			// ������������� ������������, ����� ����� ��������� ��������� ����
			// ������. �������� ����� ����� ���������� �� ����, ��� ������
			// ��������� ������������.
			// ���������:
			// id - ���������� ������������� ��� ����� ����������. ����� ����
			// �����. �������������� ������ � ���������� ����������
			// LoaderManager.
			// args - ��������� ��� ��������� ���������� ��� ��������.
			// main - ��������� ��������� ������, ������� ����� ������
			// LoaderManager ��� ��������� �� ���������� ��������� ����������.
			// ����������.
			getActivity().getSupportLoaderManager().restartLoader(
					Main.LOADER_ID, args, main);

			break;
		}
		case R.id.dateBegin: {

			DatePickerFragment datePickerFragment = new DatePickerFragment();
			datePickerFragment.setDestinationViewID(R.id.dateBegin);

			Date date = getDateFromEditText((EditText) v,
					DatePickerFragment.getDateFormat());

			datePickerFragment.updateDate(date.getYear() + 1900,
					date.getMonth(), date.getDate());

			datePickerFragment.show(getFragmentManager(), null);

			break;
		}
		case R.id.dateEnd: {

			DatePickerFragment datePickerFragment = new DatePickerFragment();
			datePickerFragment.setDestinationViewID(R.id.dateEnd);

			Date date = getDateFromEditText((EditText) v,
					DatePickerFragment.getDateFormat());

			datePickerFragment.updateDate(date.getYear() + 1900,
					date.getMonth(), date.getDate());

			datePickerFragment.show(getFragmentManager(), null);

			break;
		}
		case R.id.dateButt: {

			Main main = (Main) getActivity();

			Calendar calendar = Calendar.getInstance();

			int dayOfMonth = calendar.get(Calendar.DATE);
			int monthOfYear = calendar.get(Calendar.MONTH);
			int year = calendar.get(Calendar.YEAR) - 1900;

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"dd.MM.yyyy", Locale.getDefault());
			Date dateEnd = new Date(year, monthOfYear, dayOfMonth);
			String dateEndString = simpleDateFormat.format(dateEnd);

			// ��������� �������� ������������� �������� ����� ������ � ��������
			// � ��������� ����!
			main.dateEndString = dateEndString;
			dateEndEditText.setText(dateEndString);

			calendar.setTime(dateEnd);
			calendar.add(Calendar.DATE, -1);

			dayOfMonth = calendar.get(Calendar.DATE);
			monthOfYear = calendar.get(Calendar.MONTH);
			year = calendar.get(Calendar.YEAR) - 1900;

			Date dateBegin = new Date(year, monthOfYear, dayOfMonth);
			String dateBeginString = simpleDateFormat.format(dateBegin);

			// ��������� �������� ������������� �������� ����� ������ � ��������
			// � ��������� ����!
			main.dateBeginString = dateBeginString;
			dateBeginEditText.setText(dateBeginString);

			Toast.makeText(main,
					main.getResources().getString(R.string.setDagePeriod),
					Toast.LENGTH_SHORT).show();

			break;
		}

		}
	}

	@Override
	public boolean onDoubleTap(MotionEvent arg0) {
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {

		if (e.getAction() == MotionEvent.ACTION_UP) {

			// ���� ������ ��� �� �������, �� �� ����������.
			if (!timerOn) {

				tapsMade = 0;
				timerOn = true;

				timerHandler.postDelayed(timerRunnable, timeDelayMills);
			}

			++tapsMade;

			if (tapsMade >= tapsNeeded) {

				// ������ �������, ����� ��������, ����������� �������.
				timerHandler.removeCallbacks(timerRunnable);

				tapsMade = 0;
				timerOn = false;

				// ������������� DialogFragment:
				// ������������ ������������� DialogFragment ������� � ���, ���
				// ���� ��� ��������� ���� ������������� ���������� �����
				// ���������� � ������� ����� ���� ����� ���������.
				// ������� �������� ���������� ��������� ����, ������� �� �����
				// ���� ����� �������, ��������, ��� ������������������, ���� ��
				// � ���� ������ �����������.
				AddressDialog dialog = new AddressDialog();
				dialog.setData(getResources().getStringArray(
						R.array.exampleURIs));

				// ����� ����� �� ��������� ����� ����� �������
				// getTargetFragment � �������� ������ �� ������ ��������.
				dialog.setTargetFragment(this, ADDRESS_DIALOG);
				dialog.show(getFragmentManager(), ADDRESS_DIALOG_REQUEST_CODE);
			}
		}

		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent arg0) {
		return false;
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		return false;
	}

	// ���������� ����������, ������������ � AddressDialog.
	@Override
	public void onAddressDialogItemSelected(AddressDialog addressDialog,
			DialogInterface dialog, int which) {

		String[] data = addressDialog.getData();
		sourceServerPath.setText(data[which]);

	}

	/**
	 * ���������� �������� ����, ���������� � ������, ��������� ���������
	 * ��������������.
	 * 
	 * @param editText
	 * @param simpleDateFormat
	 * @return
	 */
	public Date getDateFromEditText(EditText editText,
			SimpleDateFormat simpleDateFormat) {

		// ���� � ��������, �� �������� ��� ������ ������ ��� �����������
		// �������� ����, �� ��� �������� ����� ����������� � ����� �������.
		// � ��������� ������ ����� ����������� �������� ������� ����.

		Date date = null;
		try {

			String dateString = editText.getText().toString();

			date = simpleDateFormat.parse(dateString);

		} catch (ParseException e) {

			date = DatePickerFragment.getCurrentDate();
		}

		return date;
	}

}
