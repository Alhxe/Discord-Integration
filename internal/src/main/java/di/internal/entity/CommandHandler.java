package di.internal.entity;

import java.util.HashMap;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Represents the Discord command handler.
 */
public class CommandHandler extends ListenerAdapter {

	/**
	 * Command list.
	 */
	private final HashMap<String, DiscordCommand> commands = new HashMap<>();

	/**
	 * Bot prefix.
	 */
	private String prefix;

	/**
	 * Add new command to the list.
	 * 
	 * @param command The command to add.
	 */
	public void registerCommand(DiscordCommand command) {
		commands.put(command.getAlias(), command);
	}

	/**
	 * Main constructor of CommandHandler.
	 * 
	 * @param prefix Discord Bot prefix.
	 */
	public CommandHandler(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * It is in charge of receiving the message creation events and checks if it is
	 * a command that has been sent or not.
	 * 
	 * @param event Main event.
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {

		if (event.getAuthor().isBot())
			return;

		if (!event.getMessage().getContentDisplay().startsWith(prefix))
			return;

		String command = getFirstWord(prefix, event.getMessage().getContentDisplay());

		if (!commands.containsKey(command)) {
			if (commands.containsKey(command + " " + getSecondWord(event.getMessage().getContentDisplay()))) {
				command = command + " " + getSecondWord(event.getMessage().getContentDisplay());
			} else {
				return;
			}
		}
		try {
			commands.get(command)
					.execute(getMessageWithoutCommand(command, event.getMessage().getContentDisplay(), prefix), event);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the first word of message excluding the prefix.
	 * 
	 * @param prefix  Bot Prefix.
	 * @param message Message received.
	 * @return First message word without prefix.
	 */
	private static String getFirstWord(String prefix, String message) {
		if (!message.contains(" "))
			return message.substring(prefix.length()).toLowerCase();
		String p = message.substring(prefix.length());
		return p.split(" ")[0].toLowerCase();
	}

	/**
	 * Get the second word of message excluding the prefix.
	 * 
	 * @param message Message received.
	 * @return Second message word.
	 */
	private static String getSecondWord(String message) {
		return message.contains(" ") ? message.split(" ")[1].toLowerCase() : "";
	}

	/**
	 * Get the message by removing the command.
	 * 
	 * @param command Command used.
	 * @param message Message received.
	 * @param prefix  Bot Prefix.
	 * @return Message without command.
	 */
	private static String getMessageWithoutCommand(String command, String message, String prefix) {
		return message.length() == command.length() + prefix.length() ? ""
				: message.substring(command.length() + prefix.length() + 1);
	}

}