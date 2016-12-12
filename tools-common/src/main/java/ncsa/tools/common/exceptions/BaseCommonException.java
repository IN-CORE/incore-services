package ncsa.tools.common.exceptions;

import java.io.Serializable;

public class BaseCommonException extends Exception implements Serializable
{
	private static final long serialVersionUID = 1031L;

	public BaseCommonException()
	{
		super();
	}

	public BaseCommonException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public BaseCommonException(String message)
	{
		super(message);
	}

	public BaseCommonException(Throwable cause)
	{
		super(cause);
	}
}
