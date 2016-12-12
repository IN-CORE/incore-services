package ncsa.tools.common.exceptions;

public class NoSuchElementException extends BaseCommonException
{
	private static final long serialVersionUID = 2012L;

	public NoSuchElementException()
	{
		super();
	}

	public NoSuchElementException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public NoSuchElementException(String message)
	{
		super(message);
	}

	public NoSuchElementException(Throwable cause)
	{
		super(cause);
	}
}
