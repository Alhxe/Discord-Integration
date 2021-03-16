package di.internal.entity;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Represents a Discord command.
 */
public interface DiscordCommand {

	/**
	 * Main body of a command
	 * 
	 * @param message It is the message that comes after the command.
	 * @param event   It is the object that includes the event information.
	 */
	void execute(String message, MessageReceivedEvent event);

	/**
	 * @return Alias of the command.
	 */
	String getAlias();
}
