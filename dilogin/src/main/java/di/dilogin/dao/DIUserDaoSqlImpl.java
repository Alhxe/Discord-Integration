package di.dilogin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;

import di.dicore.api.DIApi;
import di.dilogin.controller.DBController;
import di.dilogin.controller.MainController;
import di.dilogin.entity.DIUser;
import di.internal.utils.Util;
import net.dv8tion.jda.api.entities.User;

/**
 * Implementation of {@DIUserDao}.
 */
public class DIUserDaoSqlImpl implements DIUserDao {

	/**
	 * Database connection.
	 */
	private final Connection conn = DBController.getConnect();

	/**
	 * Core api.
	 */
	private final DIApi api = MainController.getDIApi();

	@Override
	public Optional<DIUser> get(String playerName) {
		String query = "select discord_id from user where username = ?;";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, playerName);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					long id = rs.getLong(1);
					Optional<User> userOpt = Util.getDiscordUserById(api.getCoreController().getDiscordApi().get(), id);

					if (userOpt.isPresent()) {
						return Optional.of(new DIUser(playerName, userOpt));
					} else {
						api.getInternalController().getLogger().warning("Unable to get user named " + playerName);
					}
				}
			}
		} catch (SQLException e) {
            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"DIUserDaoSqlImpl",e);
		}
		return Optional.empty();
	}

	@Override
	public void add(DIUser user) {
		String query = "insert into user(username, discord_id) values(?,?);";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			if (user.getPlayerName()!=null && user.getPlayerDiscord().isPresent()) {
				ps.setString(1, user.getPlayerName());
				ps.setLong(2, user.getPlayerDiscord().get().getIdLong());
				ps.execute();
			} else {
				api.getInternalController().getLogger().warning("Unable to add user " + user);
			}
		} catch (SQLException e) {
            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"DIUserDaoSqlImpl",e);
		}
	}

	@Override
	public void remove(DIUser user) {
		String query = "delete from user where discord_id = ?;";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			if (user.getPlayerDiscord().isPresent()) {
				ps.setLong(1, user.getPlayerDiscord().get().getIdLong());
				ps.execute();
			} else {
				api.getInternalController().getLogger().warning("Unable to remove user " + user);
			}
		} catch (SQLException e) {
            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"DIUserDaoSqlImpl",e);
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
            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"DIUserDaoSqlImpl",e);
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
            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"DIUserDaoSqlImpl",e);
		}
		return false;
	}

	@Override
	public int getDiscordUserAccounts(long discordId) {
		String query = "select count(*) as total from user where discord_id = ?;";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setLong(1, discordId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt("total");
			}
		} catch (SQLException e) {
            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"DIUserDaoSqlImpl",e);
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
            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"DIUserDaoSqlImpl",e);
		}
	}

	@Override
	public Optional<DIUser> get(long discordId) {
		String query = "select username from user where discord_id = ?;";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setLong(1, discordId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String playerName = rs.getString(1);
					Optional<User> userOpt = Util.getDiscordUserById(api.getCoreController().getDiscordApi().get(),
							discordId);

					if (userOpt.isPresent()) {
						return Optional.of(new DIUser(playerName, userOpt));
					} else {
						api.getInternalController().getLogger()
								.warning("Unable to get discord user with id " + discordId);
					}
				}
			}
		} catch (SQLException e) {
            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"DIUserDaoSqlImpl",e);
		}
		return Optional.empty();
	}

}
