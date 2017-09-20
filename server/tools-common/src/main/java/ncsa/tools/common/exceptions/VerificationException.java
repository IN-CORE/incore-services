package ncsa.tools.common.exceptions;

public class VerificationException extends BaseCommonException
{
	private static final long serialVersionUID = 2023L;

	public VerificationException()
	{
		super();
	}

	public VerificationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public VerificationException(String message)
	{
		super(message);
	}

	public VerificationException(Throwable cause)
	{
		super(cause);
	}
}
