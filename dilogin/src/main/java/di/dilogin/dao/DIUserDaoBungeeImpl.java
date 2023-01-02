package di.dilogin.dao;

import java.util.Optional;

import di.dilogin.entity.DIUser;

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
    public int getDiscordUserAccounts(long discordId) {
        return 0;
    }
}
