package ncsa.tools.common.exceptions;

public class RemoteConnectionException extends BaseCommonException
{
	private static final long serialVersionUID = 2016L;

	public RemoteConnectionException()
	{
		super();
	}

	public RemoteConnectionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public RemoteConnectionException(String message)
	{
		super(message);
	}

	public RemoteConnectionException(Throwable cause)
	{
		super(cause);
	}
}
