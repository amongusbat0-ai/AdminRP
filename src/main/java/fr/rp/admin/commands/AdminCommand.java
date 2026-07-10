package fr.rp.admin.commands;

import fr.rp.admin.AdminRP;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {

    private final AdminRP plugin;

    public AdminCommand(AdminRP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Cette commande est reservee aux joueurs.");
            return true;
        }
        if (!player.hasPermission("adminrp.admin")) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        plugin.getAdminPanelGui().ouvrirMenuPrincipal(player);
        return true;
    }
}
