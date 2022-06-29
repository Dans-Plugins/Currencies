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
    private final PersistentData persistentData;
    private final Logger logger;

    public ListCommand(PersistentData persistentData, Logger logger) {
        super(new ArrayList<>(Arrays.asList("list")), new ArrayList<>(Arrays.asList("currencies.list")));
        this.persistentData = persistentData;
        this.logger = logger;
    }

    @Override
    public boolean execute(CommandSender sender) {
        persistentData.sendListOfActiveCurrenciesToSender(sender);
        return true;
    }

    public boolean execute(CommandSender sender, String[] args) {
        String list = args[0];
        logger.log("List specified: " + list);

        if (list.equalsIgnoreCase("active")) {
            persistentData.sendListOfActiveCurrenciesToSender(sender);
            return true;
        }
        else if (list.equalsIgnoreCase("retired")) {
            persistentData.sendListOfRetiredCurrenciesToSender(sender);
            return true;
        }
        else {
            sender.sendMessage(ChatColor.RED + "Sub-commands: active, retired");
            return false;
        }
    }
}