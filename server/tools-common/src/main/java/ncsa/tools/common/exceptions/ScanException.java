package ncsa.tools.common.exceptions;

public class ScanException extends BaseCommonException
{
	private static final long serialVersionUID = 2028L;

	public ScanException()
	{
		super();
	}

	public ScanException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ScanException(String message)
	{
		super(message);
	}

	public ScanException(Throwable cause)
	{
		super(cause);
	}

}
