package pro.got4.servertelemetry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Наблюдатель, принимающий уведомления об изменении данных. Наблюдатель м.б.чем
 * угодно до тех пор, лишь бы он был в состоянии определить изменения
 * содержимого и сообщить их загрузчику путем вызова onContentChanged().
 */
public class DataObserver extends BroadcastReceiver {

	private DataLoader loader;

	public DataObserver(DataLoader loader) {

		this.loader = loader;
	}

	// Вызвается при получении сообщений.
	@Override
	public void onReceive(Context context, Intent intent) {

		// Log.d(Main.TAG, "DataObserver.onReceive()");

		// Сообщаем загрузчику, что данные изменились.
		this.loader.onContentChanged();
	}
}
