package com.example.domino;

public class Player {
    private int id; // Identifiant unique du joueur
    private String name; // Nom du joueur

    public Player() {
        // Constructeur vide requis par SQLite
    }

    public Player(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
