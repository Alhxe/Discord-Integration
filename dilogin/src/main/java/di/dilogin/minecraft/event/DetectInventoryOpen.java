package di.dilogin.minecraft.event;

import java.lang.reflect.Field;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import di.dilogin.entity.TinyProtocol;
import io.netty.channel.Channel;

public class DetectInventoryOpen {

	public void onPlayerOpenInventory(Player p) {
		p.closeInventory();
	}

	public DetectInventoryOpen(Plugin plugin) {
		new TinyProtocol(plugin) {

			@Override
			public Object onPacketInAsync(Player sender, Channel channel, Object packet) {

				String packetName = packet.getClass().getSimpleName();

				if ("PacketPlayInClientCommand".equals(packetName)) {

					for (Field field : packet.getClass().getDeclaredFields()) {
						if ("EnumClientCommand".equals(field.getType().getSimpleName())) {
							try {
								field.setAccessible(true);
								if ("OPEN_INVENTORY_ACHIEVEMENT".equals(field.get(packet).toString())) {
									onPlayerOpenInventory(sender);
									break;
								}

							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}

				}
				return super.onPacketInAsync(sender, channel, packet);
			}
		};
	}
}
