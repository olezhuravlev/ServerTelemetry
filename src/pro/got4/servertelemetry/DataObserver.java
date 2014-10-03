package pro.got4.servertelemetry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * �����������, ����������� ����������� �� ��������� ������. ����������� �.�.���
 * ������ �� ��� ���, ���� �� �� ��� � ��������� ���������� ���������
 * ����������� � �������� �� ���������� ����� ������ onContentChanged().
 */
public class DataObserver extends BroadcastReceiver {

	private DataLoader loader;

	public DataObserver(DataLoader loader) {

		this.loader = loader;
	}

	// ��������� ��� ��������� ���������.
	@Override
	public void onReceive(Context context, Intent intent) {

		// Log.d(Main.TAG, "DataObserver.onReceive()");

		// �������� ����������, ��� ������ ����������.
		this.loader.onContentChanged();
	}
}
