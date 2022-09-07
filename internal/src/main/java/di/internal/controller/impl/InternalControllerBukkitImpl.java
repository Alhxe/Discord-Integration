package di.internal.controller.impl;

import java.io.File;
import java.util.logging.Logger;

import di.internal.controller.ChannelController;
import di.internal.controller.CoreController;
import di.internal.controller.InternalController;
import di.internal.controller.PluginController;
import di.internal.interceptor.ChannelBukkitInterceptor;
import org.bukkit.plugin.Plugin;

import di.internal.controller.file.ConfigManager;
import di.internal.controller.file.YamlManager;
import lombok.Getter;

/**
 * This controller is in charge of configuring and obtaining the default files
 * of the plugin.
 */
@Getter
public class InternalControllerBukkitImpl implements PluginController, InternalController {

    /**
     * The driver for the plugin configuration file.
     */
    private ConfigManager configManager;

    /**
     * The driver for the plugin lang file.
     */
    private YamlManager langManager;

    /**
     * The bukkit plugin.
     */
    private final Plugin plugin;

    /**
     * Path of the plugin folder.
     */
    private File dataFolder;

    /**
     * The ChannelController.
     */
    private final ChannelController channelController;

    /**
     * Main Class Constructor.
     *
     * @param plugin         Bukkit plugin.
     * @param coreController Core controller.
     * @param classLoader    Class loader.
     * @param configFile     True if plugin has config file in DICore folder.
     * @param langFile       True if plugin has lang file in DICore folder.
     */
    public InternalControllerBukkitImpl(Plugin plugin, CoreController coreController, ClassLoader classLoader, boolean configFile,
                                        boolean langFile) {
        this.plugin = plugin;

        if (configFile && langFile)
            this.dataFolder = getInternalPluginDataFolder(coreController);
        if (configFile)
            this.configManager = new ConfigManager(this, dataFolder, classLoader);
        if (langFile)
            this.langManager = new YamlManager(this, "lang.yml", dataFolder, classLoader);

        this.channelController = new ChannelControllerBukkitImpl(plugin);
    }

    /**
     * Gets the plugin folder located in the DI folder
     *
     * @param coreController Core controller.
     * @return Plugin folder.
     */
    private File getInternalPluginDataFolder(CoreController coreController) {
        String stringBuilder = coreController.getDataFolder().getAbsolutePath() + "/" + getPluginName();
        return new File(stringBuilder);
    }

    /**
     * @return Logger of plugin.
     */
    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    /**
     * Disable the plugin.
     */
    @Override
    public void disablePlugin() {
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }

    @Override
    public ChannelController getChannelController() {
        return this.channelController;
    }

    private String getPluginName() {
        return plugin.getName();
    }
}
