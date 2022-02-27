package dansplugins.currencies.commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;

/**
 * @author Daniel McCoy Stephenson
 */
public class HelpCommand extends AbstractPluginCommand {

    public HelpCommand() {
        super(new ArrayList<>(Arrays.asList("help")), new ArrayList<>(Arrays.asList("currencies.help")));
    }

    @Override
    public boolean execute(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "/c help - View a list of helpful commands.");
        sender.sendMessage(ChatColor.AQUA + "/c info - View information about your currency.");
        sender.sendMessage(ChatColor.AQUA + "/c list - List existing currencies.");
        sender.sendMessage(ChatColor.AQUA + "/c balance - View the contents of your coinpurse.");
        sender.sendMessage(ChatColor.AQUA + "/c deposit - Deposit currency into your coinpurse.");
        sender.sendMessage(ChatColor.AQUA + "/c withdraw - Withdraw currency from your coinpurse.");
        sender.sendMessage(ChatColor.AQUA + "/c create - Set up your currency.");
        sender.sendMessage(ChatColor.AQUA + "/c desc - Set the description of your currency.");
        sender.sendMessage(ChatColor.AQUA + "/c mint - Mint currency in exchange for power.");
        sender.sendMessage(ChatColor.AQUA + "/c rename - Rename your currency.");
        sender.sendMessage(ChatColor.AQUA + "/c retire - Retire your currency.");
        sender.sendMessage(ChatColor.AQUA + "/c config - View or set config options.");
        sender.sendMessage(ChatColor.AQUA + "/c force - Force the plugin to perform an action.");
        return true;
    }

    public boolean execute(CommandSender sender, String[] arguments) {
        return execute(sender);
    }
}