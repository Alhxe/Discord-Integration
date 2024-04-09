package di.dicore.api;

import di.internal.controller.CoreController;
import di.internal.controller.InternalController;
import di.internal.entity.DiscordCommand;
import di.internal.entity.DiscordSlashCommand;

public interface DIApi {

    /**
     * @return The core controller.
     */
    CoreController getCoreController();

    /**
     * @return The internal controller of the plugin.
     */
    InternalController getInternalController();

    /**
     * Add a new event as listener.
     *
     * @param listener Discord Listener.
     */
    void registerDiscordEvent(Object listener);

    /**
     * Add a new command to command handler.
     *
     * @param command Discord command.
     */
    void registerDiscordCommand(DiscordCommand command);
     
    /**
     * Add a new slash command to command handler.
     *
     * @param command  Slash discord command.
     */
    void registerDiscordSlashCommand(DiscordSlashCommand command);

    /**
     * @return true if bungee is enabled. False if server is bungeecord or is bukkit and is bungeecord settings false.
     */
    boolean isBungeeDetected();

}
