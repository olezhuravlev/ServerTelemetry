package pro.got4.servertelemetry;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * ��������, �������������� �������� ��� ViewPager-�.
 * 
 * @author programmer
 * 
 */
public class PageFragment extends Fragment implements /* OnClickListener, */
Main.FragmentUpdateListener {

	static final int REQUEST_CODE_TEMPERATURE = 1;

	int pageNumber;

	private DataAdapter dataAdapter;

	static PageFragment newInstance(Bundle args) {

		// ��� �������� ���������� � ����������� ��������.
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

		// ��������� ���������� ������������ ���������.
		pageNumber = getArguments().getInt(Main.ARGUMENT_PAGE_NUMBER_FIELDNAME);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View fragmentView = null;

		switch (pageNumber) {
		case Main.PAGE_PREFS_INDEX: {

			Log.d(Main.TAG,
					"PageFragment.onCreateView(): Main.PAGE_PREFS_INDEX: ������ �� ������.");

			// ������ ��������� �� �����, �.�. �������� ��������� ������������
			// ����� ��������� ��������, ����������� ��������� ������.

			break;
		}
		case Main.PAGE_GRAPH_INDEX: {

			Log.d(Main.TAG,
					"PageFragment.onCreateView(): Main.PAGE_GRAPH_INDEX: �������� R.layout.graph, ��������� � Main ������ �� ����.");

			fragmentView = inflater.inflate(R.layout.graph, null);

			Main.graphFragmentView = fragmentView;

			Main.setGraph(getActivity());

			break;
		}
		case Main.PAGE_TABLE_INDEX: {

			Log.d(Main.TAG,
					"PageFragment.onCreateView(): Main.PAGE_TABLE_INDEX: ��������� ��������� �������, �������� R.layout.list, �������� DataAdapter � ���������� ��� ListView.");

			// TODO: ��������� ������!
			if (Main.dbAdapter != null) {
				Main.cursorCurrentDataBackward = Main.dbAdapter
						.fetchAllNotes(DatabaseAdapter.DATE_FIELD_NAME
								+ " DESC");
			}

			Log.d(Main.TAG,
					"PageFragment.onCreateView(): Main.PAGE_TABLE_INDEX: �������� ������ = "
							+ Main.cursorCurrentDataBackward.toString());

			if (Main.cursorCurrentDataBackward.getCount() == 0) {
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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(Main.TAG, "PageFragment.onActivityCreated()");
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(Main.TAG, "PageFragment.onStart()");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(Main.TAG, "PageFragment.onResume()");
	}

	// �������, ��������� FragmentUpdateListener. �������� �� ���������, �.�.
	// ���������� ������������ ������ ������������� ���������.
	@Override
	public void update(Bundle bundle) {

		if (pageNumber == Main.PAGE_PREFS_INDEX) {

			// ���������� ��������� ��������.

		} else if (pageNumber == Main.PAGE_GRAPH_INDEX) {

			// ���������� ��������� � ��������.

		} else if (pageNumber == Main.PAGE_TABLE_INDEX) {

			// ���������� ��������� � ��������.
		}
	}
}
