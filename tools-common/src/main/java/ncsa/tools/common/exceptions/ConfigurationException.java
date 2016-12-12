package ncsa.tools.common.exceptions;

public class ConfigurationException extends BaseCommonException
{
	private static final long serialVersionUID = 2000L;

	public ConfigurationException()
	{
		super();
	}

	public ConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ConfigurationException(String message)
	{
		super(message);
	}

	public ConfigurationException(Throwable cause)
	{
		super(cause);
	}
}
