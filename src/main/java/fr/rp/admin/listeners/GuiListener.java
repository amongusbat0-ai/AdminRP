package fr.rp.admin.listeners;

import fr.rp.admin.AdminRP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiListener implements Listener {

    private final AdminRP plugin;
    // Retient quel joueur cible est actuellement gere par quel admin
    private final Map<UUID, UUID> cibleActuelle = new HashMap<>();

    public GuiListener(AdminRP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String titre = event.getView().getTitle();
        if (!titre.contains("⚡")) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player admin)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (titre.contains("Joueurs en ligne")) {
            if (item.getType() == Material.BARRIER) {
                admin.closeInventory();
                return;
            }
            if (item.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (meta != null && meta.getOwningPlayer() != null) {
                    OfflinePlayer cible = meta.getOwningPlayer();
                    cibleActuelle.put(admin.getUniqueId(), cible.getUniqueId());
                    plugin.getAdminPanelGui().ouvrirMenuJoueur(admin, cible);
                }
            }
            return;
        }

        if (titre.startsWith(ChatColor.DARK_RED + "⚡ Gestion")) {
            UUID cibleUuid = cibleActuelle.get(admin.getUniqueId());
            if (cibleUuid == null) return;
            OfflinePlayer cible = Bukkit.getOfflinePlayer(cibleUuid);

            switch (item.getType()) {
                case ARROW -> plugin.getAdminPanelGui().ouvrirMenuPrincipal(admin);
                case IRON_SWORD -> {
                    admin.closeInventory();
                    plugin.getSanctionManager().warn(cible, admin, "Avertissement via panel admin");
                    admin.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a recu un avertissement.");
                }
                case COOKED_CHICKEN -> {
                    admin.closeInventory();
                    plugin.getSanctionManager().mute(cible, admin, "Mute via panel admin", 3_600_000L);
                    admin.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a ete mute 1h.");
                }
                case LIME_DYE -> {
                    admin.closeInventory();
                    plugin.getSanctionManager().unmute(cible);
                    admin.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a ete demute.");
                }
                case WOODEN_DOOR -> {
                    admin.closeInventory();
                    Player enLigne = cible.getPlayer();
                    if (enLigne != null) {
                        plugin.getSanctionManager().kick(enLigne, admin, "Expulsion via panel admin");
                        admin.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a ete expulse.");
                    } else {
                        admin.sendMessage(ChatColor.RED + "Ce joueur n'est pas en ligne.");
                    }
                }
                case BARRIER -> {
                    admin.closeInventory();
                    plugin.getSanctionManager().bannir(cible, admin, "Bannissement via panel admin", -1);
                    admin.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a ete banni.");
                }
                case LIME_CONCRETE -> {
                    admin.closeInventory();
                    plugin.getSanctionManager().debannir(cible);
                    admin.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " a ete debanni.");
                }
                case BOOK -> {
                    admin.closeInventory();
                    Bukkit.dispatchCommand(admin, "sanctions " + cible.getName());
                }
                case NAME_TAG -> {
                    String nomAffiche = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                    String gradeId = nomAffiche.replace("Grade: ", "").trim();
                    if (plugin.getGradeManager().existe(gradeId)) {
                        plugin.getDatabaseManager().assurerJoueur(cible.getUniqueId(), cible.getName());
                        plugin.getDatabaseManager().setGrade(cible.getUniqueId(), gradeId);
                        Player enLigne = cible.getPlayer();
                        if (enLigne != null) plugin.getGradeManager().appliquerPermissions(enLigne);
                        admin.sendMessage(ChatColor.GREEN + "✔ Grade " + gradeId + " attribue a " + cible.getName() + ".");
                        plugin.getAdminPanelGui().ouvrirMenuJoueur(admin, cible);
                    }
                }
                default -> {}
            }
        }
    }
}
