package dansplugins.currencies.commands;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.utils.Logger;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel McCoy Stephenson
 */
public class ListCommand extends AbstractPluginCommand {

    public ListCommand() {
        super(new ArrayList<>(Arrays.asList("list")), new ArrayList<>(Arrays.asList("currencies.list")));
    }

    @Override
    public boolean execute(CommandSender sender) {
        PersistentData.getInstance().sendListOfActiveCurrenciesToSender(sender);
        return true;
    }

    public boolean execute(CommandSender sender, String[] args) {
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