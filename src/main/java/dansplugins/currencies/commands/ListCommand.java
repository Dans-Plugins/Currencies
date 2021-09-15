package dansplugins.currencies.commands;

import dansplugins.currencies.data.PersistentData;
import org.bukkit.command.CommandSender;

public class ListCommand {

    public boolean execute(CommandSender sender) {
        PersistentData.getInstance().sendListOfCurrenciesToSender(sender);
        return true;
    }

}
