package com.mayssadhahri.stockflow.repository;

import com.mayssadhahri.stockflow.entity.Produit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    boolean existsByReferenceIgnoreCase(String reference);

    boolean existsByReferenceIgnoreCaseAndIdNot(String reference, Long id);

    List<Produit> findAllByOrderByNomAsc();

    @Query("select p from Produit p where p.quantite <= p.seuilAlerte order by p.quantite asc")
    List<Produit> findEnAlerte();

    @Query("select distinct p.categorie from Produit p order by p.categorie")
    List<String> findCategories();
}
