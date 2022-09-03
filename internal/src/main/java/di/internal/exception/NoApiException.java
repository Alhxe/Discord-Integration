package di.internal.exception;

import java.util.logging.Logger;

/**
 * Exception generated when not finding the DIApi.
 */
public class NoApiException extends Exception{

	private static final long serialVersionUID = -8403248971778801419L;
	
	public NoApiException(Logger logger) {
		logger.warning("The bot API could not be found. Report this bug to the developers.");
	}

}
