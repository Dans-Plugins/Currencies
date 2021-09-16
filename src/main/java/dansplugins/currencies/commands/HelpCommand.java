package dansplugins.currencies.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand {

    public boolean execute(CommandSender sender, String[] arguments) {
        sender.sendMessage(ChatColor.AQUA + "/c help - View a list of helpful commands.");
        sender.sendMessage(ChatColor.AQUA + "/c create - Set up your currency.");
        sender.sendMessage(ChatColor.AQUA + "/c info - View information about your currency.");
        sender.sendMessage(ChatColor.AQUA + "/c list - List existing currencies.");
        sender.sendMessage(ChatColor.AQUA + "/c mint - Mint currency in exchange for power.");
        sender.sendMessage(ChatColor.AQUA + "/c balance - View the contents of your coinpurse.");
        return true;
    }

}