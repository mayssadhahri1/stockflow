package com.mayssadhahri.stockflow.service;

import com.mayssadhahri.stockflow.dto.StockDtos.DashboardResponse;
import com.mayssadhahri.stockflow.dto.StockDtos.MouvementRequest;
import com.mayssadhahri.stockflow.dto.StockDtos.MouvementResponse;
import com.mayssadhahri.stockflow.dto.StockDtos.ProduitResponse;
import com.mayssadhahri.stockflow.entity.Mouvement;
import com.mayssadhahri.stockflow.entity.Produit;
import com.mayssadhahri.stockflow.entity.TypeMouvement;
import com.mayssadhahri.stockflow.entity.User;
import com.mayssadhahri.stockflow.exception.ConflictException;
import com.mayssadhahri.stockflow.repository.MouvementRepository;
import com.mayssadhahri.stockflow.repository.ProduitRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MouvementService {

    private final MouvementRepository mouvements;
    private final ProduitRepository produits;
    private final ProduitService produitService;

    public MouvementService(MouvementRepository mouvements, ProduitRepository produits, ProduitService produitService) {
        this.mouvements = mouvements;
        this.produits = produits;
        this.produitService = produitService;
    }

    /** Enregistre une entrée ou une sortie et met à jour le stock dans la même transaction. */
    @Transactional
    public MouvementResponse enregistrer(MouvementRequest r, User auteur) {
        Produit p = produitService.find(r.produitId());
        int nouveauStock = r.type() == TypeMouvement.ENTREE ? p.getQuantite() + r.quantite() : p.getQuantite() - r.quantite();
        if (nouveauStock < 0) {
            throw new ConflictException("Stock insuffisant : " + p.getQuantite() + " unité(s) disponible(s)");
        }
        p.setQuantite(nouveauStock);
        String motif = r.motif() == null || r.motif().isBlank() ? null : r.motif().trim();
        Mouvement m = mouvements.save(new Mouvement(p, r.type(), r.quantite(), nouveauStock, motif, auteur));
        return toResponse(m, p, auteur);
    }

    public List<MouvementResponse> historique(Long produitId, int limite) {
        PageRequest page = PageRequest.of(0, Math.min(Math.max(limite, 1), 200));
        List<Mouvement> list = produitId == null
                ? mouvements.findAllByOrderByDateDescIdDesc(page)
                : mouvements.findByProduitOrderByDateDescIdDesc(produitService.find(produitId), page);
        return list.stream().map(MouvementResponse::from).toList();
    }

    public DashboardResponse dashboard() {
        List<Produit> tous = produits.findAll();
        BigDecimal valeur = BigDecimal.ZERO;
        int unites = 0;
        Map<String, BigDecimal> parCategorie = new TreeMap<>();
        for (Produit p : tous) {
            valeur = valeur.add(p.valeurStock());
            unites += p.getQuantite();
            parCategorie.merge(p.getCategorie(), p.valeurStock(), BigDecimal::add);
        }
        List<ProduitResponse> alertes = produits.findEnAlerte().stream().map(ProduitResponse::from).toList();
        LocalDateTime depuis = LocalDateTime.now().minusDays(30);
        return new DashboardResponse(
                tous.size(),
                unites,
                valeur,
                alertes.size(),
                mouvements.sumQuantite(TypeMouvement.ENTREE, depuis),
                mouvements.sumQuantite(TypeMouvement.SORTIE, depuis),
                parCategorie,
                alertes,
                historique(null, 8));
    }

    // Construit la réponse sans recharger les relations paresseuses
    private static MouvementResponse toResponse(Mouvement m, Produit p, User auteur) {
        return new MouvementResponse(m.getId(), p.getId(), p.getReference(), p.getNom(), m.getType(), m.getQuantite(),
                m.getStockApres(), m.getMotif(), auteur.getNom(), m.getDate());
    }
}
