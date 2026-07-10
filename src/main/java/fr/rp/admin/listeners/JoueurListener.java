package fr.rp.admin.listeners;

import fr.rp.admin.AdminRP;
import fr.rp.admin.models.Sanction;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JoueurListener implements Listener {

    private final AdminRP plugin;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public JoueurListener(AdminRP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        Sanction ban = plugin.getDatabaseManager().getSanctionActive(event.getUniqueId(), Sanction.Type.BAN);
        if (ban != null) {
            String duree = ban.isPermanent() ? "Permanent" : "Jusqu'au " + sdf.format(new Date(ban.getDateFin()));
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    ChatColor.RED + "Vous etes banni du serveur.\n" +
                            ChatColor.GRAY + "Raison : " + ChatColor.WHITE + ban.getRaison() + "\n" +
                            ChatColor.GRAY + "Duree : " + ChatColor.WHITE + duree);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getDatabaseManager().assurerJoueur(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        plugin.getGradeManager().appliquerPermissions(event.getPlayer());
        event.setJoinMessage(ChatColor.GREEN + "+ " + plugin.getGradeManager().formaterAffichage(event.getPlayer()));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (plugin.getSanctionManager().estMute(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Vous etes actuellement mute et ne pouvez pas parler.");
            return;
        }
        String format = plugin.getGradeManager().formaterChat(event.getPlayer(), "%2$s");
        event.setFormat(format.replace("%", "%%").replace("%%2$s", "%2$s"));
    }
}
