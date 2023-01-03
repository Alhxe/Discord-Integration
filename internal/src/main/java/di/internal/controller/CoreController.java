package di.internal.controller;

import di.internal.entity.DiscordBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Optional;

public interface CoreController extends BasicController {

    Optional<JDA> getDiscordApi();

    Optional<Guild> getGuild();

    /**
     * Gets the bot config.
     *
     * @return the bot config
     */
    DiscordBot getBot();

    /**
     * In case the bot is in bungeeCord, we set its information. This method does not change the bot's configuration!
     *
     * @param prefix   Bot prefix.
     * @param serverId Main server id.
     */
    void setBotInfo(String prefix, long serverId);
}
