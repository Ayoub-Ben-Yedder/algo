package algo;
/**
 * The ErrorThrower abstract class implements the Logger interface
 * and provides a base for classes that need to handle error and log messages.
 */
public abstract class ErrorThrower  implements Logger{
	public abstract void error(String msg);
	public abstract void log(String msg);
}
