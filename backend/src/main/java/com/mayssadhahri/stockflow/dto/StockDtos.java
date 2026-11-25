package com.mayssadhahri.stockflow.dto;

import com.mayssadhahri.stockflow.entity.Mouvement;
import com.mayssadhahri.stockflow.entity.Produit;
import com.mayssadhahri.stockflow.entity.TypeMouvement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Objets échangés par l'API de gestion de stock. */
public final class StockDtos {

    private StockDtos() {
    }

    public record ProduitRequest(
            @NotBlank(message = "La référence est obligatoire")
            @Pattern(regexp = "^[A-Za-z0-9-]{2,40}$", message = "Référence : lettres, chiffres et tirets (2 à 40)")
            String reference,
            @NotBlank(message = "Le nom est obligatoire") @Size(max = 120) String nom,
            @NotBlank(message = "La catégorie est obligatoire") @Size(max = 60) String categorie,
            @NotNull(message = "Le prix est obligatoire") @DecimalMin(value = "0.0", message = "Le prix doit être positif")
            @Digits(integer = 9, fraction = 3) BigDecimal prixUnitaire,
            @Min(value = 0, message = "Le seuil doit être positif") @Max(100000) int seuilAlerte,
            // Stock initial : utilisé uniquement à la création (ensuite, le stock change par mouvements)
            @Min(value = 0, message = "Le stock initial doit être positif") @Max(1000000) int stockInitial) {
    }

    public record ProduitResponse(
            Long id,
            String reference,
            String nom,
            String categorie,
            BigDecimal prixUnitaire,
            int quantite,
            int seuilAlerte,
            boolean enAlerte,
            BigDecimal valeurStock) {

        public static ProduitResponse from(Produit p) {
            return new ProduitResponse(p.getId(), p.getReference(), p.getNom(), p.getCategorie(), p.getPrixUnitaire(),
                    p.getQuantite(), p.getSeuilAlerte(), p.enAlerte(), p.valeurStock());
        }
    }

    public record MouvementRequest(
            @NotNull(message = "Le produit est obligatoire") Long produitId,
            @NotNull(message = "Le type est obligatoire") TypeMouvement type,
            @Min(value = 1, message = "La quantité doit être au moins 1") @Max(1000000) int quantite,
            @Size(max = 200) String motif) {
    }

    public record MouvementResponse(
            Long id,
            Long produitId,
            String produitReference,
            String produitNom,
            TypeMouvement type,
            int quantite,
            int stockApres,
            String motif,
            String auteur,
            LocalDateTime date) {

        public static MouvementResponse from(Mouvement m) {
            return new MouvementResponse(m.getId(), m.getProduit().getId(), m.getProduit().getReference(),
                    m.getProduit().getNom(), m.getType(), m.getQuantite(), m.getStockApres(), m.getMotif(),
                    m.getAuteur().getNom(), m.getDate());
        }
    }

    public record DashboardResponse(
            long nbProduits,
            int unitesEnStock,
            BigDecimal valeurStock,
            long nbAlertes,
            long entrees30j,
            long sorties30j,
            Map<String, BigDecimal> valeurParCategorie,
            List<ProduitResponse> alertes,
            List<MouvementResponse> derniersMouvements) {
    }
}
