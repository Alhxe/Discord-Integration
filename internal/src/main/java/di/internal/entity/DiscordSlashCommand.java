package di.internal.entity;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Represents a Discord Slash Command.
 */
public interface DiscordSlashCommand {

    /**
     * Executes the slash command.
     * 
     * @param event The event triggered when the command is received.
     */
    void execute(SlashCommandInteractionEvent event);

    /**
     * Retrieves the alias of the command.
     * 
     * @return The alias of the command.
     */
    String getAlias();
}
