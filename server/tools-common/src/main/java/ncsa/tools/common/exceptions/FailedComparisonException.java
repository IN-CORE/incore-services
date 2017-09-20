package ncsa.tools.common.exceptions;

public class FailedComparisonException extends BaseCommonException
{
	private static final long serialVersionUID = 2008L;

	public FailedComparisonException()
	{
		super();
	}

	public FailedComparisonException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public FailedComparisonException(String message)
	{
		super(message);
	}

	public FailedComparisonException(Throwable cause)
	{
		super(cause);
	}

}
