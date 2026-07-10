package fr.rp.admin;

import fr.rp.admin.commands.AdminCommand;
import fr.rp.admin.commands.GradeCommand;
import fr.rp.admin.commands.SanctionCommand;
import fr.rp.admin.gui.AdminPanelGui;
import fr.rp.admin.listeners.GuiListener;
import fr.rp.admin.listeners.JoueurListener;
import fr.rp.admin.managers.DatabaseManager;
import fr.rp.admin.managers.GradeManager;
import fr.rp.admin.managers.SanctionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AdminRP extends JavaPlugin {

    private static AdminRP instance;

    private DatabaseManager databaseManager;
    private GradeManager gradeManager;
    private SanctionManager sanctionManager;
    private AdminPanelGui adminPanelGui;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        databaseManager.connecter();

        gradeManager = new GradeManager(this);
        gradeManager.charger();

        sanctionManager = new SanctionManager(this);
        adminPanelGui = new AdminPanelGui(this);

        // Commandes
        getCommand("ban").setExecutor(new SanctionCommand(this));
        getCommand("unban").setExecutor(new SanctionCommand(this));
        getCommand("mute").setExecutor(new SanctionCommand(this));
        getCommand("unmute").setExecutor(new SanctionCommand(this));
        getCommand("kick").setExecutor(new SanctionCommand(this));
        getCommand("warn").setExecutor(new SanctionCommand(this));
        getCommand("sanctions").setExecutor(new SanctionCommand(this));
        getCommand("grade").setExecutor(new GradeCommand(this));
        getCommand("grades").setExecutor(new GradeCommand(this));
        getCommand("admin").setExecutor(new AdminCommand(this));

        // Listeners
        getServer().getPluginManager().registerEvents(new JoueurListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        getLogger().info("AdminRP a ete active avec succes !");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) databaseManager.fermer();
        getLogger().info("AdminRP a ete desactive.");
    }

    public static AdminRP getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public GradeManager getGradeManager() {
        return gradeManager;
    }

    public SanctionManager getSanctionManager() {
        return sanctionManager;
    }

    public AdminPanelGui getAdminPanelGui() {
        return adminPanelGui;
    }
}
