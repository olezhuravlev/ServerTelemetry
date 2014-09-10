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
 * Асинхронный загрузчик данных.
 */

// public class DataLoader extends AsyncTaskLoader<Cursor> {
public class DataLoader extends CursorLoader {

	public final static String BROADCAST_ACTION = "pro.got4.servertelemetry";

	private String uriString;
	private Cursor cursorCurrentData;

	// Пользовательский класс-наблюдатель.
	private DataObserver mObserver;

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public DataLoader(Context context) {
		// Загрузчики могут использоваться несколькими активностями
		// (предполагая, что они не связаны с LoadManager), поэтому НИКОГДА не
		// сохраняйте ссылку на контекст напрямую. В противном случае это
		// приведет к утечке всего контекста активности. Вместо этого
		// конструктор суперкласса сохранит ссылку на контекст
		// приложения, который может быть получен вызовом getContext().
		super(context);
	}

	public void setURIString(String uriString) {
		this.uriString = uriString;
	}

	// Субкласс должен реализовывать этот метод для загрузки данных методом
	// startLoading().
	// onStartLoading() не вызывается клиентом напрямую, его вызов является
	// результатом вызова метода startLoading().
	// Здесь реализуется поведение загрузчика в зависимости от его
	// состояния.
	@Override
	public void onStartLoading() {

		Log.d(Main.TAG,
				"DataLoader.onStartLoading(): если курсор существует, то его доставка. Создание экземпляра наблюдателя. Принудительный вызов загрузки, если обнаружено изменение контента.");

		// Если данные от предыдущей загрузки существуют, то их стоит доставить
		// немедленно.
		if (cursorCurrentData != null) {
			deliverResult(cursorCurrentData);
		}

		// Начало мониторинга изменения источника данных.
		if (mObserver == null) {

			mObserver = new DataObserver(this);

			// Регистрация на получаемые события.
			IntentFilter filter = new IntentFilter(BROADCAST_ACTION);
			getContext().registerReceiver(mObserver, filter);
		}

		// Когда наблюдатель обнаружит изменения, он должен вызвать метод
		// onContentChanged() загрузчикаLoader-а, который приведет к тому, что
		// следующий вызов метода takeContentChanged() вернет TRUE. В этом
		// случае мы инициируем новую загрузку.
		if (takeContentChanged() || cursorCurrentData == null) {
			forceLoad();
		}
	}

	@Override
	public void forceLoad() {

		Log.d(Main.TAG, "DataLoader.forceLoad()");

		// Принудительное выполнение асинхронной загрузки.
		// В отличие от startLoading() этот метод игнорирует предыдущие
		// загруженные данные и устанавливает загрузку новых
		// (это осуществляется просто через реализацию onForceLoad()).
		// В основном вы должны только вызывать этот метод только когда
		// загрузчик стартует, т.е. метод isStarted() возвращает TRUE.

		// Приводит к вызову события onForceLoad() (которое всегда срабатывает в
		// основном потоке процесса).

		super.forceLoad();
	}

	// Вызывается в рабочем потоке для выполнения текущей загрузки и возврата
	// результата загрузки. Реализации не должны доставлять результат напрямую,
	// но должны возвращать его из этого метода, который в конце концов
	// закончится вызовом deliverResult(D) в потоке интерфейса пользователя.
	// Если реализации требуют обработки результатов в потоке интерфейса, то они
	// могут перегружать deliverResult(D) и сделать это там. Чтобы поддерживать
	// отмену, это метод должен периодическе проверять значение функции
	// isLoadInBackgroundCanceled() и прерывать когда она возвращает ИСТИНА.
	// Субклассы также могут перегружать cancelLoadInBackground() чтобы
	// прерывать загрузку напрямую вместо опроса isLoadInBackgroundCanceled().
	// Когда загрузка отменена, этот метод может или завершиться нормально, или
	// выбросить OperationCanceledException. В другом случае загрузчик вызовет
	// onCanceled(D) чтобы выполнить очистку после отмены и распорядиться
	// результатом, в случае его наличия.
	@Override
	public Cursor loadInBackground() {

		Log.d(Main.TAG,
				"DataLoader.loadInBackground(): получение XML-данных, создание или открытие БД, загрузка данных в транзакции, создание и возвращение курсора на все записи.");

		// Выполнение асинхронной загрузки. Метод вызывается в фоновом потоке и
		// должен генерировать новый набор данных, которые д.б. возвращены
		// клиенту. Здесь следует выполнять запрос и добавлять результат в
		// данные.

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

						// Получение значений в виде строк.
						String dateValue = dateNode.getTextContent(); // 2014-01-01
																		// 12:00:00
						String tempValue = pointNode.getTextContent(); // 10.5

						// Приведение значений.
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

						// Сохранение в БД.
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

				// Возвращает курсор на первую строку данных всего содержимого
				// БД.
				Log.d(Main.TAG,
						"DataLoader.loadInBackground(): СОЗДАНИЕ КУРСОРА НА ВСЕ ЗАПИСИ!");
				cursor = Main.dbAdapter
						.fetchAllNotes(DatabaseAdapter.DATE_FIELD_NAME + " ASC");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return cursor;
	}

