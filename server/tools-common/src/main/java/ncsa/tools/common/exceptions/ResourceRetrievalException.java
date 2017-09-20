package ncsa.tools.common.exceptions;

public class ResourceRetrievalException extends BaseCommonException
{
	private static final long serialVersionUID = 2017L;

	public ResourceRetrievalException()
	{
		super();
	}

	public ResourceRetrievalException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ResourceRetrievalException(String message)
	{
		super(message);
	}

	public ResourceRetrievalException(Throwable cause)
	{
		super(cause);
	}
}
