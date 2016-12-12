package ncsa.tools.common.exceptions;

public class DeserializationException extends BaseCommonException
{
	private static final long serialVersionUID = 2002L;

	public DeserializationException()
	{
		super();
	}

	public DeserializationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DeserializationException(String message)
	{
		super(message);
	}

	public DeserializationException(Throwable cause)
	{
		super(cause);
	}
}
