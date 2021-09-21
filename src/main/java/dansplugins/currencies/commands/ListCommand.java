package dansplugins.currencies.commands;

import dansplugins.currencies.data.PersistentData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ListCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            PersistentData.getInstance().sendListOfActiveCurrenciesToSender(sender);
            return true;
        }

        String list = args[0];

        if (list.equalsIgnoreCase("active")) {
            PersistentData.getInstance().sendListOfActiveCurrenciesToSender(sender);
        }
        else if (list.equalsIgnoreCase("retired")) {
            PersistentData.getInstance().sendListOfRetiredCurrenciesToSender(sender);
        }
        else {
            sender.sendMessage(ChatColor.RED + "Sub-commands: active, retired");
        }

        return true;
    }

}
