package di.dilogin.discord.util;

import di.dicore.api.DIApi;
import di.dilogin.controller.file.CommandAliasController;
import di.dilogin.controller.file.LangController;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * Utility class for configuring slash commands in Discord.
 */
public class SlashCommandsConfiguration {

    /**
     * Configures slash commands for the Discord bot.
     *
     * @param api The DIApi instance.
     */
    public static void configureSlashCommands(DIApi api) {
    	api.getCoreController().getBot().getApi().get().updateCommands().addCommands(
                        Commands.slash(CommandAliasController.getAlias("userinfo_discord_command").toLowerCase(), LangController.getString("userinfo_discord_slash_description"))
                                .addOption(OptionType.STRING, LangController.getString("userinfo_discord_slash_option_minecraftname_title").toLowerCase(), LangController.getString("userinfo_discord_slash_option_minecraftname_description").toLowerCase(), false, false)
                                .addOption(OptionType.NUMBER, LangController.getString("userinfo_discord_slash_option_discordid_title").toLowerCase(), LangController.getString("userinfo_discord_slash_option_discordid_description").toLowerCase(), false, false)
                                .addOption(OptionType.MENTIONABLE, LangController.getString("userinfo_discord_slash_option_mention_title").toLowerCase(), LangController.getString("userinfo_discord_slash_option_mention_description").toLowerCase(), false, false),
                        Commands.slash(CommandAliasController.getAlias("register_discord_command").toLowerCase(), LangController.getString("register_discord_slash_description"))
                        		.addOption(OptionType.STRING, LangController.getString("register_discord_slash_option_code_title").toLowerCase(), LangController.getString("register_discord_slash_option_code_description").toLowerCase(), true, false),
                        Commands.slash(CommandAliasController.getAlias("userlist_discord_command").toLowerCase(), LangController.getString("userlist_discord_slash_description"))
                        		.addOption(OptionType.INTEGER,  LangController.getString("userlist_discord_slash_option_page_title").toLowerCase(), LangController.getString("userlist_discord_slash_option_page_description").toLowerCase())
                		).queue();
    }
}
