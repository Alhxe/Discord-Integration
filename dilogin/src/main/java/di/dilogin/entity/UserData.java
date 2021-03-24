package di.dilogin.entity;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import di.dilogin.BukkitApplication;
import lombok.Getter;

/**
 * This class contains the necessary user data before login.
 */
@Getter
public class UserData {

	/**
	 * Main plugin.
	 */
	private static final Plugin plugin = BukkitApplication.getPlugin();

	/**
	 * The x-coordinate of this location.
	 */
	private double x;

	/**
	 * The y-coordinate of this new location.
	 */
	private double y;

	/**
	 * The z-coordinate of this location.
	 */
	private double z;

	/**
	 * The name of the world in which this location resides.
	 */
	private String world;

	/**
	 * The absolute rotation on the x-plane, in degrees.
	 */
	private float yaw;

	/**
	 * The absolute rotation on the y-plane, in degrees.
	 */
	private float pitch;

	/**
	 * @param location Player location.
	 */
	public UserData(Location location) {
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.world = location.getWorld().getName();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}

	/**
	 * @param x     The x-coordinate of this location.
	 * @param y     The y-coordinate of this new location.
	 * @param z     The z-coordinate of this location.
	 * @param world The name of the world in which this location resides.
	 * @param yaw   The absolute rotation on the x-plane, in degrees.
	 * @param pitch The absolute rotation on the y-plane, in degrees.
	 */
	public UserData(double x, double y, double z, String world, float yaw, float pitch) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	/**
	 * @return The location obtained with the user's data. In case the world does
	 *         not exist, it will take you to the spawn of the first world on the
	 *         list.
	 */
	public Location asLocation() {
		Optional<World> optWorld = getWorldByName();
		if (optWorld.isPresent())
			return new Location(optWorld.get(), x, y, z, yaw, pitch);

		return null;
	}

	/**
	 * @return Gets an optional that can contain the world of the location, if the
	 *         world exists.
	 */
	private Optional<World> getWorldByName() {
		return Optional.ofNullable(plugin.getServer().getWorld(world));
	}

}
