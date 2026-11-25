package com.mayssadhahri.stockflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mayssadhahri.stockflow.entity.Role;
import com.mayssadhahri.stockflow.entity.User;
import com.mayssadhahri.stockflow.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.demo-data=false")
@AutoConfigureMockMvc
class StockFlowApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder encoder;

    private String admin;
    private String employe;

    @BeforeEach
    void setUp() throws Exception {
        // L'administrateur est créé directement en base : l'inscription publique donne toujours le rôle EMPLOYE
        String adminEmail = "admin-" + UUID.randomUUID() + "@test.tn";
        users.save(new User("Admin", adminEmail, encoder.encode("motdepasse123"), Role.ADMIN));
        admin = login(adminEmail);
        employe = register("employe-" + UUID.randomUUID() + "@test.tn");
    }

    @Test
    void sansJetonRenvoie401() throws Exception {
        mvc.perform(get("/api/produits")).andExpect(status().isUnauthorized());
    }

    @Test
    void unEmployeNePeutPasCreerDeProduit() throws Exception {
        mvc.perform(post("/api/produits").header("Authorization", employe)
                        .contentType(MediaType.APPLICATION_JSON).content(produitJson(ref(), 10, 2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void entreesEtSortiesMettentAJourLeStock() throws Exception {
        long id = creerProduit(ref(), 10, 3);

        mouvement(id, "SORTIE", 4).andExpect(status().isCreated())
                .andExpect(jsonPath("$.stockApres").value(6));
        mouvement(id, "ENTREE", 5).andExpect(status().isCreated())
                .andExpect(jsonPath("$.stockApres").value(11));

        mvc.perform(get("/api/produits/" + id).header("Authorization", employe))
                .andExpect(jsonPath("$.quantite").value(11));

        // Historique : stock initial + 2 mouvements, du plus récent au plus ancien
        mvc.perform(get("/api/mouvements").param("produitId", String.valueOf(id)).header("Authorization", employe))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].type").value("ENTREE"))
                .andExpect(jsonPath("$[2].motif").value("Stock initial"));
    }

    @Test
    void sortieSuperieureAuStockRenvoie409() throws Exception {
        long id = creerProduit(ref(), 3, 1);
        mouvement(id, "SORTIE", 5)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Stock insuffisant")));
        mvc.perform(get("/api/produits/" + id).header("Authorization", employe))
                .andExpect(jsonPath("$.quantite").value(3));
    }

    @Test
    void alerteQuandLeStockPasseSousLeSeuil() throws Exception {
        String reference = ref();
        long id = creerProduit(reference, 10, 5);
        mvc.perform(get("/api/produits/" + id).header("Authorization", employe))
                .andExpect(jsonPath("$.enAlerte").value(false));
        mouvement(id, "SORTIE", 6);
        mvc.perform(get("/api/produits").param("alerte", "true").param("q", reference).header("Authorization", employe))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].enAlerte").value(true));
    }

    @Test
    void referenceEnDoubleEtValidation() throws Exception {
        String reference = ref();
        creerProduit(reference, 1, 0);
        mvc.perform(post("/api/produits").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(produitJson(reference, 1, 0)))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/produits").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reference\":\"a b\",\"nom\":\"\",\"categorie\":\"X\",\"prixUnitaire\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.reference").exists())
                .andExpect(jsonPath("$.fields.nom").exists())
                .andExpect(jsonPath("$.fields.prixUnitaire").exists());
    }

    @Test
    void modificationEtSuppression() throws Exception {
        String reference = ref();
        long id = creerProduit(reference, 2, 1);
        mvc.perform(put("/api/produits/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(produitJson(reference, 999, 4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seuilAlerte").value(4))
                .andExpect(jsonPath("$.quantite").value(2)); // la quantité ne change pas par modification

        mvc.perform(delete("/api/produits/" + id).header("Authorization", admin)).andExpect(status().isConflict());
        mouvement(id, "SORTIE", 2);
        mvc.perform(delete("/api/produits/" + id).header("Authorization", admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/produits/" + id).header("Authorization", admin)).andExpect(status().isNotFound());
    }

    @Test
    void tableauDeBordEtExport() throws Exception {
        String reference = ref();
        creerProduit(reference, 4, 10);
        String body = mvc.perform(get("/api/dashboard").header("Authorization", employe))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode dash = mapper.readTree(body);
        assertThat(dash.get("nbProduits").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(dash.get("nbAlertes").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(dash.get("valeurParCategorie").has("Test")).isTrue();

        mvc.perform(get("/api/produits/export").header("Authorization", employe))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("inventaire.csv")))
                .andExpect(content().string(containsString(reference)));
    }

    // ---- utilitaires ----

    private static String ref() {
        return "T-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static String produitJson(String reference, int stock, int seuil) {
        return "{\"reference\":\"" + reference + "\",\"nom\":\"Produit test\",\"categorie\":\"Test\","
                + "\"prixUnitaire\":12.5,\"seuilAlerte\":" + seuil + ",\"stockInitial\":" + stock + "}";
    }

    private long creerProduit(String reference, int stock, int seuil) throws Exception {
        String body = mvc.perform(post("/api/produits").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(produitJson(reference, stock, seuil)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions mouvement(long id, String type, int quantite) throws Exception {
        return mvc.perform(post("/api/mouvements").header("Authorization", employe)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"produitId\":" + id + ",\"type\":\"" + type + "\",\"quantite\":" + quantite + "}"));
    }

    private String register(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Employé\",\"email\":\"" + email + "\",\"password\":\"motdepasse123\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(body).get("token").asText();
    }

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"motdepasse123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(body).get("token").asText();
    }
}
