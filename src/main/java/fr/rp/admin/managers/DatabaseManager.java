package fr.rp.admin.managers;

import fr.rp.admin.AdminRP;
import fr.rp.admin.models.Sanction;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final AdminRP plugin;
    private Connection connection;

    public DatabaseManager(AdminRP plugin) {
        this.plugin = plugin;
    }

    public void connecter() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "adminrp.db");
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            creerTables();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Impossible de se connecter a la base de donnees", e);
        }
    }

    private void creerTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS sanctions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    joueur_uuid TEXT NOT NULL,
                    joueur_pseudo TEXT NOT NULL,
                    auteur_uuid TEXT,
                    auteur_pseudo TEXT,
                    type TEXT NOT NULL,
                    raison TEXT,
                    date_debut INTEGER NOT NULL,
                    date_fin INTEGER NOT NULL,
                    active INTEGER NOT NULL DEFAULT 1
                )
            """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS joueurs (
                    uuid TEXT PRIMARY KEY,
                    pseudo TEXT NOT NULL,
                    grade TEXT NOT NULL DEFAULT 'joueur',
                    warns INTEGER NOT NULL DEFAULT 0
                )
            """);
        }
    }

    public void fermer() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erreur fermeture base de donnees", e);
        }
    }

    // ---------- Joueurs / Grades ----------

    public void assurerJoueur(UUID uuid, String pseudo) {
        String sql = "INSERT INTO joueurs (uuid, pseudo, grade, warns) VALUES (?, ?, 'joueur', 0) " +
                "ON CONFLICT(uuid) DO UPDATE SET pseudo = excluded.pseudo";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, pseudo);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erreur assurerJoueur", e);
        }
    }

    public String getGrade(UUID uuid) {
        String sql = "SELECT grade FROM joueurs WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("grade");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erreur getGrade", e);
        }
        return "joueur";
    }

    public void setGrade(UUID uuid, String grade) {
        String sql = "UPDATE joueurs SET grade = ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, grade);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erreur setGrade", e);
        }
    }

    public int incrementerWarn(UUID uuid) {
        String sql = "UPDATE joueurs SET warns = warns + 1 WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erreur incrementerWarn", e);
        }
        return getWarns(uuid);
    }

    public int getWarns(UUID uuid) {
        String sql = "SELECT warns FROM joueurs WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("warns");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erreur getWarns", e);
        }
        return 0;
    }

    // ---------- Sanctions ----------

    public void ajouterSanction(Sanction s) {
        String sql = "INSERT INTO sanctions (joueur_uuid, joueur_pseudo, auteur_uuid, auteur_pseudo, type, raison, date_debut, date_fin, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s.getJoueur().toString());
            ps.setString(2, s.getPseudoJoueur());
            ps.setString(3, s.getAuteur() != null ? s.getAuteur().toString() : null);
            ps.setString(4, s.getPseudoAuteur());
            ps.setString(5, s.getType().name());
            ps.setString(6, s.getRaison());
            ps.setLong(7, s.getDateDebut());
            ps.setLong(8, s.getDateFin());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erreur ajouterSanction", e);
        }
    }

    public void desactiverSanctionsActives(UUID uuid, Sanction.Type type) {
        String sql = "UPDATE sanctions SET active = 0 WHERE joueur_uuid = ? AND type = ? AND active = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, type.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erreur desactiverSanctionsActives", e);
        }
    }

    public Sanction getSanctionActive(UUID uuid, Sanction.Type type) {
        String sql = "SELECT * FROM sanctions WHERE joueur_uuid = ? AND type = ? AND active = 1 ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, type.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Sanction s = mapSanction(rs);
                if (s.isExpiree()) {
                    desactiverSanctionsActives(uuid, type);
                    return null;
                }
                return s;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erreur getSanctionActive", e);
        }
        return null;
    }

    public List<Sanction> getHistorique(UUID uuid) {
        List<Sanction> liste = new ArrayList<>();
        String sql = "SELECT * FROM sanctions WHERE joueur_uuid = ? ORDER BY id DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapSanction(rs));
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erreur getHistorique", e);
        }
        return liste;
    }

    private Sanction mapSanction(ResultSet rs) throws SQLException {
        String auteurUuid = rs.getString("auteur_uuid");
        return new Sanction(
                rs.getInt("id"),
                UUID.fromString(rs.getString("joueur_uuid")),
                rs.getString("joueur_pseudo"),
                auteurUuid != null ? UUID.fromString(auteurUuid) : null,
                rs.getString("auteur_pseudo"),
                Sanction.Type.valueOf(rs.getString("type")),
                rs.getString("raison"),
                rs.getLong("date_debut"),
                rs.getLong("date_fin"),
                rs.getInt("active") == 1
        );
    }
}
