package ncsa.tools.common.exceptions;

public class FileWriteException extends BaseCommonException
{
	private static final long serialVersionUID = 2010L;

	public FileWriteException()
	{
		super();
	}

	public FileWriteException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public FileWriteException(String message)
	{
		super(message);
	}

	public FileWriteException(Throwable cause)
	{
		super(cause);
	}
}
