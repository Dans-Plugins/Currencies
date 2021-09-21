package dansplugins.currencies.commands;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.utils.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ListCommand {

    public boolean execute(CommandSender sender, String[] args) {
        Logger.getInstance().log("Number of arguments: " + args.length);
        if (args.length == 0) {
            PersistentData.getInstance().sendListOfActiveCurrenciesToSender(sender);
            return true;
        }

        String list = args[0];
        Logger.getInstance().log("List specified: " + list);

        if (list.equalsIgnoreCase("active")) {
            PersistentData.getInstance().sendListOfActiveCurrenciesToSender(sender);
            return true;
        }
        else if (list.equalsIgnoreCase("retired")) {
            PersistentData.getInstance().sendListOfRetiredCurrenciesToSender(sender);
            return true;
        }
        else {
            sender.sendMessage(ChatColor.RED + "Sub-commands: active, retired");
            return false;
        }
    }

}
