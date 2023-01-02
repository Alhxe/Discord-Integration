package di.dilogin.minecraft.bukkit.ext.luckperms;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import di.dilogin.BukkitApplication;
import di.dilogin.controller.MainController;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.node.Node;

/**
 * Ejemplo from LuckPerms plugin.
 */
public class LuckPermsEvents {

	/**
	 * Main plugin instance.
	 */
	private final Plugin plugin = BukkitApplication.getPlugin();

	/**
	 * Declare events and register them.
	 */
	public LuckPermsEvents() {
		LuckPerms api = LuckPermsController.getApi();

		EventBus eventBus = api.getEventBus();

		if (MainController.getDILoginController().isSyncroRolEnabled()) {
			eventBus.subscribe(this.plugin, NodeAddEvent.class, this::addRole);
			eventBus.subscribe(this.plugin, NodeClearEvent.class, this::clearRoles);
			eventBus.subscribe(this.plugin, NodeRemoveEvent.class, this::removeRole);
		}
	}

	/**
	 * Event handler for NodeRemoveEvent.
	 * @param event It is the object that includes the event information.
	 */
	private void removeRole(NodeRemoveEvent event) {
		String playerName = event.getTarget().getFriendlyName();
		Player player = plugin.getServer().getPlayer(playerName);

		if (player == null)
			return;

		internalRemoveRole(player, event.getNode());
	}

	/**
	 * Event handler for NodeClearEvent.
	 * @param event It is the object that includes the event information.
	 */
	private void clearRoles(NodeClearEvent event) {
		String playerName = event.getTarget().getFriendlyName();
		Player player = plugin.getServer().getPlayer(playerName);

		if (player == null)
			return;

		for (Node node : event.getNodes()) {
			internalRemoveRole(player, node);
		}

	}

	/**
	 * Remove role from player.
	 * @param player player to remove role.
	 * @param node role info.
	 */
	private void internalRemoveRole(Player player, Node node) {
		String group = node.getKey().replace("group.", "");
		List<String> roleList = LuckPermsController.getDiscordRoleFromMinecraftRole(group);

		for (String role : roleList) {
			if (!MainController.getDiscordController().serverHasRole(role))
				continue;

			if (MainController.getDiscordController().userHasRole(role, player.getName())) {
				MainController.getDiscordController().removeRole(role, player.getName(), "Stop being part of the " + group + " group");
			}

		}
	}

	/**
	 * Add role to player.
	 * @param event It is the object that includes the event information.
	 */
	private void addRole(NodeAddEvent event) {
		String playerName = event.getTarget().getFriendlyName();
		Player player = plugin.getServer().getPlayer(playerName);

		if (player == null)
			return;

		String group = event.getNode().getKey().replace("group.", "");
		List<String> roleList = LuckPermsController.getDiscordRoleFromMinecraftRole(group);
		for (String role : roleList) {
			if (!MainController.getDiscordController().serverHasRole(role))
				continue;

			if (!MainController.getDiscordController().userHasRole(role, player.getName())) {
				MainController.getDiscordController().giveRole(role, player.getName(), "Belong to the " + group + " group");
			}

		}
	}
}
