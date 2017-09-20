package ncsa.tools.common.exceptions;

public class AuthenticationException extends Exception
{
	private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

	private static final long serialVersionUID = 2030L;

	private String actualMessage;

	public AuthenticationException()
	{
		super();
	}

	public AuthenticationException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
		setActualMessage(arg0);
	}

	public AuthenticationException(String arg0)
	{
		super(arg0);
		setActualMessage(arg0);
	}

	public AuthenticationException(Throwable arg0)
	{
		super(arg0);
	}

	public void setActualMessage(String message)
	{
		logger.debug("Setting message to: " + message);

		this.actualMessage = message;
	}

	public String getActualMessage()
	{
		return actualMessage;
	}
}
