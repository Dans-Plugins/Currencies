package dansplugins.currencies.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import dansplugins.currencies.Currencies;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Daniel McCoy Stephenson
 */
public class DefaultCommand extends AbstractPluginCommand {
    private final Currencies currencies;

    public DefaultCommand(Currencies currencies) {
        super(new ArrayList<>(Arrays.asList("default")), new ArrayList<>(Arrays.asList("currencies.default")));
        this.currencies = currencies;
    }

    @Override
    public boolean execute(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.AQUA + "Currencies " + currencies.getVersion());
        commandSender.sendMessage(ChatColor.AQUA + "Developer: Daniel McCoy Stephenson");
        commandSender.sendMessage(ChatColor.AQUA + "Wiki: https://github.com/dmccoystephenson/Currencies/wiki");
        return true;
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] strings) {
        return execute(commandSender);
    }
}