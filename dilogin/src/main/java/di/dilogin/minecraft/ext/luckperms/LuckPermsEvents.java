package di.dilogin.minecraft.ext.luckperms;

import java.util.List;

import di.dilogin.controller.MainController;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.node.Node;

/**
 * LuckPerms event.
 */
public class LuckPermsEvents {

	/**
	 * Declare events and register them.
	 */
	public LuckPermsEvents(Object plugin) {
		LuckPerms api = LuckPermsController.getApi();

		EventBus eventBus = api.getEventBus();

		if (MainController.getDILoginController().isSyncroRolEnabled()) {
			eventBus.subscribe(plugin, NodeAddEvent.class, this::addRole);
			eventBus.subscribe(plugin, NodeClearEvent.class, this::clearRoles);
			eventBus.subscribe(plugin, NodeRemoveEvent.class, this::removeRole);
		}
	}

	/**
	 * Event handler for NodeRemoveEvent.
	 * @param event It is the object that includes the event information.
	 */
	private void removeRole(NodeRemoveEvent event) {
		String playerName = event.getTarget().getFriendlyName();
		internalRemoveRole(playerName, event.getNode());
	}

	/**
	 * Event handler for NodeClearEvent.
	 * @param event It is the object that includes the event information.
	 */
	private void clearRoles(NodeClearEvent event) {
		String playerName = event.getTarget().getFriendlyName();

		for (Node node : event.getNodes()) {
			internalRemoveRole(playerName, node);
		}

	}

	/**
	 * Remove role from player.
	 * @param playerName player to remove role.
	 * @param node role info.
	 */
	private void internalRemoveRole(String playerName, Node node) {
		String group = node.getKey().replace("group.", "");
		List<String> roleList = LuckPermsController.getDiscordRoleFromMinecraftRole(group);

		for (String role : roleList) {
			if (!MainController.getDiscordController().serverHasRole(role))
				continue;

			if (MainController.getDiscordController().userHasRole(role, playerName)) {
				MainController.getDiscordController().removeRole(role, playerName, "Stop being part of the " + group + " group");
			}

		}
	}

	/**
	 * Add role to player.
	 * @param event It is the object that includes the event information.
	 */
	private void addRole(NodeAddEvent event) {
		String playerName = event.getTarget().getFriendlyName();

		String group = event.getNode().getKey().replace("group.", "");
		List<String> roleList = LuckPermsController.getDiscordRoleFromMinecraftRole(group);
		for (String role : roleList) {
			if (!MainController.getDiscordController().serverHasRole(role))
				continue;

			if (!MainController.getDiscordController().userHasRole(role, playerName)) {
				MainController.getDiscordController().giveRole(role, playerName, "Belong to the " + group + " group");
			}

		}
	}
}
