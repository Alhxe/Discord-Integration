package di.dicore.event;

import di.dicore.BungeeApplication;
import di.internal.controller.CoreController;
import net.dv8tion.jda.api.entities.Activity;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In this class, the flow of events related to the visible state of the bot in discord is controlled.
 */
public class BotStatusBungeeEvent implements Listener {

    /**
     * The internal controller of the core plugin.
     */
    private static final CoreController controller = BungeeApplication.getInternalController();

    /**
     * The configuration type of bot status selected in the file (status_type).
     */
    private static int type;

    /**
     * The configuration message of bot status selected in the file (status_content).
     */
    private static String content;

    /**
     * Bukkit plugin.
     */
    private static Plugin plugin;

    /**
     * Init the thread related to the configuration of the bot status events.
     */
    public static void init(Plugin p) {
        plugin = p;
        if (!controller.getConfigManager().contains("status_type")
                || !controller.getConfigManager().contains("status_content"))
            return;

        type = controller.getConfigManager().getInt("status_type");
        content = controller.getConfigManager().getString("status_content");

        initEvents();
    }

    /**
     * Init the events related to the configuration of the bot status.
     */
    private static void initEvents() {
        if (type == 1) {
            fire();
            plugin.getProxy().getPluginManager().registerListener(plugin, new BotStatusBungeeEvent());
        } else if (type == 2) {
            fireOnTime();
        }
    }

    /**
     * Fire the status of the bot every 5 mins.
     */
    private static void fireOnTime() {
        ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
        threadPool.scheduleWithFixedDelay(BotStatusBungeeEvent::fire, 0, 5, TimeUnit.MINUTES);
    }

    /**
     * Fire the status of the bot when player joins.
     *
     * @param e The event of player join.
     */
    @EventHandler
    public void onPostLogin(PostLoginEvent e) {
        fire();
    }

    /**
     * Fire the status of the bot.
     */
    private static void fire() {
        controller.getBot().getApi().getPresence().setActivity(Activity.playing(getContent()));
    }

    /**
     * Get the content of the status.
     *
     * @return The content of the status.
     */
    private static String getContent() {
        return content.replace("%minecraft_players%", String.valueOf(getOnlinePlayersCount()));
    }

    private static int getOnlinePlayersCount() {
        AtomicInteger result = new AtomicInteger();
        plugin.getProxy().getServers().forEach((name, server) -> result.addAndGet(server.getPlayers().size()));
        return result.get();
    }

}
