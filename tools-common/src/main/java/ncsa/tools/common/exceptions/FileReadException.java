package ncsa.tools.common.exceptions;

public class FileReadException extends BaseCommonException
{
	private static final long serialVersionUID = 2009L;

	public FileReadException()
	{
		super();
	}

	public FileReadException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public FileReadException(String message)
	{
		super(message);
	}

	public FileReadException(Throwable cause)
	{
		super(cause);
	}
}
