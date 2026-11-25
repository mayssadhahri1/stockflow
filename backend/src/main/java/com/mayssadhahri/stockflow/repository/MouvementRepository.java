package com.mayssadhahri.stockflow.repository;

import com.mayssadhahri.stockflow.entity.Mouvement;
import com.mayssadhahri.stockflow.entity.Produit;
import com.mayssadhahri.stockflow.entity.TypeMouvement;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MouvementRepository extends JpaRepository<Mouvement, Long> {

    // On charge le produit et l'auteur en une seule requête (évite le problème N+1)
    @EntityGraph(attributePaths = {"produit", "auteur"})
    List<Mouvement> findAllByOrderByDateDescIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"produit", "auteur"})
    List<Mouvement> findByProduitOrderByDateDescIdDesc(Produit produit, Pageable pageable);

    boolean existsByProduit(Produit produit);

    @Query("select coalesce(sum(m.quantite), 0L) from Mouvement m where m.type = :type and m.date >= :depuis")
    Long sumQuantite(@Param("type") TypeMouvement type, @Param("depuis") LocalDateTime depuis);
}
