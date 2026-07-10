package fr.rp.admin.managers;

import fr.rp.admin.AdminRP;
import fr.rp.admin.models.Sanction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class SanctionManager {

    private final AdminRP plugin;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public SanctionManager(AdminRP plugin) {
        this.plugin = plugin;
    }

    /**
     * Convertit une chaine comme "1d", "2h", "30m" en millisecondes.
     * Retourne -1 si "perm"/"permanent", 0 si invalide/vide.
     */
    public long parserDuree(String duree) {
        if (duree == null || duree.isEmpty()) return 0;
        if (duree.equalsIgnoreCase("perm") || duree.equalsIgnoreCase("permanent")) return -1;
        try {
            char unite = duree.charAt(duree.length() - 1);
            long valeur = Long.parseLong(duree.substring(0, duree.length() - 1));
            return switch (unite) {
                case 's' -> valeur * 1000L;
                case 'm' -> valeur * 60_000L;
                case 'h' -> valeur * 3_600_000L;
                case 'd' -> valeur * 86_400_000L;
                case 'j' -> valeur * 86_400_000L;
                default -> 0L;
            };
        } catch (Exception e) {
            return 0;
        }
    }

    public void bannir(OfflinePlayer cible, CommandSender auteur, String raison, long dureeMs) {
        long dateDebut = System.currentTimeMillis();
        long dateFin = dureeMs == -1 ? -1 : dateDebut + dureeMs;
        plugin.getDatabaseManager().desactiverSanctionsActives(cible.getUniqueId(), Sanction.Type.BAN);

        Sanction sanction = new Sanction(0, cible.getUniqueId(), cible.getName(),
                auteur instanceof Player p ? p.getUniqueId() : null, auteur.getName(),
                Sanction.Type.BAN, raison, dateDebut, dateFin, true);
        plugin.getDatabaseManager().ajouterSanction(sanction);
        logSanction(sanction);

        Player enLigne = cible.getPlayer();
        String messageKick = ChatColor.RED + "Vous etes banni du serveur.\n" +
                ChatColor.GRAY + "Raison : " + ChatColor.WHITE + raison + "\n" +
                ChatColor.GRAY + "Duree : " + ChatColor.WHITE + (dureeMs == -1 ? "Permanent" : formatDuree(dureeMs));
        if (enLigne != null) enLigne.kickPlayer(messageKick);

        if (plugin.getConfig().getBoolean("broadcast-sanctions", true)) {
            Bukkit.broadcastMessage(ChatColor.RED + "⚡ " + cible.getName() + " a ete banni. Raison : " + raison);
        }
    }

    public void debannir(OfflinePlayer cible) {
        plugin.getDatabaseManager().desactiverSanctionsActives(cible.getUniqueId(), Sanction.Type.BAN);
    }

    public void mute(OfflinePlayer cible, CommandSender auteur, String raison, long dureeMs) {
        long dateDebut = System.currentTimeMillis();
        long dateFin = dureeMs == -1 ? -1 : dateDebut + dureeMs;
        plugin.getDatabaseManager().desactiverSanctionsActives(cible.getUniqueId(), Sanction.Type.MUTE);

        Sanction sanction = new Sanction(0, cible.getUniqueId(), cible.getName(),
                auteur instanceof Player p ? p.getUniqueId() : null, auteur.getName(),
                Sanction.Type.MUTE, raison, dateDebut, dateFin, true);
        plugin.getDatabaseManager().ajouterSanction(sanction);
        logSanction(sanction);

        Player enLigne = cible.getPlayer();
        if (enLigne != null) {
            enLigne.sendMessage(ChatColor.RED + "Vous avez ete rendu muet. Raison : " + raison +
                    " (" + (dureeMs == -1 ? "Permanent" : formatDuree(dureeMs)) + ")");
        }
        if (plugin.getConfig().getBoolean("broadcast-sanctions", true)) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "🔇 " + cible.getName() + " a ete mute. Raison : " + raison);
        }
    }

    public void unmute(OfflinePlayer cible) {
        plugin.getDatabaseManager().desactiverSanctionsActives(cible.getUniqueId(), Sanction.Type.MUTE);
    }

    public boolean estMute(UUID uuid) {
        return plugin.getDatabaseManager().getSanctionActive(uuid, Sanction.Type.MUTE) != null;
    }

    public boolean estBanni(UUID uuid) {
        return plugin.getDatabaseManager().getSanctionActive(uuid, Sanction.Type.BAN) != null;
    }

    public void kick(Player cible, CommandSender auteur, String raison) {
        Sanction sanction = new Sanction(0, cible.getUniqueId(), cible.getName(),
                auteur instanceof Player p ? p.getUniqueId() : null, auteur.getName(),
                Sanction.Type.KICK, raison, System.currentTimeMillis(), System.currentTimeMillis(), false);
        plugin.getDatabaseManager().ajouterSanction(sanction);
        logSanction(sanction);
        cible.kickPlayer(ChatColor.RED + "Vous avez ete expulse.\n" + ChatColor.GRAY + "Raison : " + ChatColor.WHITE + raison);

        if (plugin.getConfig().getBoolean("broadcast-sanctions", true)) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "👢 " + cible.getName() + " a ete expulse. Raison : " + raison);
        }
    }

    public void warn(OfflinePlayer cible, CommandSender auteur, String raison) {
        Sanction sanction = new Sanction(0, cible.getUniqueId(), cible.getName(),
                auteur instanceof Player p ? p.getUniqueId() : null, auteur.getName(),
                Sanction.Type.WARN, raison, System.currentTimeMillis(), System.currentTimeMillis(), false);
        plugin.getDatabaseManager().ajouterSanction(sanction);
        logSanction(sanction);

        int total = plugin.getDatabaseManager().incrementerWarn(cible.getUniqueId());
        Player enLigne = cible.getPlayer();
        if (enLigne != null) {
            enLigne.sendMessage(ChatColor.YELLOW + "⚠ Vous avez recu un avertissement (" + total + "). Raison : " + raison);
        }

        int seuilMute = plugin.getConfig().getInt("warns-avant-mute", 3);
        int seuilBan = plugin.getConfig().getInt("warns-avant-ban", 5);
        if (total == seuilBan) {
            bannir(cible, Bukkit.getConsoleSender(), "Trop d'avertissements (" + total + ")", -1);
        } else if (total == seuilMute) {
            mute(cible, Bukkit.getConsoleSender(), "Trop d'avertissements (" + total + ")", 3_600_000L);
        }
    }

    private void logSanction(Sanction s) {
        String ligne = String.format("[%s] %s -> %s | Auteur: %s | Raison: %s | Duree: %s",
                sdf.format(new Date(s.getDateDebut())),
                s.getType(),
                s.getPseudoJoueur(),
                s.getPseudoAuteur() != null ? s.getPseudoAuteur() : "Console",
                s.getRaison(),
                s.isPermanent() ? "Permanent" : formatDuree(s.getDateFin() - s.getDateDebut()));

        plugin.getLogger().info(ligne);
        try {
            java.io.File dossierLogs = new java.io.File(plugin.getDataFolder(), "logs");
            if (!dossierLogs.exists()) dossierLogs.mkdirs();
            java.io.File fichier = new java.io.File(dossierLogs, "sanctions.log");
            try (FileWriter fw = new FileWriter(fichier, true)) {
                fw.write(ligne + System.lineSeparator());
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Impossible d'ecrire le log de sanction", e);
        }
    }

    public String formatDuree(long ms) {
        long secondes = ms / 1000;
        long jours = secondes / 86400;
        secondes %= 86400;
        long heures = secondes / 3600;
        secondes %= 3600;
        long minutes = secondes / 60;

        StringBuilder sb = new StringBuilder();
        if (jours > 0) sb.append(jours).append("j ");
        if (heures > 0) sb.append(heures).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (sb.isEmpty()) sb.append(secondes).append("s");
        return sb.toString().trim();
    }
}
