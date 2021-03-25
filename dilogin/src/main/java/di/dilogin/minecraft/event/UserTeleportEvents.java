package di.dilogin.minecraft.event;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.entity.UserData;
import di.dilogin.minecraft.controller.UserDataController;
import di.dilogin.minecraft.event.custom.DILoginEvent;
import di.internal.controller.file.ConfigManager;

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

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!isTeleportEnabled)
			return;

		String uuid = event.getPlayer().getUniqueId().toString();
		if (UserDataController.isFilePresent(uuid))
			return;

		UserDataController.saveUserData(event.getPlayer());
		if (teleportLocation.isPresent())
			event.getPlayer().teleport(teleportLocation.get());
	}

	@EventHandler
	public void onPlayerLogin(DILoginEvent event) {
		if (!isTeleportEnabled)
			return;

		String uuid = event.getPlayer().getUniqueId().toString();
		if (!UserDataController.isFilePresent(uuid))
			return;

		Optional<UserData> optUserData = UserDataController.getUserDataFromUuid(uuid);
		if (!optUserData.isPresent())
			return;

		UserData userData = optUserData.get();
		Location location = userData.asLocation();
		event.getPlayer().teleport(location);
		UserDataController.removeFile(uuid);
	}

	private Optional<Location> getTeleportLocation() {
		if (isTeleportEnabled) {
			ConfigManager confManager = api.getInternalController().getConfigManager();
			Double x = Double.parseDouble(confManager.getString("teleport_x"));
			Double y = Double.parseDouble(confManager.getString("teleport_y"));
			Double z = Double.parseDouble(confManager.getString("teleport_z"));
			String worldName = confManager.getString("teleport_world");
			Float yaw = Float.parseFloat(confManager.getString("teleport_yaw"));
			Float pitch = Float.parseFloat(confManager.getString("teleport_pitch"));

			Optional<World> world = Optional
					.ofNullable(api.getInternalController().getPlugin().getServer().getWorld(worldName));

			if (!world.isPresent())
				throw new IllegalArgumentException("No world named " + worldName);

			return Optional.of(new Location(world.get(), x, y, z, yaw, pitch));
		} 
		return Optional.empty();
	}
}
