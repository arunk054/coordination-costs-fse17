package cmu.isr.arunkaly;

public class MyLogger {

	public static LogLevel CURRENT_LOG_LEVEL = LogLevel.INFO;
	public static void setCurrentLogLevel(LogLevel level) {
		CURRENT_LOG_LEVEL = level;
	}
	
	public static void log(String message) {
		log(message,LogLevel.INFO);
	}
	public static void logError(String message) {
		log(message,LogLevel.ERROR);
	}

	public static void log(String message, LogLevel level) {
		if (level.ordinal() >= CURRENT_LOG_LEVEL.ordinal()) {
			System.out.println(message);
		}
	}
}
