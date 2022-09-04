package di.internal.controller;

import di.internal.entity.DiscordBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public interface CoreController extends BasicController {
    JDA getDiscordApi();

    Guild getGuild();

    DiscordBot getBot();
}