	// Отправляет результат загрузки зарегистрированному слушателю.
	@Override
	public void deliverResult(Cursor cursorNewData) {

		Log.d(Main.TAG,
				"DataLoader.deliverResult(): если загрузчик сброшен, то освобождение ресурсов и выход. Установка члену cursorData ссылки на новый курсор. Если загрузчик запущен, то доставка данных.");

		// Метод доставляет результат зарегистрированному слушателю.
		// Должен вызыватся только субклассом. Обязательно должен быть вызван из
		// основного потока процесса.

		// Вызывается, когда появляются новые данные для доставки клиенту.
		// Суперкласс доставит их зарегистрированному слушателю (т.е.
		// LoaderManager), который перешлет этот результат клиенту через вызов
		// onLoadFinished.

		// Если загрузчик был сброшен, то игрорируется результат и аннулируются
		// данные.
		if (isReset()) {
			releaseResources(cursorNewData);
			return;
		}

		// Сохранение ссылки на старые данные, чтобы они не были уничтожены
		// сборщиком мусора. Мы должны защитить эти данные до тех пор, пока не
		// будут получены новые данные.
		Cursor oldData = cursorCurrentData;
		cursorCurrentData = cursorNewData;

		// Если загрузчик в запущенном состоянии, происходит доставка данных
		// клиенту.
		// Это выполнит метод суперкласса.

		// deliverResult() отправляет результат загрузки зарегистрированному
		// слушателю.
		// Должен вызыватся только субклассом. Обязательно должен быть вызван из
		// основного потока процесса.
		if (isStarted()) {
			super.deliverResult(cursorNewData);
		}

		// Обнуление старых данных, т.к. они больше не нужны.
		// Метод releaseResources(Cursor) пуст, т.к. о фактическом удалении
		// данных заботится загрузчик.
		if (oldData != null && oldData != cursorNewData) {
			releaseResources(oldData);
		}

	}

	// Субкласс обязан реализовать это событие для того, чтобы обеспечить
	// остановку своего загрузчика. Это событие вызывается клиентом не
	// напрямую, а как результат вызова метода stopLoading().
	// Событие всегда вызывается из главного потока процесса.
	@Override
	public void onStopLoading() {

		Log.d(Main.TAG, "DataLoader.onStopLoading(): отмена текущей загрузки.");

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

		// Загрузчик находится в остановленном состоянии, поэтому нам следует
		// попытаться отменить текущую загрузку (если она существует).
		cancelLoad();

		// Мы оставляем наблюдателя без изменений. Загрузчики в остановленном
		// состоянии должны всё ещё отслеживать источник данных на предмет
		// изменений, поэтому если загрузчик будет запущен снова, то он будет
		// знать, что нужно выполнить новую загрузку.
	}

	// Субкласс обязан реализовывать этот метод для обеспечение сброса
	// своего загрузчика при вызове reset(). Событие вызывается клиентом
	// не напрямую, а как результат вызова reset(). Метод всегда вызывается
	// из главного потока процесса.
	@Override
	public void onReset() {

		Log.d(Main.TAG,
				"DataLoader.onReset(): вызов onStopLoading(), освобождение курсора, освобождение наблюдателя.");

		// Гарантируем, что загрузчик был остановлен.
		onStopLoading();

		// Загрузчик сбрасывается, поэтому нам следует остановить отслеживание
		// изменений.
		if (mObserver != null) {

			Log.d(Main.TAG, "DataLoader.onReset(): ОСВОБОЖДЕНИЕ НАБЛЮДАТЕЛЯ!");
			getContext().unregisterReceiver(mObserver);

			mObserver = null;
		}
	}

	/**
	 * Вспомогательный метод, обеспечивающий освобождение ресурсов,
	 * ассоциированных с активно загружаемым набором данных.
	 */
	private void releaseResources(Cursor cursor) {

		Log.d(Main.TAG, "DataLoader.releaseResources(): Освобождение курсора!");

		// Для простого списка делать ничего не нужно.
		// Но, например, для курсора нам следует закрыть его здесь.
		// Все ресурсы, ассоциированные с загрузчиком д.б. освобождены здесь.
		// Освобождаем ресурсы, ассоциированные с данными.
		if (cursor != null) {

			Log.d(Main.TAG,
					"DataLoader.releaseResources(): ОСВОБОЖДЕНИЕ КУРСОРА!");
			releaseResources(cursor);

			cursor = null;
		}
	}
}
