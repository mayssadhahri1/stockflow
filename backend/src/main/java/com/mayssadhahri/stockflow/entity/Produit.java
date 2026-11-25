package com.mayssadhahri.stockflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produits")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Référence interne unique, ex. « PC-LEN-T14 »
    @Column(nullable = false, unique = true, length = 40)
    private String reference;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, length = 60)
    private String categorie;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal prixUnitaire;

    @Column(nullable = false)
    private int quantite;

    // En dessous de ce seuil, le produit apparaît dans les alertes
    @Column(nullable = false)
    private int seuilAlerte;

    // Verrouillage optimiste : empêche deux mises à jour simultanées d'écraser le stock
    @Version
    private Long version;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    protected Produit() {
    }

    public Produit(String reference, String nom, String categorie, BigDecimal prixUnitaire, int quantite, int seuilAlerte) {
        this.reference = reference;
        this.nom = nom;
        this.categorie = categorie;
        this.prixUnitaire = prixUnitaire;
        this.quantite = quantite;
        this.seuilAlerte = seuilAlerte;
    }

    public boolean enAlerte() {
        return quantite <= seuilAlerte;
    }

    public BigDecimal valeurStock() {
        return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public int getSeuilAlerte() { return seuilAlerte; }
    public void setSeuilAlerte(int seuilAlerte) { this.seuilAlerte = seuilAlerte; }
    public LocalDateTime getDateCreation() { return dateCreation; }
}
