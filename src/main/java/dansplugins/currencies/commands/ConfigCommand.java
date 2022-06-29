package dansplugins.currencies.commands;

import dansplugins.currencies.services.ConfigService;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;
import preponderous.ponder.misc.ArgumentParser;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Daniel McCoy Stephenson
 */
public class ConfigCommand extends AbstractPluginCommand {
    private final ConfigService configService;

    public ConfigCommand(ConfigService configService) {
        super(new ArrayList<>(Arrays.asList("config")), new ArrayList<>(Arrays.asList("currencies.config")));
        this.configService = configService;
    }

    @Override
    public boolean execute(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Sub-commands: show, set");
        return false;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (args[0].equalsIgnoreCase("show")) {
            configService.sendConfigList(sender);
            return true;
        }
        else if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /c config set (option) (value)");
                return false;
            }
            String option = args[1];

            String value = "";
            if (option.equalsIgnoreCase("denyUsageMessage") || option.equalsIgnoreCase("denyCreationMessage")) { // is this relevant to this project?
                ArgumentParser argumentParser = new ArgumentParser();
                List<String> singleQuoteArgs = argumentParser.getArgumentsInsideDoubleQuotes(args);
                if (singleQuoteArgs.size() == 0) {
                    sender.sendMessage(ChatColor.RED + "New message must be in between quotation marks.");
                    return false;
                }
                value = singleQuoteArgs.get(0);
            }
            else {
                value = args[2];
            }

            configService.setConfigOption(option, value, sender);
            return true;
        }
        else {
            sender.sendMessage(ChatColor.RED + "Sub-commands: show, set");
            return false;
        }
    }
}