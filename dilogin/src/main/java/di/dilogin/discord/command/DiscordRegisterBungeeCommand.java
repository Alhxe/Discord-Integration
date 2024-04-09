package di.dilogin.discord.command;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import di.dicore.api.DIApi;
import di.dilogin.controller.MainController;
import di.dilogin.controller.file.CommandAliasController;
import di.dilogin.controller.file.LangController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.discord.util.DiscordMessageDeleter;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.bungee.BungeeUtil;
import di.dilogin.minecraft.cache.TmpCache;
import di.internal.entity.DiscordCommand;
import di.internal.entity.DiscordSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class DiscordRegisterBungeeCommand implements DiscordCommand, DiscordSlashCommand {

    private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();
    private final DIApi api = MainController.getDIApi();

    @Override
    public void execute(String message, MessageReceivedEvent event) {
        execute(event, message, event.getAuthor());
    }
    
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        execute(event, event.getOption("code").getAsString(), event.getUser());
    }

    @SuppressWarnings("deprecation")
	private void execute(Object event, String message, User discordUser) {
        if (!MainController.getDILoginController().isRegisterByDiscordCommandEnabled()) return;

        DiscordMessageDeleter.deleteMessage(20, event instanceof MessageReceivedEvent ? ((MessageReceivedEvent) event).getMessage() : null);

        if (userDao.containsDiscordId(discordUser.getIdLong())) {
            sendTempMessage(event, LangController.getString("register_already_exists"));
            return;
        }

        if (userDao.getDiscordUserAccounts(discordUser.getIdLong()) >= api.getInternalController().getConfigManager().getInt("register_max_discord_accounts")) {
            sendTempMessage(event, LangController.getString("register_max_accounts"));
            return;
        }

        if (message.isEmpty()) {
            sendTempMessage(event, LangController.getString("register_discord_arguments"));
            return;
        }

        Optional<ProxiedPlayer> playerOpt = catchRegister(message, event);
        if (!playerOpt.isPresent()) {
            sendTempMessage(event, LangController.getString("register_code_not_found"));
            return;
        }

        ProxiedPlayer player = playerOpt.get();


        String password = CodeGenerator.getCode(8, api);
        player.sendMessage(LangController.getString(discordUser, player.getName(), "register_success").replace("%authme_password%", password));
        sendTempEmbedMessage(event, getEmbedMessage(player, discordUser));
        TmpCache.removeRegister(player.getName());
        userDao.add(new DIUser(player.getName(), Optional.of(discordUser)));

        if (MainController.getDILoginController().isRegisterGiveRoleEnabled()) {
            giveRolesToUser(event instanceof MessageReceivedEvent ? ((MessageReceivedEvent) event).getMember() : null, player.getName());
        }

        MainController.getDILoginController().loginUser(player.getName(), discordUser);
    }

    private void sendTempMessage(Object event, String message) {
        if (event instanceof MessageReceivedEvent) {
            ((MessageReceivedEvent) event).getChannel().sendMessage(message).delay(Duration.ofSeconds(20)).flatMap(Message::delete).queue();
        } else if (event instanceof SlashCommandInteractionEvent) {
            ((SlashCommandInteractionEvent) event).getHook().sendMessage(message).setEphemeral(true).queue();
        }
    }
    
    private void sendTempEmbedMessage(Object event, MessageEmbed message) {
        if (event instanceof MessageReceivedEvent) {
            ((MessageReceivedEvent) event).getChannel().sendMessageEmbeds(message).delay(Duration.ofSeconds(20)).flatMap(Message::delete).queue();
        } else if (event instanceof SlashCommandInteractionEvent) {
            ((SlashCommandInteractionEvent) event).getHook().sendMessageEmbeds(message).setEphemeral(true).queue();
        }
    }

    private Optional<ProxiedPlayer> catchRegister(String message, Object event) {
        Optional<String> code = registerByCode(message);
        if (code.isPresent()) {
            return BungeeUtil.getProxiedPlayer(code.get());
        }
        
        return Optional.empty();
    }

    private Optional<String> registerByCode(String message) {
        Optional<TmpMessage> tmpMessageOpt = TmpCache.getRegisterMessageByCode(message);
        return tmpMessageOpt.map(TmpMessage::getPlayer);
    }

    private MessageEmbed getEmbedMessage(ProxiedPlayer player, User discordUser) {
        EmbedBuilder embedBuilder = MainController.getDILoginController().getEmbedBase()
                .setTitle(LangController.getString(player.getName(), "register_discord_title"))
                .setDescription(LangController.getString(discordUser, player.getName(), "register_discord_success"));
        return embedBuilder.build();
    }

    private void giveRolesToUser(Member member, String playerName) {
        List<Long> roleList = MainController.getDIApi().getInternalController().getConfigManager().getLongList("register_give_role_list");
        for (long roleId : roleList) {
            MainController.getDiscordController().giveRole(String.valueOf(roleId), member != null ? member.getId() : null, "by registering on the server.");
        }    
    }

    @Override
    public String getAlias() {
        return CommandAliasController.getAlias("register_discord_command");
    }
}
