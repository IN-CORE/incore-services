package ncsa.tools.common.exceptions;

public class SerializationException extends BaseCommonException
{
	private static final long serialVersionUID = 2020L;

	public SerializationException()
	{
		super();
	}

	public SerializationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public SerializationException(String message)
	{
		super(message);
	}

	public SerializationException(Throwable cause)
	{
		super(cause);
	}
}
