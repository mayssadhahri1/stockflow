package com.mayssadhahri.stockflow.config;

import com.mayssadhahri.stockflow.dto.StockDtos.MouvementRequest;
import com.mayssadhahri.stockflow.dto.StockDtos.ProduitRequest;
import com.mayssadhahri.stockflow.dto.StockDtos.ProduitResponse;
import com.mayssadhahri.stockflow.entity.Role;
import com.mayssadhahri.stockflow.entity.TypeMouvement;
import com.mayssadhahri.stockflow.entity.User;
import com.mayssadhahri.stockflow.repository.UserRepository;
import com.mayssadhahri.stockflow.service.MouvementService;
import com.mayssadhahri.stockflow.service.ProduitService;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Crée deux comptes de démonstration et un petit catalogue (désactivable avec APP_DEMO_DATA=false). */
@Component
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final ProduitService produits;
    private final MouvementService mouvements;

    public DemoDataInitializer(UserRepository users, PasswordEncoder encoder, ProduitService produits,
                               MouvementService mouvements) {
        this.users = users;
        this.encoder = encoder;
        this.produits = produits;
        this.mouvements = mouvements;
    }

    @Override
    public void run(String... args) {
        if (users.existsByEmail("admin@stockflow.tn")) {
            return;
        }
        User admin = users.save(new User("Admin Démo", "admin@stockflow.tn", encoder.encode("admin1234"), Role.ADMIN));
        User employe = users.save(new User("Employé Démo", "employe@stockflow.tn", encoder.encode("employe1234"), Role.EMPLOYE));

        ProduitResponse laptop = produit(admin, "PC-LEN-T14", "Ordinateur portable Lenovo T14", "Informatique", "2890", 12, 5);
        ProduitResponse ecran = produit(admin, "ECR-DELL-24", "Écran Dell 24 pouces", "Informatique", "520", 25, 8);
        ProduitResponse souris = produit(admin, "SOU-LOG-M185", "Souris sans fil Logitech", "Accessoires", "45.5", 60, 20);
        ProduitResponse papier = produit(admin, "PAP-A4-500", "Ramette papier A4 (500 feuilles)", "Fournitures", "14.9", 40, 30);
        ProduitResponse cable = produit(admin, "CAB-HDMI-2M", "Câble HDMI 2 m", "Accessoires", "18", 30, 10);
        produit(admin, "CHA-ERGO-01", "Chaise de bureau ergonomique", "Mobilier", "450", 6, 2);

        sortie(employe, laptop, 9, "Équipement nouveaux stagiaires");
        sortie(employe, papier, 15, "Service comptabilité");
        sortie(employe, souris, 12, "Remplacement");
        sortie(employe, cable, 24, "Salle de réunion");
        mouvements.enregistrer(new MouvementRequest(ecran.id(), TypeMouvement.ENTREE, 10, "Réception commande fournisseur"), admin);
        log.info("Comptes de démonstration : admin@stockflow.tn / admin1234 et employe@stockflow.tn / employe1234");
    }

    private ProduitResponse produit(User admin, String ref, String nom, String categorie, String prix, int stock, int seuil) {
        return produits.create(new ProduitRequest(ref, nom, categorie, new BigDecimal(prix), seuil, stock), admin);
    }

    private void sortie(User auteur, ProduitResponse p, int quantite, String motif) {
        mouvements.enregistrer(new MouvementRequest(p.id(), TypeMouvement.SORTIE, quantite, motif), auteur);
    }
}
