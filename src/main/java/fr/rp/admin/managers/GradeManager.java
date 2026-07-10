package fr.rp.admin.managers;

import fr.rp.admin.AdminRP;
import fr.rp.admin.models.Grade;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class GradeManager {

    private final AdminRP plugin;
    private final Map<String, Grade> grades = new LinkedHashMap<>();
    private String gradeParDefaut = "joueur";

    public GradeManager(AdminRP plugin) {
        this.plugin = plugin;
    }

    public void charger() {
        grades.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("grades");
        if (section == null) {
            plugin.getLogger().warning("Aucune section 'grades' trouvee dans config.yml !");
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection g = section.getConfigurationSection(id);
            if (g == null) continue;
            Grade grade = new Grade(
                    id,
                    g.getInt("priorite", 0),
                    traduire(g.getString("prefixe", "")),
                    g.getString("logo", ""),
                    traduire(g.getString("couleur-nom", "&7")),
                    g.getStringList("permissions"),
                    g.getBoolean("par-defaut", false)
            );
            grades.put(id, grade);
            if (grade.isParDefaut()) gradeParDefaut = id;
        }
        plugin.getLogger().info("Grades charges : " + grades.size());
    }

    private String traduire(String texte) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', texte);
    }

    public Grade getGrade(String id) {
        return grades.getOrDefault(id, grades.get(gradeParDefaut));
    }

    public Grade getGradeJoueur(Player joueur) {
        String id = plugin.getDatabaseManager().getGrade(joueur.getUniqueId());
        return getGrade(id);
    }

    public boolean existe(String id) {
        return grades.containsKey(id);
    }

    public Map<String, Grade> getGrades() {
        return grades;
    }

    public String getGradeParDefaut() {
        return gradeParDefaut;
    }

    public String formaterAffichage(Player joueur) {
        Grade grade = getGradeJoueur(joueur);
        return grade.getPrefixe() + " " + grade.getLogo() + " " + grade.getCouleurNom() + joueur.getName();
    }

    public String formaterChat(Player joueur, String message) {
        Grade grade = getGradeJoueur(joueur);
        String format = plugin.getConfig().getString("format-chat",
                "{prefixe} {logo} {couleur}{joueur}&r: {message}");
        format = format.replace("{prefixe}", grade.getPrefixe())
                .replace("{logo}", grade.getLogo())
                .replace("{couleur}", grade.getCouleurNom())
                .replace("{joueur}", joueur.getName())
                .replace("{message}", message);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', format);
    }

    public void appliquerPermissions(Player joueur) {
        Grade grade = getGradeJoueur(joueur);
        if (grade == null) return;
        // Retire les anciennes attachments AdminRP puis reapplique
        joueur.recalculatePermissions();
        var attachment = joueur.addAttachment(plugin);
        for (String perm : grade.getPermissions()) {
            attachment.setPermission(perm, true);
        }
    }
}
