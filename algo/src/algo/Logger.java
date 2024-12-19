package algo;
/**
 * The Logger interface provides a simple logging mechanism.
 * It defines methods for logging error messages and general log messages.
 */
public interface Logger {
	void error(String msg);
	void log(String msg);
}
