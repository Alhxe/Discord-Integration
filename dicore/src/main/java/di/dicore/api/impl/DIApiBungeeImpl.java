package di.dicore.api.impl;

import di.dicore.BungeeApplication;
import di.dicore.api.DIApi;
import di.internal.controller.CoreController;
import di.internal.controller.InternalController;
import di.internal.controller.impl.InternalControllerBungeeImpl;
import di.internal.entity.DiscordCommand;
import di.internal.entity.DiscordSlashCommand;
import di.internal.exception.NoApiException;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Class used to communicate between the rest of the plugins.
 */
@Getter
public class DIApiBungeeImpl implements DIApi {

    /**
     * Core controller.
     */
    private final CoreController coreController;

    /**
     * Contains the plugin driver.
     */
    private final InternalController internalController;

    /**
     * Main Discord Integration Api. When this class is instantiated, the internal
     * controller of the core is obtained.
     *
     * @param plugin      Plugin from where it is instantiated. The goal is the
     *                    logger.
     * @param classLoader Class loader.
     * @param configFile  True if plugin has config file in DICore folder.
     * @param langFile    True if plugin has lang file in DICore folder.
     * @throws NoApiException In case the internal controller of the core is not
     *                        instantiated, it will throw an error.
     */
    public DIApiBungeeImpl(Plugin plugin, ClassLoader classLoader, boolean configFile, boolean langFile) throws NoApiException {
        if (BungeeApplication.getInternalController() == null) {
            throw new NoApiException(plugin.getLogger());
        }
        this.coreController = BungeeApplication.getInternalController();
        this.internalController = new InternalControllerBungeeImpl(plugin, coreController, classLoader, configFile, langFile);
        plugin.getLogger().info("DICore has successfully connected with " + plugin.getDescription().getName());

    }

    @Override
    public void registerDiscordEvent(Object listener) {
        this.coreController.getDiscordApi().get().addEventListener(listener);
    }

    @Override
    public void registerDiscordCommand(DiscordCommand command) {
        this.coreController.getBot().getCommandHandler().registerCommand(command);
    }
    
	@Override
	public void registerDiscordSlashCommand(DiscordSlashCommand command) {
		this.coreController.getBot().getSlashCommandHandler().registerCommand(command);
	}

    @Override
    public boolean isBungeeDetected() {
        return false;
    }
}
