package di.dilogin.minecraft.bukkit.event;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import di.dicore.api.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.entity.UserData;
import di.dilogin.minecraft.controller.UserDataController;
import di.dilogin.minecraft.bukkit.event.custom.DILoginEvent;
import di.internal.controller.file.ConfigManager;

import javax.swing.text.html.Option;

public class UserTeleportEvents implements Listener {

	/**
	 * Main api.
	 */
	private final DIApi api = BukkitApplication.getDIApi();

	/**
	 * Teleport condition.
	 */
	private final boolean isTeleportEnabled = api.getInternalController().getConfigManager().getBoolean("teleport");

	/**
	 * Teleport location.
	 */
	private final Optional<Location> teleportLocation = getTeleportLocation();

	/**
	 * Main player join event body.
	 * @param event It is the object that includes the event information.
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!isTeleportEnabled)
			return;

		String uuid = event.getPlayer().getUniqueId().toString();
		if (UserDataController.isFilePresent(uuid))
			return;

		UserDataController.saveUserData(event.getPlayer());
		teleportLocation.ifPresent(location -> event.getPlayer().teleport(location));
	}

	/**
	 * Main player login event body.
	 * @param event It is the object that includes the event information.
	 */
	@EventHandler
	public void onPlayerLogin(DILoginEvent event) {
		if (!isTeleportEnabled)
			return;

		Optional<Player> playerOptional = event.getUser().getPlayerBukkit();
		if(!playerOptional.isPresent())
			return;

		Player player = playerOptional.get();

		String uuid = player.getUniqueId().toString();
		if (!UserDataController.isFilePresent(uuid))
			return;

		Optional<UserData> optUserData = UserDataController.getUserDataFromUuid(uuid);
		if (!optUserData.isPresent())
			return;

		UserData userData = optUserData.get();
		Location location = userData.asLocation();
		player.teleport(location);
		UserDataController.removeFile(uuid);
	}

	/**
	 * Get the teleport location.
	 * @return Optional<Location>
	 */
	private Optional<Location> getTeleportLocation() {
		if (isTeleportEnabled) {
			ConfigManager confManager = api.getInternalController().getConfigManager();
			double x = Double.parseDouble(confManager.getString("teleport_x"));
			double y = Double.parseDouble(confManager.getString("teleport_y"));
			double z = Double.parseDouble(confManager.getString("teleport_z"));
			String worldName = confManager.getString("teleport_world");
			float yaw = Float.parseFloat(confManager.getString("teleport_yaw"));
			float pitch = Float.parseFloat(confManager.getString("teleport_pitch"));

			Optional<World> world = Optional
					.ofNullable(BukkitApplication.getPlugin().getServer().getWorld(worldName));

			if (!world.isPresent())
				throw new IllegalArgumentException("No world named " + worldName);

			return Optional.of(new Location(world.get(), x, y, z, yaw, pitch));
		} 
		return Optional.empty();
	}
}
