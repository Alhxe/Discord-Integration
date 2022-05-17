package di.dilogin.minecraft.ext.luckperms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import di.dicore.DIApi;
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
public class LuckPermsController {

	private static LuckPerms api;

	private static final DIApi diapi = BukkitApplication.getDIApi();

	private static List<Map<String, String>> rolMap = initRoleList();

	public static LuckPerms getApi() {
		if (api == null) {
			api = LuckPermsProvider.get();
		}
		return api;
	}

	public static boolean isUserInGroup(Player player, String group) {
		List<User> a = getUsersInGroup(group).join();
		for (User u : a) {
			System.out.println(u.getFriendlyName());
		}
		return getUsersInGroup(group).join().stream().filter(u->u.getFriendlyName().equalsIgnoreCase(player.getName())).findFirst().isPresent();
	}
	
	public static User getLuckPermsUser(Player player) {
		return api.getPlayerAdapter(Player.class).getUser(player);
	}

	public static void addGroup(Player player, String group, String reason) {
		User user = getLuckPermsUser(player);
		user.data().add(Node.builder("group." + group).build());
		api.getUserManager().saveUser(user);
		diapi.getInternalController().getPlugin().getLogger()
				.info(group + " group has been given to " + player.getName() + ". Reason: " + reason);
	}

	public static void removeGroup(Player player, String group, String reason) {
		User user = getLuckPermsUser(player);
		user.data().remove(Node.builder("group." + group).build());
		api.getUserManager().saveUser(user);
		diapi.getInternalController().getPlugin().getLogger()
				.info(group + " group has been removed from " + player.getName() + ". Reason: " + reason);
	}

	public static List<String> getMinecraftRoleFromDiscordRole(String role) {
		List<String> result = new ArrayList<>();
		rolMap.stream().filter(r -> r.containsKey(role)).forEach(r -> r.values().stream().forEach(s -> result.add(s)));
		return result;
	}

	public static List<String> getDiscordRoleFromMinecraftRole(String role) {
		List<String> result = new ArrayList<>();
		rolMap.stream().filter(r -> r.containsValue(role)).forEach(r -> r.keySet().forEach(s -> result.add(s)));
		return result;
	}

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
