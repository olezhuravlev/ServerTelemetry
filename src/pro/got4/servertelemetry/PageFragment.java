package pro.got4.servertelemetry;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class PageFragment extends Fragment implements /* OnClickListener, */
Main.FragmentUpdateListener {

	static final int REQUEST_CODE_TEMPERATURE = 1;

	int pageNumber;

	private DataAdapter dataAdapter;

	static PageFragment newInstance(Bundle args) {

		// Для передачи параметров в создаваемый фрагмент.
		Bundle arguments = new Bundle();
		arguments.putInt(Main.ARGUMENT_PAGE_NUMBER_FIELDNAME,
				args.getInt(Main.ARGUMENT_PAGE_NUMBER_FIELDNAME));

		PageFragment pageFragment = new PageFragment();
		pageFragment.setArguments(arguments);

		return pageFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Получение параметров создаваемого фрагмента.
		pageNumber = getArguments().getInt(Main.ARGUMENT_PAGE_NUMBER_FIELDNAME);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View fragmentView = null;

		switch (pageNumber) {
		case Main.PAGE_PREFS_INDEX: {

			// Ничего создавать не нужно, т.к. страница установок представляет
			// собой отдельный фрагмент, реализующий слушателя жестов.

			break;
		}
		case Main.PAGE_GRAPH_INDEX: {

			// Если страница пересоздана, то значит она уже обновлена.
			Main.updateGraph = false;

			fragmentView = inflater.inflate(R.layout.graph, null);

			Main.graphFragmentView = fragmentView;

			break;
		}
		case Main.PAGE_TABLE_INDEX: {

			if (Main.cursorCurrentDataBackward == null
					|| Main.cursorCurrentDataBackward.isClosed()) {
				fragmentView = inflater.inflate(R.layout.no_data, null);
			} else {

				fragmentView = inflater.inflate(R.layout.list, null);

				dataAdapter = new DataAdapter(getActivity(),
						Main.cursorCurrentDataBackward);

				ListView listView = (ListView) fragmentView
						.findViewById(R.id.temperListView);
				listView.setAdapter(dataAdapter);
			}

			break;
		}
		default: {
			fragmentView = inflater.inflate(R.layout.no_data, null);
		}
		}

		return fragmentView;
	}

	// Функция, требуемая FragmentUpdateListener. Действие не выполняет, т.к.
	// обновление производится полным пересозданием фрагмента.
	@Override
	public void update(Bundle bundle) {

		if (pageNumber == Main.PAGE_PREFS_INDEX) {

			// Обновление фрагмента настроек.

		} else if (pageNumber == Main.PAGE_GRAPH_INDEX) {

			// Обновление фрагмента с графиком.

		} else if (pageNumber == Main.PAGE_TABLE_INDEX) {

			// Обновление фрагмента с таблицей.
		}
	}
}
