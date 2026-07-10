package fr.rp.admin.gui;

import fr.rp.admin.AdminRP;
import fr.rp.admin.models.Grade;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class AdminPanelGui {

    private final AdminRP plugin;

    public AdminPanelGui(AdminRP plugin) {
        this.plugin = plugin;
    }

    /** Ouvre le menu principal listant les joueurs connectes */
    public void ouvrirMenuPrincipal(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "⚡ Panel Admin - Joueurs en ligne");

        int slot = 0;
        for (Player cible : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break;
            ItemStack tete = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) tete.getItemMeta();
            meta.setOwningPlayer(cible);
            Grade grade = plugin.getGradeManager().getGradeJoueur(cible);

            meta.setDisplayName(grade.getCouleurNom() + cible.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Grade: " + grade.getPrefixe() + " " + grade.getLogo());
            lore.add(ChatColor.GRAY + "Warns: " + ChatColor.YELLOW + plugin.getDatabaseManager().getWarns(cible.getUniqueId()));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Clic gauche" + ChatColor.GRAY + " : gerer ce joueur");
            meta.setLore(lore);
            tete.setItemMeta(meta);

            inv.setItem(slot, tete);
            slot++;
        }

        // Item de fermeture
        ItemStack fermer = creerItem(Material.BARRIER, ChatColor.RED + "Fermer", List.of());
        inv.setItem(49, fermer);

        admin.openInventory(inv);
    }

    /** Ouvre le menu de gestion d'un joueur specifique */
    public void ouvrirMenuJoueur(Player admin, org.bukkit.OfflinePlayer cible) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_RED + "⚡ Gestion : " + cible.getName());

        Grade grade = plugin.getGradeManager().getGrade(plugin.getDatabaseManager().getGrade(cible.getUniqueId()));

        ItemStack info = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) info.getItemMeta();
        meta.setOwningPlayer(cible);
        meta.setDisplayName(grade.getCouleurNom() + cible.getName());
        meta.setLore(List.of(
                ChatColor.GRAY + "Grade actuel: " + grade.getPrefixe() + " " + grade.getLogo(),
                ChatColor.GRAY + "Warns: " + ChatColor.YELLOW + plugin.getDatabaseManager().getWarns(cible.getUniqueId())
        ));
        info.setItemMeta(meta);
        inv.setItem(4, info);

        inv.setItem(10, creerItem(Material.IRON_SWORD, ChatColor.GOLD + "Avertir (/warn)",
                List.of(ChatColor.GRAY + "Cliquer pour avertir ce joueur")));
        inv.setItem(11, creerItem(Material.COOKED_CHICKEN, ChatColor.YELLOW + "Rendre muet (/mute)",
                List.of(ChatColor.GRAY + "Mute 1h par defaut")));
        inv.setItem(12, creerItem(Material.LIME_DYE, ChatColor.GREEN + "Demuter (/unmute)",
                List.of(ChatColor.GRAY + "Retirer le mute")));
        inv.setItem(13, creerItem(Material.WOODEN_DOOR, ChatColor.GOLD + "Expulser (/kick)",
                List.of(ChatColor.GRAY + "Le joueur doit etre en ligne")));
        inv.setItem(14, creerItem(Material.BARRIER, ChatColor.RED + "Bannir (/ban)",
                List.of(ChatColor.GRAY + "Bannissement permanent")));
        inv.setItem(15, creerItem(Material.LIME_CONCRETE, ChatColor.GREEN + "Debannir (/unban)",
                List.of(ChatColor.GRAY + "Retirer le bannissement")));
        inv.setItem(16, creerItem(Material.BOOK, ChatColor.AQUA + "Historique (/sanctions)",
                List.of(ChatColor.GRAY + "Voir toutes les sanctions")));

        // Ligne du bas : changer de grade
        int slot = 19;
        for (Grade g : plugin.getGradeManager().getGrades().values()) {
            if (slot > 25) break;
            inv.setItem(slot, creerItem(Material.NAME_TAG, g.getCouleurNom() + "Grade: " + g.getId(),
                    List.of(ChatColor.GRAY + "Cliquer pour attribuer ce grade", g.getPrefixe() + " " + g.getLogo())));
            slot++;
        }

        inv.setItem(26, creerItem(Material.ARROW, ChatColor.WHITE + "Retour", List.of()));

        admin.openInventory(inv);
    }

    private ItemStack creerItem(Material material, String nom, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(nom);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
