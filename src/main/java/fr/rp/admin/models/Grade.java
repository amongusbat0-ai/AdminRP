package fr.rp.admin.models;

import java.util.List;

public class Grade {

    private final String id;
    private final int priorite;
    private final String prefixe;
    private final String logo;
    private final String couleurNom;
    private final List<String> permissions;
    private final boolean parDefaut;

    public Grade(String id, int priorite, String prefixe, String logo, String couleurNom,
                 List<String> permissions, boolean parDefaut) {
        this.id = id;
        this.priorite = priorite;
        this.prefixe = prefixe;
        this.logo = logo;
        this.couleurNom = couleurNom;
        this.permissions = permissions;
        this.parDefaut = parDefaut;
    }

    public String getId() { return id; }
    public int getPriorite() { return priorite; }
    public String getPrefixe() { return prefixe; }
    public String getLogo() { return logo; }
    public String getCouleurNom() { return couleurNom; }
    public List<String> getPermissions() { return permissions; }
    public boolean isParDefaut() { return parDefaut; }
}
