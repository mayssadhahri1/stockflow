package com.mayssadhahri.stockflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Historique des entrées et sorties : le stock d'un produit n'est jamais modifié sans mouvement. */
@Entity
@Table(name = "mouvements")
public class Mouvement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TypeMouvement type;

    @Column(nullable = false)
    private int quantite;

    // Stock du produit juste après ce mouvement (pratique pour l'historique)
    @Column(nullable = false)
    private int stockApres;

    private String motif;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auteur_id")
    private User auteur;

    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    protected Mouvement() {
    }

    public Mouvement(Produit produit, TypeMouvement type, int quantite, int stockApres, String motif, User auteur) {
        this.produit = produit;
        this.type = type;
        this.quantite = quantite;
        this.stockApres = stockApres;
        this.motif = motif;
        this.auteur = auteur;
    }

    public Long getId() { return id; }
    public Produit getProduit() { return produit; }
    public TypeMouvement getType() { return type; }
    public int getQuantite() { return quantite; }
    public int getStockApres() { return stockApres; }
    public String getMotif() { return motif; }
    public User getAuteur() { return auteur; }
    public LocalDateTime getDate() { return date; }
}
