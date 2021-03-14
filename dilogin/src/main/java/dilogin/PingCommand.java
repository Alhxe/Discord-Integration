package dilogin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import utils.entity.DiscordCommand;

public class PingCommand implements DiscordCommand {

	@Override
	public void execute(String message, MessageReceivedEvent event) {
		event.getChannel().sendMessage("pong desde Login").submit();
	}

	@Override
	public String getAlias() {
		return "ping";
	}

}
