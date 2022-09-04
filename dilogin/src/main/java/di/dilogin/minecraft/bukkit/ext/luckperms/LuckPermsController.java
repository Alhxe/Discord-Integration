package di.dilogin.minecraft.bukkit.ext.luckperms;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import di.dilogin.minecraft.util.Util;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.entity.Player;

import di.dicore.api.DIApi;
import di.dilogin.BukkitApplication;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.InheritanceNode;

/**
 * Controller for LuckPerms plugin
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LuckPermsController {

	/**
	 * LuckPerms api instance
	 */
	private static LuckPerms api;

	/**
	 * DIApi api instance.
	 */
	private static final DIApi diapi = BukkitApplication.getDIApi();

	/**
	 * Role map from config file.
	 */
	private static final List<Map<String, String>> rolMap = initRoleList();

	/**
	 * Get the LuckPerms api instance.
	 * @return the LuckPerms api instance.
	 */
	public static LuckPerms getApi() {
		if (api == null) {
			api = LuckPermsProvider.get();
		}
		return api;
	}

	/**
	 * Check if user is in group role.
	 * @param playerName player to check.
	 * @param group group to check.
	 * @return true if user is in group role, false otherwise.
	 */
	public static boolean isUserInGroup(String playerName, String group) {
		List<User> a = getUsersInGroup(group).join();
		for (User u : a) {
			diapi.getCoreController().getLogger().info(u.getFriendlyName());
		}
		return getUsersInGroup(group).join().stream().anyMatch(u->u.getFriendlyName().equalsIgnoreCase(playerName));
	}

	/**
	 * Get the perms of the user.
	 * @param userName player to get perms.
	 * @return the perms of the user.
	 */
	public static User getLuckPermsUser(String userName) {
		return api.getUserManager().getUser(userName);
	}

	/**
	 * Syncronize the roles of the player.
	 * @param player minecraft player to syncronize.
	 */
	public static void syncUserRole(Player player){
		rolMap.forEach(map -> {
			Optional<Entry<String, String>> optEntry = map.entrySet().stream().findFirst();
			if (optEntry.isPresent()) {
				Entry<String, String> entry = optEntry.get();
				String role = entry.getKey();
				String group = entry.getValue();

				boolean isUserInGroup = isUserInGroup(player.getName(), group);
				boolean isUserInRole = Util.userHasRole(role, player.getName());

				Role discordRole = diapi.getCoreController().getGuild().getRoleById(role);
				if (isUserInGroup && !isUserInRole){
					removeGroup(player.getName(), group, "Removed " + discordRole + " role in discord.");
				}

				if (!isUserInGroup && isUserInRole){
					addGroup(player.getName(), group, "Get " + discordRole + " role in discord.");
				}
			}
		});

	}

	/**
	 * Add the user to the group.
	 * @param playerName player to add.
	 * @param group	group to add.
	 * @param reason Reason to add the group to the user.
	 */
	public static void addGroup(String playerName, String group, String reason) {
		User user = getLuckPermsUser(playerName);
		user.data().add(Node.builder("group." + group).build());
		api.getUserManager().saveUser(user);
		diapi.getInternalController().getLogger()
				.info(group + " group has been given to " + playerName + ". Reason: " + reason);
	}

	/**
	 * Remove the user from the group.
	 * @param playerName player to remove.
	 * @param group group to remove.
	 * @param reason Reason to remove the group from the user.
	 */
	public static void removeGroup(String playerName, String group, String reason) {
		User user = getLuckPermsUser(playerName);
		user.data().remove(Node.builder("group." + group).build());
		api.getUserManager().saveUser(user);
		diapi.getInternalController().getLogger()
				.info(group + " group has been removed from " + playerName + ". Reason: " + reason);
	}

	/**
	 * Get the minecraft role associated with the minecraft group.
	 * @param role role to get.
	 * @return the minecraft group associated with the discord role.
	 */
	public static List<String> getMinecraftRoleFromDiscordRole(String role) {
		List<String> result = new ArrayList<>();
		rolMap.stream().filter(r -> r.containsKey(role)).forEach(r -> result.addAll(r.values()));
		return result;
	}

	/**
	 * Get the minecraft group associated with the discord role.
	 * @param role role to get.
	 * @return the discord role associated with the minecraft group.
	 */
	public static List<String> getDiscordRoleFromMinecraftRole(String role) {
		List<String> result = new ArrayList<>();
		rolMap.stream().filter(r -> r.containsValue(role)).forEach(r -> result.addAll(r.keySet()));
		return result;
	}

	/**
	 * Init the role map from config file.
	 * @return the role map from config file.
	 */
	private static List<Map<String, String>> initRoleList() {
		List<Map<Object, Object>> tmpList = diapi.getInternalController().getConfigManager().getList("syncro_rol_list");
		List<Map<String, String>> result = new ArrayList<>();
		for (Map<Object, Object> m : tmpList) {
			Map<String, String> tmap = new HashMap<>();
			Entry<Object, Object> entry = m.entrySet().iterator().next();
			String key = String.valueOf(entry.getKey());
			String value = String.valueOf(entry.getValue());
			tmap.put(key, value);
			result.add(tmap);
		}
		return result;
	}

	/**
	 * Get the users in the group.
	 * @param groupName group to get.
	 * @return the users in the group.
	 */
	private static CompletableFuture<List<User>> getUsersInGroup(String groupName) {
		NodeMatcher<InheritanceNode> matcher = NodeMatcher.key(InheritanceNode.builder(groupName).build());
		return api.getUserManager().searchAll(matcher).thenComposeAsync(results -> {
			List<CompletableFuture<User>> users = new ArrayList<>();
			return CompletableFuture
					.allOf(results.keySet().stream().map(uuid -> api.getUserManager().loadUser(uuid))
							.peek(users::add).toArray(CompletableFuture[]::new))
					.thenApply(x -> users.stream().map(CompletableFuture::join).collect(Collectors.toList()));
		});
	}

}
