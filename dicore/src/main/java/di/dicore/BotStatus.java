package di.dicore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import di.internal.controller.CoreController;
import net.dv8tion.jda.api.entities.Activity;

public class BotStatus implements Listener {

	private static final CoreController controller = BukkitApplication.getInternalController();
	private static int type;
	private static String content;

	// Init bot presence system
	public static void init() {
		if (!controller.getConfigManager().contains("status_type")
				|| !controller.getConfigManager().contains("status_content"))
			return;

		type = controller.getConfigManager().getInt("status_type");
		content = controller.getConfigManager().getString("status_content");

		initEvents();

	}
	
	// Init events from type
	private static void initEvents() {
		if (type==1) {
			fire();
			controller.getPlugin().getServer().getPluginManager().registerEvents(new BotStatus(), controller.getPlugin());
		} else if (type==2) {
			fireOnTime();
		}
	}
	
	// Every 5 minutes
	private static void fireOnTime() {
		ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
		threadPool.scheduleWithFixedDelay(BotStatus::fire, 0, 5, TimeUnit.MINUTES);
	}
	
	// When player joins server
	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		fire();
	}
	
	// When player quit server
	@EventHandler
	 public void onPlayerQuit(PlayerQuitEvent event) {
		fire();
	}
	
	// Change presence
	private static void fire() {
		controller.getBot().getApi().getPresence().setActivity(Activity.playing(getContent()));
	}
	
	// Change content
	private static String getContent() {
		return content.replace("%minecraft_players%", String.valueOf(controller.getPlugin().getServer().getOnlinePlayers().size()));
	}

}
