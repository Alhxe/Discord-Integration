package di.dilogin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.bukkit.entity.Player;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.DBController;
import di.dilogin.entity.DIUser;
import di.internal.utils.Utils;
import net.dv8tion.jda.api.entities.User;

/**
 * Implementation of {@DIUserDao}.
 */
public class DIUserDaoSqlImpl implements DIUserDao {

	/**
	 * Database connection.
	 */
	private Connection conn = DBController.getConnect();

	/**
	 * Core api.
	 */
	private final DIApi api = BukkitApplication.getDIApi();

	@Override
	public Optional<DIUser> get(String playerName) {
		String query = "select discord_id from user where username = ?;";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, playerName);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					long id = rs.getLong(1);
					Optional<User> userOpt = Utils.getDiscordUserById(api.getCoreController().getDiscordApi(), id);
					Optional<Player> playerOpt = Utils.getUserPlayerByName(api.getInternalController().getPlugin(),
							playerName);

					if (userOpt.isPresent()) {
						return Optional.of(new DIUser(playerOpt, userOpt.get()));
					} else {
						BukkitApplication.getPlugin().getLogger().warning("Unable to get user named " + playerName);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	@Override
	public void add(DIUser user) {
		String query = "insert into user(username, discord_id) values(?,?);";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, user.getPlayerBukkit().get().getName());
			ps.setLong(2, user.getPlayerDiscord().getIdLong());
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void remove(DIUser user) {
		String query = "delete from user where discord_id = ?;";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setLong(1, user.getPlayerDiscord().getIdLong());
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean contains(String name) {
		String query = "select * from user where username = ?;";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean containsDiscordId(long id) {
		String query = "select * from user where discord_id = ?;";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int getDiscordUserAccounts(User user) {
		String query = "select count(*) as total from user where discord_id = ?;";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setLong(1, user.getIdLong());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt("total");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void remove(String playerName) {
		String query = "delete from user where username = ?;";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, playerName);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
