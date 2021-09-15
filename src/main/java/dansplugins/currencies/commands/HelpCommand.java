package dansplugins.currencies.commands;

import org.bukkit.command.CommandSender;

public class HelpCommand {

    public boolean execute(CommandSender sender, String[] arguments) {
        sender.sendMessage("/c help");
        return true;
    }

}