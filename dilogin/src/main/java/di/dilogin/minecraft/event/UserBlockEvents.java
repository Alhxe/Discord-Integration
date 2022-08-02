package di.dilogin.minecraft.event;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import di.dilogin.minecraft.cache.UserBlockedCache;

@SuppressWarnings("deprecation")
public class UserBlockEvents implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSendCommand(PlayerCommandPreprocessEvent event) {
		String message = event.getMessage();
		if (!message.contains(" ")) {
			if (message.equalsIgnoreCase("/register"))
				return;
		} else if (message.split(" ")[0].equalsIgnoreCase("/register")) {
			return;
		}
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()) && (event.getFrom().getX() != event.getTo().getX()
				|| event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() < event.getTo().getZ())) {
			Location loc = event.getFrom();
			event.getPlayer().teleport(loc.setDirection(event.getTo().getDirection()));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHungerDecrease(FoodLevelChangeEvent event) {
		if (event.getEntity().getType() != EntityType.PLAYER)
			return;
		Player player = (Player) event.getEntity();
		if (UserBlockedCache.contains(player.getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onOpenInventory(InventoryOpenEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent event) {
		if (UserBlockedCache.contains(event.getWhoClicked().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUserDejaCaerItem(PlayerDropItemEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUserCambiaDeModo(PlayerGameModeChangeEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteractEvent(PlayerInteractEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUserCambiaObjetoDeMano(PlayerItemHeldEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUserLevantaObjeto(PlayerPickupItemEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUserPortal(PlayerPortalEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onConsumeItem(PlayerItemConsumeEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemDamage(PlayerItemDamageEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDamageEvent(BlockDamageEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlacedEvent(BlockPlaceEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlacedEvent(SignChangeEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDamage(EntityDamageByBlockEvent event) {
		if (event.getEntityType() != EntityType.PLAYER)
			return;
		Player player = (Player) event.getEntity();
		if (UserBlockedCache.contains(player.getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.PLAYER)
			return;
		Player player = (Player) event.getEntity();
		if (UserBlockedCache.contains(player.getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity().getType() == EntityType.PLAYER) {
			Player player = (Player) event.getEntity();
			if (UserBlockedCache.contains(player.getName()))
				event.setCancelled(true);
		} else if (event.getDamager().getType() == EntityType.PLAYER) {
			Player player = (Player) event.getDamager();
			if (UserBlockedCache.contains(player.getName()))
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAnimation(PlayerAnimationEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityInteractWithPlayer(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() == null)
			return;
		if (event.getTarget().getType() != EntityType.PLAYER)
			return;
		Player player = (Player) event.getTarget();
		if (UserBlockedCache.contains(player.getName()))
			event.setCancelled(true);
	}
}
