package dansplugins.currencies;

import dansplugins.currencies.commands.HelpCommand;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandInterpreter {

    public boolean interpretCommand(CommandSender sender, String label, String[] args) {
        if (label.equalsIgnoreCase("currencies") || label.equalsIgnoreCase("c")) {

            if (args.length == 0) {
                sender.sendMessage(ChatColor.AQUA + "Currencies " + Currencies.getInstance().getVersion());
                sender.sendMessage(ChatColor.AQUA + "Developer: DanTheTechMan");
                sender.sendMessage(ChatColor.AQUA + "Wiki: https://github.com/dmccoystephenson/Currencies/wiki");
                return false;
            }

            String secondaryLabel = args[0];
            String[] arguments = getArguments(args);

            if (secondaryLabel.equalsIgnoreCase("help")) {
                if (!checkPermission(sender, "currencies.help")) { return false; }
                HelpCommand command = new HelpCommand();
                return command.execute(sender, arguments);
            }

            // disallow the usage of most of the commands if Currencies cannot utilize Medieval Factions
            if (!MedievalFactionsIntegrator.getInstance().isMedievalFactionsAPIAvailable()) {
                sender.sendMessage(ChatColor.RED + "Currencies cannot utilize Medieval Factions for some reason. It may have to be updated.");
                return false;
            }

            sender.sendMessage(ChatColor.RED + "Currencies doesn't recognize that command.");
        }
        return false;
    }

    private String[] getArguments(String[] args) {
        String[] toReturn = new String[args.length - 1];

        for (int i = 1; i < args.length; i++) {
            toReturn[i - 1] = args[i];
        }

        return toReturn;
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "In order to use this command, you need the following permission: '" + permission + "'");
            return false;
        }
        return true;
    }

}