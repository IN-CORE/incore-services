package ncsa.tools.common.exceptions;

public class InvalidStateException extends BaseCommonException
{
	private static final long serialVersionUID = 2012L;

	public InvalidStateException()
	{
		super();
	}

	public InvalidStateException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidStateException(String message)
	{
		super(message);
	}

	public InvalidStateException(Throwable cause)
	{
		super(cause);
	}
}
