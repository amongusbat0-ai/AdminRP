package fr.rp.admin.models;

import java.util.UUID;

public class Sanction {

    public enum Type {
        BAN, MUTE, KICK, WARN
    }

    private final int id;
    private final UUID joueur;
    private final String pseudoJoueur;
    private final UUID auteur;
    private final String pseudoAuteur;
    private final Type type;
    private final String raison;
    private final long dateDebut;
    private final long dateFin; // -1 = permanent
    private boolean active;

    public Sanction(int id, UUID joueur, String pseudoJoueur, UUID auteur, String pseudoAuteur,
                     Type type, String raison, long dateDebut, long dateFin, boolean active) {
        this.id = id;
        this.joueur = joueur;
        this.pseudoJoueur = pseudoJoueur;
        this.auteur = auteur;
        this.pseudoAuteur = pseudoAuteur;
        this.type = type;
        this.raison = raison;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.active = active;
    }

    public int getId() { return id; }
    public UUID getJoueur() { return joueur; }
    public String getPseudoJoueur() { return pseudoJoueur; }
    public UUID getAuteur() { return auteur; }
    public String getPseudoAuteur() { return pseudoAuteur; }
    public Type getType() { return type; }
    public String getRaison() { return raison; }
    public long getDateDebut() { return dateDebut; }
    public long getDateFin() { return dateFin; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isPermanent() { return dateFin == -1; }

    public boolean isExpiree() {
        if (isPermanent()) return false;
        return System.currentTimeMillis() > dateFin;
    }
}
