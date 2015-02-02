package pro.got4.servertelemetry;

public class Tracker1 {

	/**
	 * Флаг того, что используются версии функций с идентификаторами.
	 */
	private static boolean USE_ID = true;

	/**
	 * Флаг того, что используются версии функций без идентификаторов.
	 */
	private static boolean USE_NOT_ID = true;

	/**
	 * Идентификатор сообщений: только вызовы, имеющие это значение
	 * идентификатора будут отображаться. <br>
	 * Если == 0, то отображаются вызовы с любым значением идентификатора.
	 */
	private static int MESSAGE_ID = 0;

	static public void show() {

		if (USE_NOT_ID) {
			StackTraceElement ste = getTrace();
			System.out.println(ste);
		}
	}

	static public void show(int id) {

		if (USE_ID && (MESSAGE_ID == 0 || MESSAGE_ID == id)) {
			StackTraceElement ste = getTrace();
			System.out.println(ste);
		}
	}

	static public void show(String comment) {

		if (USE_NOT_ID) {
			StackTraceElement ste = getTrace();
			System.out.println(ste + " " + comment);
		}
	}

	static public void show(String comment, int id) {

		if (USE_ID && (MESSAGE_ID == 0 || MESSAGE_ID == id)) {
			StackTraceElement ste = getTrace();
			System.out.println(ste + " " + comment);
		}
	}

	static public void show(boolean flag) {

		if (USE_NOT_ID) {
			StackTraceElement ste = getTrace();
			System.out.println(ste + " [" + flag + "]");
		}
	}

	static public void show(boolean flag, int id) {

		if (USE_ID && (MESSAGE_ID == 0 || MESSAGE_ID == id)) {
			StackTraceElement ste = getTrace();
			System.out.println(ste + " [" + flag + "]");
		}
	}

	static public void show(Object obj) {

		if (USE_NOT_ID) {
			StackTraceElement ste = getTrace();
			System.out.println(ste + " [hashCode = " + obj.hashCode() + "]");
		}
	}

	static public void show(Object obj, int id) {

		if (USE_ID && (MESSAGE_ID == 0 || MESSAGE_ID == id)) {
			StackTraceElement ste = getTrace();
			System.out.println(ste + " [hashCode = " + obj.hashCode() + "]");
		}
	}

	private static StackTraceElement getTrace() {

		StackTraceElement[] steArr = Thread.currentThread().getStackTrace();
		return steArr[4];
	}
}
