package com.mayssadhahri.stockflow.service;

import com.mayssadhahri.stockflow.dto.StockDtos.ProduitRequest;
import com.mayssadhahri.stockflow.dto.StockDtos.ProduitResponse;
import com.mayssadhahri.stockflow.entity.Mouvement;
import com.mayssadhahri.stockflow.entity.Produit;
import com.mayssadhahri.stockflow.entity.TypeMouvement;
import com.mayssadhahri.stockflow.entity.User;
import com.mayssadhahri.stockflow.exception.ConflictException;
import com.mayssadhahri.stockflow.exception.NotFoundException;
import com.mayssadhahri.stockflow.repository.MouvementRepository;
import com.mayssadhahri.stockflow.repository.ProduitRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProduitService {

    private final ProduitRepository produits;
    private final MouvementRepository mouvements;

    public ProduitService(ProduitRepository produits, MouvementRepository mouvements) {
        this.produits = produits;
        this.mouvements = mouvements;
    }

    public List<ProduitResponse> list(String recherche, String categorie, boolean alerteSeulement) {
        String q = recherche == null ? "" : recherche.trim().toLowerCase(Locale.ROOT);
        return produits.findAllByOrderByNomAsc().stream()
                .filter(p -> q.isEmpty()
                        || p.getNom().toLowerCase(Locale.ROOT).contains(q)
                        || p.getReference().toLowerCase(Locale.ROOT).contains(q))
                .filter(p -> categorie == null || categorie.isBlank() || p.getCategorie().equalsIgnoreCase(categorie))
                .filter(p -> !alerteSeulement || p.enAlerte())
                .map(ProduitResponse::from)
                .toList();
    }

    public List<String> categories() {
        return produits.findCategories();
    }

    public ProduitResponse get(Long id) {
        return ProduitResponse.from(find(id));
    }

    @Transactional
    public ProduitResponse create(ProduitRequest r, User auteur) {
        String reference = r.reference().trim().toUpperCase(Locale.ROOT);
        if (produits.existsByReferenceIgnoreCase(reference)) {
            throw new ConflictException("La référence " + reference + " existe déjà");
        }
        Produit p = produits.save(new Produit(reference, r.nom().trim(), r.categorie().trim(), r.prixUnitaire(),
                r.stockInitial(), r.seuilAlerte()));
        // Le stock initial est tracé comme une première entrée
        if (r.stockInitial() > 0) {
            mouvements.save(new Mouvement(p, TypeMouvement.ENTREE, r.stockInitial(), r.stockInitial(), "Stock initial", auteur));
        }
        return ProduitResponse.from(p);
    }

    @Transactional
    public ProduitResponse update(Long id, ProduitRequest r) {
        Produit p = find(id);
        String reference = r.reference().trim().toUpperCase(Locale.ROOT);
        if (produits.existsByReferenceIgnoreCaseAndIdNot(reference, id)) {
            throw new ConflictException("La référence " + reference + " existe déjà");
        }
        p.setReference(reference);
        p.setNom(r.nom().trim());
        p.setCategorie(r.categorie().trim());
        p.setPrixUnitaire(r.prixUnitaire());
        p.setSeuilAlerte(r.seuilAlerte());
        // La quantité n'est pas modifiable ici : elle évolue uniquement par des mouvements tracés
        return ProduitResponse.from(p);
    }

    @Transactional
    public void delete(Long id) {
        Produit p = find(id);
        if (mouvements.existsByProduit(p) && p.getQuantite() > 0) {
            throw new ConflictException("Impossible de supprimer un produit encore en stock");
        }
        mouvements.deleteAll(mouvements.findByProduitOrderByDateDescIdDesc(p, Pageable.unpaged()));
        produits.delete(p);
    }

    /** Export CSV de l'inventaire (séparateur « ; » pour Excel en français). */
    public String exportCsv() {
        StringBuilder sb = new StringBuilder("Référence;Nom;Catégorie;Prix unitaire;Quantité;Seuil d'alerte;Valeur du stock\n");
        for (Produit p : produits.findAllByOrderByNomAsc()) {
            sb.append(csv(p.getReference())).append(';')
                    .append(csv(p.getNom())).append(';')
                    .append(csv(p.getCategorie())).append(';')
                    .append(p.getPrixUnitaire().toPlainString()).append(';')
                    .append(p.getQuantite()).append(';')
                    .append(p.getSeuilAlerte()).append(';')
                    .append(p.valeurStock().toPlainString()).append('\n');
        }
        return sb.toString();
    }

    Produit find(Long id) {
        return produits.findById(id).orElseThrow(() -> new NotFoundException("Produit introuvable"));
    }

    private static String csv(String value) {
        // Les guillemets protègent les valeurs qui contiennent « ; »
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
