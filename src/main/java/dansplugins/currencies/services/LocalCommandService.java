package dansplugins.currencies.services;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.integrators.MedievalFactionsIntegrator;
import dansplugins.currencies.commands.*;
import dansplugins.currencies.utils.ArgumentParser;
import dansplugins.currencies.utils.PermissionChecker;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class LocalCommandService {

    public boolean interpretCommand(CommandSender sender, String label, String[] args) {
        if (label.equalsIgnoreCase("currencies") || label.equalsIgnoreCase("c")) {

            if (args.length == 0) {
                sender.sendMessage(ChatColor.AQUA + "Currencies " + Currencies.getInstance().getVersion());
                sender.sendMessage(ChatColor.AQUA + "Developer: DanTheTechMan");
                sender.sendMessage(ChatColor.AQUA + "Wiki: https://github.com/dmccoystephenson/Currencies/wiki");
                return false;
            }

            String secondaryLabel = args[0];
            String[] arguments = ArgumentParser.getInstance().dropFirstArgument(args);

            if (secondaryLabel.equalsIgnoreCase("help")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.help")) { return false; }
                HelpCommand command = new HelpCommand();
                return command.execute(sender, arguments);
            }

            // disallow the usage of most of the commands if Currencies cannot utilize Medieval Factions
            if (!MedievalFactionsIntegrator.getInstance().isMedievalFactionsAPIAvailable()) {
                sender.sendMessage(ChatColor.RED + "Currencies cannot utilize Medieval Factions for some reason. It may have to be updated.");
                return false;
            }

            if (secondaryLabel.equalsIgnoreCase("create")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.create")) { return false; }
                CreateCommand command = new CreateCommand();
                return command.execute(sender, arguments);
            }

            if (secondaryLabel.equalsIgnoreCase("info")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.info")) { return false; }
                InfoCommand command = new InfoCommand();
                return command.execute(sender, arguments);
            }

            if (secondaryLabel.equalsIgnoreCase("list")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.list")) { return false; }
                ListCommand command = new ListCommand();
                return command.execute(sender, arguments);
            }

            if (secondaryLabel.equalsIgnoreCase("mint")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.mint")) { return false; }
                MintCommand command = new MintCommand();
                return command.execute(sender, arguments);
            }

            if (secondaryLabel.equalsIgnoreCase("balance")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.balance")) { return false; }
                BalanceCommand command = new BalanceCommand();
                return command.execute(sender, arguments);
            }

            if (secondaryLabel.equalsIgnoreCase("deposit")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.deposit")) { return false; }
                DepositCommand command = new DepositCommand();
                return command.execute(sender, arguments);
            }

            if (secondaryLabel.equalsIgnoreCase("withdraw")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.withdraw")) { return false; }
                WithdrawCommand command = new WithdrawCommand();
                return command.execute(sender, arguments);
            }

            if (secondaryLabel.equalsIgnoreCase("config")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.config")) { return false; }
                ConfigCommand command = new ConfigCommand();
                return command.execute(sender, arguments);
            }

            if (secondaryLabel.equalsIgnoreCase("desc")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.desc")) { return false; }
                DescCommand command = new DescCommand();
                return command.execute(sender, arguments);
            }

            if (secondaryLabel.equalsIgnoreCase("rename")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.rename")) { return false; }
                RenameCommand command = new RenameCommand();
                return command.execute(sender, arguments);
            }

            if (secondaryLabel.equalsIgnoreCase("retire")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.retire")) { return false; }
                RetireCommand command = new RetireCommand();
                return command.execute(sender);
            }

            if (secondaryLabel.equalsIgnoreCase("force")) {
                if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.force")) { return false; }
                ForceCommand command = new ForceCommand();
                return command.execute(sender, arguments);
            }

            sender.sendMessage(ChatColor.RED + "Currencies doesn't recognize that command.");
        }
        return false;
    }

}