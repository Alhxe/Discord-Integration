package di.dilogin.dao;

import di.dilogin.entity.DIUser;
import net.dv8tion.jda.api.entities.User;

import java.util.Optional;

/**
 * {@DIUser} DAO. It is implemented when the database is hosted on bungeecord.
 */
public class DIUserDaoBungeeImpl implements DIUserDao{
    @Override
    public Optional<DIUser> get(String playerName) {
        return Optional.empty();
    }

    @Override
    public Optional<DIUser> get(long discordid) {
        return Optional.empty();
    }

    @Override
    public void add(DIUser user) {

    }

    @Override
    public void remove(DIUser user) {

    }

    @Override
    public void remove(String playerName) {

    }

    @Override
    public boolean contains(String name) {
        return false;
    }

    @Override
    public boolean containsDiscordId(long id) {
        return false;
    }

    @Override
    public int getDiscordUserAccounts(User user) {
        return 0;
    }
}
