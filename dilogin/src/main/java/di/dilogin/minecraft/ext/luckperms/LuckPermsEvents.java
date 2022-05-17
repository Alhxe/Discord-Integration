package di.dilogin.minecraft.ext.luckperms;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.minecraft.util.Util;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.node.Node;

public class LuckPermsEvents {

	private final Plugin plugin = BukkitApplication.getPlugin();

	public LuckPermsEvents() {
		LuckPerms api = LuckPermsController.getApi();

		EventBus eventBus = api.getEventBus();

		if (DILoginController.isSyncroRolEnabled()) {
			eventBus.subscribe(this.plugin, NodeAddEvent.class, this::addRole);
			eventBus.subscribe(this.plugin, NodeClearEvent.class, this::clearRoles);
			eventBus.subscribe(this.plugin, NodeRemoveEvent.class, this::removeRole);
			eventBus.subscribe(this.plugin, NodeMutateEvent.class, this::info);
		}
	}

	private void info(NodeMutateEvent event) {
		System.out.println(event.getEventType());
	}

	private void removeRole(NodeRemoveEvent event) {
		String playerName = event.getTarget().getFriendlyName();
		Player player = plugin.getServer().getPlayer(playerName);

		if (player == null)
			return;

		internalRemoveRole(player, event.getNode());
	}

	private void clearRoles(NodeClearEvent event) {
		String playerName = event.getTarget().getFriendlyName();
		Player player = plugin.getServer().getPlayer(playerName);

		if (player == null)
			return;

		for (Node node : event.getNodes()) {
			internalRemoveRole(player, node);
		}

	}

	private void internalRemoveRole(Player player, Node node) {
		String group = node.getKey().replace("group.", "");
		List<String> roleList = LuckPermsController.getDiscordRoleFromMinecraftRole(group);

		for (String role : roleList) {
			if (!Util.serverHasRole(role))
				continue;

			if (Util.userHasRole(role, player.getName())) {
				Util.removeRole(role, player.getName(), "Stop being part of the " + group + " group");
			}

		}
	}

	private void addRole(NodeAddEvent event) {
		String playerName = event.getTarget().getFriendlyName();
		Player player = plugin.getServer().getPlayer(playerName);

		if (player == null)
			return;

		String group = event.getNode().getKey().replace("group.", "");
		List<String> roleList = LuckPermsController.getDiscordRoleFromMinecraftRole(group);
		for (String role : roleList) {
			if (!Util.serverHasRole(role))
				continue;

			if (!Util.userHasRole(role, player.getName())) {
				Util.giveRole(role, player.getName(), "Belong to the " + group + " group");
			}

		}
	}
}
