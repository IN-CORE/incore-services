package ncsa.tools.common.exceptions;

public class CycleException extends BaseCommonException
{
	private static final long serialVersionUID = 2001L;

	public CycleException()
	{
		super();
	}

	public CycleException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public CycleException(String message)
	{
		super(message);
	}

	public CycleException(Throwable cause)
	{
		super(cause);
	}
}
