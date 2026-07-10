package fr.rp.admin.commands;

import fr.rp.admin.AdminRP;
import fr.rp.admin.models.Sanction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SanctionCommand implements CommandExecutor {

    private final AdminRP plugin;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public SanctionCommand(AdminRP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("adminrp.staff")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        switch (label.toLowerCase()) {
            case "ban" -> handleBan(sender, args);
            case "unban" -> handleUnban(sender, args);
            case "mute" -> handleMute(sender, args);
            case "unmute" -> handleUnmute(sender, args);
            case "kick" -> handleKick(sender, args);
            case "warn" -> handleWarn(sender, args);
            case "sanctions" -> handleSanctions(sender, args);
        }
        return true;
    }

    private void handleBan(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /ban <joueur> [duree] [raison]");
            return;
        }
        OfflinePlayer cible = Bukkit.getOfflinePlayer(args[0]);
        long duree = args.length >= 2 ? plugin.getSanctionManager().parserDuree(args[1]) : -1;
        String raison = args.length >= 3 ? String.join(" ", List.of(args).subList(2, args.length)) : "Non specifiee";
        // Si args[1] n'est pas une duree valide, on le considere comme faisant partie de la raison
        if (args.length >= 2 && duree == 0 && !args[1].equalsIgnoreCase("perm")) {
            raison = String.join(" ", List.of(args).subList(1, args.length));
            duree = -1;
        }
        plugin.getSanctionManager().bannir(cible, sender, raison, duree);
        sender.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a ete banni.");
    }

    private void handleUnban(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unban <joueur>");
            return;
        }
        OfflinePlayer cible = Bukkit.getOfflinePlayer(args[0]);
        plugin.getSanctionManager().debannir(cible);
        sender.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a ete debanni.");
    }

    private void handleMute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /mute <joueur> [duree] [raison]");
            return;
        }
        OfflinePlayer cible = Bukkit.getOfflinePlayer(args[0]);
        long duree = args.length >= 2 ? plugin.getSanctionManager().parserDuree(args[1]) : 3_600_000L;
        String raison = args.length >= 3 ? String.join(" ", List.of(args).subList(2, args.length)) : "Non specifiee";
        if (args.length >= 2 && duree == 0 && !args[1].equalsIgnoreCase("perm")) {
            raison = String.join(" ", List.of(args).subList(1, args.length));
            duree = 3_600_000L;
        }
        plugin.getSanctionManager().mute(cible, sender, raison, duree);
        sender.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a ete mute.");
    }

    private void handleUnmute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unmute <joueur>");
            return;
        }
        OfflinePlayer cible = Bukkit.getOfflinePlayer(args[0]);
        plugin.getSanctionManager().unmute(cible);
        sender.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a ete demute.");
    }

    private void handleKick(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /kick <joueur> [raison]");
            return;
        }
        Player cible = Bukkit.getPlayer(args[0]);
        if (cible == null) {
            sender.sendMessage(ChatColor.RED + "Ce joueur n'est pas en ligne.");
            return;
        }
        String raison = args.length >= 2 ? String.join(" ", List.of(args).subList(1, args.length)) : "Non specifiee";
        plugin.getSanctionManager().kick(cible, sender, raison);
        sender.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a ete expulse.");
    }

    private void handleWarn(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /warn <joueur> [raison]");
            return;
        }
        OfflinePlayer cible = Bukkit.getOfflinePlayer(args[0]);
        String raison = args.length >= 2 ? String.join(" ", List.of(args).subList(1, args.length)) : "Non specifiee";
        plugin.getSanctionManager().warn(cible, sender, raison);
        sender.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a recu un avertissement.");
    }

    private void handleSanctions(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /sanctions <joueur>");
            return;
        }
        OfflinePlayer cible = Bukkit.getOfflinePlayer(args[0]);
        List<Sanction> historique = plugin.getDatabaseManager().getHistorique(cible.getUniqueId());
        if (historique.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Aucune sanction trouvee pour " + cible.getName() + ".");
            return;
        }
        sender.sendMessage(ChatColor.GOLD + "=== Historique de " + cible.getName() + " (" + historique.size() + ") ===");
        for (Sanction s : historique) {
            ChatColor c = switch (s.getType()) {
                case BAN -> ChatColor.RED;
                case MUTE -> ChatColor.YELLOW;
                case KICK -> ChatColor.GOLD;
                case WARN -> ChatColor.AQUA;
            };
            sender.sendMessage(c + "[" + s.getType() + "] " + ChatColor.WHITE + s.getRaison() +
                    ChatColor.GRAY + " - par " + (s.getPseudoAuteur() != null ? s.getPseudoAuteur() : "Console") +
                    " le " + sdf.format(new Date(s.getDateDebut())) +
                    (s.isActive() && !s.isPermanent() ? ChatColor.GRAY + " (expire le " + sdf.format(new Date(s.getDateFin())) + ")" : ""));
        }
    }
}
