package ncsa.tools.common.concurrent;

public abstract class ExceptionPreservingThread extends Thread
{
	protected Throwable thrown;

	public Throwable getException()
	{
		return thrown;
	}
}
