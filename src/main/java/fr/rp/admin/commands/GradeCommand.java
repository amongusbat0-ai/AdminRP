package fr.rp.admin.commands;

import fr.rp.admin.AdminRP;
import fr.rp.admin.models.Grade;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GradeCommand implements CommandExecutor {

    private final AdminRP plugin;

    public GradeCommand(AdminRP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("grades")) {
            sender.sendMessage(ChatColor.GOLD + "=== Grades disponibles ===");
            plugin.getGradeManager().getGrades().values().forEach(g ->
                    sender.sendMessage(g.getPrefixe() + " " + g.getLogo() + " " + g.getCouleurNom() + g.getId() +
                            ChatColor.GRAY + " (priorite " + g.getPriorite() + ")"));
            return true;
        }

        if (!sender.hasPermission("adminrp.admin")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /grade <set|info> <joueur> [grade]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /grade set <joueur> <grade>");
                    return true;
                }
                OfflinePlayer cible = Bukkit.getOfflinePlayer(args[1]);
                String gradeId = args[2].toLowerCase();
                if (!plugin.getGradeManager().existe(gradeId)) {
                    sender.sendMessage(ChatColor.RED + "Ce grade n'existe pas. Utilisez /grades pour voir la liste.");
                    return true;
                }
                plugin.getDatabaseManager().assurerJoueur(cible.getUniqueId(), cible.getName());
                plugin.getDatabaseManager().setGrade(cible.getUniqueId(), gradeId);
                Player enLigne = cible.getPlayer();
                if (enLigne != null) plugin.getGradeManager().appliquerPermissions(enLigne);
                sender.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a maintenant le grade " + gradeId + ".");
                if (enLigne != null) {
                    enLigne.sendMessage(ChatColor.GREEN + "Votre grade a ete change en : " +
                            plugin.getGradeManager().getGrade(gradeId).getPrefixe());
                }
            }
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /grade info <joueur>");
                    return true;
                }
                OfflinePlayer cible = Bukkit.getOfflinePlayer(args[1]);
                String gradeId = plugin.getDatabaseManager().getGrade(cible.getUniqueId());
                Grade grade = plugin.getGradeManager().getGrade(gradeId);
                sender.sendMessage(ChatColor.GOLD + cible.getName() + " -> " + grade.getPrefixe() + " " + grade.getLogo());
            }
            default -> sender.sendMessage(ChatColor.RED + "Usage: /grade <set|info> <joueur> [grade]");
        }
        return true;
    }
}
