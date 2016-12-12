package ncsa.tools.common.exceptions;

public class FailedCommandException extends BaseCommonException
{
	private static final long serialVersionUID = 2007L;

	public FailedCommandException()
	{
		super();
	}

	public FailedCommandException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public FailedCommandException(String message)
	{
		super(message);
	}

	public FailedCommandException(Throwable cause)
	{
		super(cause);
	}
}
