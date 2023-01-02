package di.internal.controller.impl;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import di.internal.controller.ChannelController;
import di.internal.controller.CoreController;
import di.internal.controller.InternalController;
import di.internal.controller.PluginController;
import di.internal.controller.file.ConfigManager;
import di.internal.controller.file.YamlManager;
import di.internal.dto.Demand;
import di.internal.event.UserLoginForBungeeCallBukkitEvent;
import di.internal.utils.Util;
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
     * The CoreController.
     */
    private final CoreController coreController;

    /**
     * Main Class Constructor.
     *
     * @param plugin         Bukkit plugin.
     * @param coreController Core controller.
     * @param classLoader    Class loader.
     * @param configFile     True if plugin has config file in DICore folder.
     * @param langFile       True if plugin has lang file in DICore folder.
     * @param isDataInBungee True if plugin data is in BungeeCord.
     */
    public InternalControllerBukkitImpl(Plugin plugin, CoreController coreController, ClassLoader classLoader, boolean configFile,
                                        boolean langFile, boolean isDataInBungee) {
        this.plugin = plugin;
        this.coreController = coreController;

        if (configFile && langFile)
            this.dataFolder = getInternalPluginDataFolder(coreController);
        if (configFile)
            this.configManager = new ConfigManager(this, dataFolder, classLoader, isDataInBungee);
        if (langFile)
            this.langManager = new YamlManager(this, "lang.yml", dataFolder, classLoader, isDataInBungee);

        this.channelController = new ChannelControllerBukkitImpl(plugin);

    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public void disablePlugin() {
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }

    @Override
    public ChannelController getChannelController() {
        return this.channelController;
    }

    @Override
    public CompletableFuture<String> initConnectionWithBungee() {
        CompletableFuture<String> future = new CompletableFuture<>();
        UserLoginForBungeeCallBukkitEvent event = new UserLoginForBungeeCallBukkitEvent();
        plugin.getServer().getPluginManager().registerEvents(event, plugin);
        event.getFirstPlayer().whenCompleteAsync((player, throwable) -> {
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                String subChannel = Util.getRandomSubChannel(player.getName());
                channelController.sendMessageAndWaitResponseWithSubChannel(subChannel, player.getName(), Demand.checkConnection.name(), "")
                        .whenCompleteAsync((s, throwable1) -> {
                            Util.loadConfigFile(channelController, configManager, player.getName());
                            Util.loadLangFile(channelController, langManager, player.getName());
                            Util.updateBotInfo(channelController, coreController, player.getName());
                            future.complete(s);
                        });
            }, 20);
        });
        return future;
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
     * @return Plugin name.
     */
    private String getPluginName() {
        return plugin.getName();
    }
}
