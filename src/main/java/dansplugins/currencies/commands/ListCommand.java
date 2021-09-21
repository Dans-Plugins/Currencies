package dansplugins.currencies.commands;

import dansplugins.currencies.data.PersistentData;
import org.bukkit.command.CommandSender;

public class ListCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            PersistentData.getInstance().sendListOfActiveCurrenciesToSender(sender);
            return true;
        }

        String list = args[0];

        if (list.equals("active")) {
            PersistentData.getInstance().sendListOfActiveCurrenciesToSender(sender);
        }
        else if (list.equals("retired")) {
            PersistentData.getInstance().sendListOfRetiredCurrenciesToSender(sender);
        }

        return true;
    }

}
