package ncsa.tools.common.exceptions;

public class InitializationException extends BaseCommonException
{
	private static final long serialVersionUID = 2011L;

	public InitializationException()
	{
		super();
	}

	public InitializationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InitializationException(String message)
	{
		super(message);
	}

	public InitializationException(Throwable cause)
	{
		super(cause);
	}

}
