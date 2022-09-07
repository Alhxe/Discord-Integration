package di.internal.controller;

import di.internal.entity.DiscordBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Optional;

public interface CoreController extends BasicController {
    JDA getDiscordApi();

    Guild getGuild();

    /**
     * Gets the bot config.
     *
     * @return the bot config
     */
    DiscordBot getBot();

    /**
     * Initializes the bot.
     */
    void startBot();
}
