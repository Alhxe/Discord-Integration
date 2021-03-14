package utils.exception;

import org.bukkit.plugin.Plugin;

/**
 * Exception generated when not finding the DIApi.
 */
public class NoApiException extends Exception{

	private static final long serialVersionUID = -8403248971778801419L;
	
	public NoApiException(Plugin plugin) {
		plugin.getLogger().warning("The bot API could not be found. Report this bug to the developers.");
	}

}
