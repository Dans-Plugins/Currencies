package dansplugins.currencies.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class PermissionChecker {

    private static PermissionChecker instance;

    private PermissionChecker() {

    }

    public static PermissionChecker getInstance() {
        if (instance == null) {
            instance = new PermissionChecker();
        }
        return instance;
    }

    public boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "In order to use this command, you need the following permission: '" + permission + "'");
            return false;
        }
        return true;
    }

}
