package di.dilitebanslogs;

import java.awt.Color;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.logging.Level;

import di.dicore.api.DIApi;
import di.internal.utils.Util;
import litebans.api.Database;
import litebans.api.Entry;
import litebans.api.Events;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * Litebans Listeners.
 */
public class EventsListener {

    /**
     * Prohibits instantiation of the class.
     */
    private EventsListener() {
        throw new IllegalStateException();
    }

    /**
     * Main Discord Integration Project api.
     */
    private static final DIApi api = BukkitApplication.getDIApi();

    /**
     * Main Channel.
     */
    private static final TextChannel channel = BukkitApplication.getChannel();

    /**
     * Init events.
     */
    public static void init() {

        Events.get().register(new Events.Listener() {

            @Override
            public void entryAdded(Entry entry) {
                if (entry.getType().equals("kick")) {
                    if (api.getInternalController().getConfigManager().getBoolean("kick"))
                        kick(entry);
                } else if (entry.getType().equals("ban")) {
                    if (api.getInternalController().getConfigManager().getBoolean("ban"))
                        ban(entry);
                } else if (entry.getType().equals("mute")) {
                    if (api.getInternalController().getConfigManager().getBoolean("mute"))
                        mute(entry);
                } else if (entry.getType().equals("warn")
                        && api.getInternalController().getConfigManager().getBoolean("warn")) {
                    warn(entry);
                }
            }
        });
    }

    /**
     * Send a kick message to the Discord channel.
     *
     * @param entry Litebans entry.
     */
    private static void kick(Entry entry) {
        MessageEmbed embed = getEmbed(entry, "kick");
        channel.sendMessageEmbeds(embed).queue();
    }

    /**
     * Send a ban message to the Discord channel.
     *
     * @param entry Litebans entry.
     */
    private static void ban(Entry entry) {
        MessageEmbed embed = getEmbed(entry, "ban");
        channel.sendMessageEmbeds(embed).queue();
    }

    /**
     * Send a mute message to the Discord channel.
     *
     * @param entry Litebans entry.
     */
    private static void mute(Entry entry) {
        MessageEmbed embed = getEmbed(entry, "mute");
        channel.sendMessageEmbeds(embed).queue();
    }

    /**
     * Send a warn message to the Discord channel.
     *
     * @param entry Litebans entry.
     */
    private static void warn(Entry entry) {
        MessageEmbed embed = getEmbed(entry, "warn");
        channel.sendMessageEmbeds(embed).queue();
    }

    /**
     * @param entry Entry log.
     * @param type  Type of entry log.
     * @return Embed configured.
     */
    private static MessageEmbed getEmbed(Entry entry, String type) {
        EmbedBuilder embed = (new EmbedBuilder()).setColor(getColor(type));

        embed.setTitle(getEntryString(entry, "embed_" + type + "_title"));
        embed.appendDescription(getEntryString(entry, "embed_" + type + "_description"));

        for (int i = 1; i < 10; i++) {
            try {
                String title = getEntryString(entry, "embed_" + type + "_field_" + i + "_title");
                String desc = getEntryString(entry, "embed_" + type + "_field_" + i + "_desc");
                Boolean inline = api.getInternalController().getConfigManager()
                        .getBoolean("embed_" + type + "_field_" + i + "_inline");
                if (title != null && desc != null)
                    embed.addField(title, desc, inline.booleanValue());
            } catch (Exception e) {
                break;
            }
        }
        if (api.getInternalController().getConfigManager().getBoolean("embed_" + type + "_timestamp"))
            embed.setTimestamp(Instant.now());
        return embed.build();
    }

    /**
     * @param string Type of log.
     * @return color.
     */
    private static Color getColor(String string) {
        return Util.hex2Rgb(api.getInternalController().getConfigManager().getString("embed_" + string + "_color"));
    }

    /**
     * @param entry Entry log.
     * @param path  Key of the string.
     * @return String.
     */
    private static String getEntryString(Entry entry, String path) {
        return api.getInternalController().getConfigManager().getString(path)
                .replace("%executor_name%", entry.getExecutorName()).replace("%duration%", entry.getDurationString())
                .replace("%executor_uuid%", entry.getExecutorUUID()).replace("%reason%", getReason(entry.getReason()))
                .replace("%user_name%", getName(entry.getUuid())).replace("%user_uuid%", entry.getUuid())
                .replace("%server%", api.getCoreController().getConfigManager().getString("server_name"));
    }

    /**
     * @param uuid Player UUID.
     * @return Player UUID.
     */
    private static String getName(String uuid) {
        String query = "SELECT name FROM {history} WHERE uuid=? ORDER BY date DESC LIMIT 1";
        try (PreparedStatement ps = Database.get().prepareStatement(query)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            api.getInternalController().getLogger().log(Level.SEVERE, "EventsListener - getName", e);
        }
        return "Unknown";

    }

    /**
     * Check if reason is null or empty.
     *
     * @param reason Original reason.
     * @return Reason.
     */
    private static String getReason(String reason) {
        if (reason.isEmpty() || reason.equals(""))
            return "No reason";
        return reason;
    }

}