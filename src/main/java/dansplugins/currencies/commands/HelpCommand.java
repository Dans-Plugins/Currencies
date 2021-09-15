package dansplugins.currencies.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand {

    public boolean execute(CommandSender sender, String[] arguments) {
        sender.sendMessage(ChatColor.AQUA + "/c help");
        sender.sendMessage(ChatColor.AQUA + "/c create");
        sender.sendMessage(ChatColor.AQUA + "/c info");
        sender.sendMessage(ChatColor.AQUA + "/c list");
        return true;
    }

}