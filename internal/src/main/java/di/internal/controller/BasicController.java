package di.internal.controller;

import di.internal.controller.file.ConfigManager;
import di.internal.controller.file.YamlManager;

import java.io.File;
import java.util.logging.Logger;

public interface BasicController {

    Logger getLogger();

    void disablePlugin();

    ConfigManager getConfigManager();

    YamlManager getLangManager();

    File getDataFolder();
}
