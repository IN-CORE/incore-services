package ncsa.tools.common.exceptions;

public class ReflectionException extends BaseCommonException
{
	private static final long serialVersionUID = 2015L;

	public ReflectionException()
	{
		super();
	}

	public ReflectionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ReflectionException(String message)
	{
		super(message);
	}

	public ReflectionException(Throwable cause)
	{
		super(cause);
	}
}
