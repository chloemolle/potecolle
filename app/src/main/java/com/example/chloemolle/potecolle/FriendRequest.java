package com.example.chloemolle.potecolle;

public class FriendRequest {
    private String id;
    private String email;
    private String username;
    private Boolean demande;
    private Boolean pending;
    private Boolean vu;
    private Boolean accepte;

    public FriendRequest(){}

    public Boolean getAccepte() {
        return accepte;
    }

    public Boolean getVu() {
        return vu;
    }

    public Boolean getDemande() {
        return demande;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public Boolean getPending() {
        return pending;
    }

    public String getUsername() {
        return username;
    }

    public void setAccepte(Boolean accepte) {
        this.accepte = accepte;
    }

    public void setDemande(Boolean demande) {
        this.demande = demande;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setVu(Boolean vu) {
        this.vu = vu;
    }
}
