package di.internal.controller;

import di.internal.controller.file.ConfigManager;
import di.internal.entity.DiscordBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.io.File;
import java.util.logging.Logger;

public interface CoreController {
    JDA getDiscordApi();

    Guild getGuild();

    Logger getLogger();

    void disablePlugin();

    File getDataFolder();

    DiscordBot getBot();

    ConfigManager getConfigManager();
}
