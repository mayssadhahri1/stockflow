package com.mayssadhahri.stockflow;

import static org.assertj.core.api.Assertions.assertThat;

import com.mayssadhahri.stockflow.entity.Role;
import com.mayssadhahri.stockflow.entity.User;
import com.mayssadhahri.stockflow.security.JwtService;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("une-cle-de-test-suffisamment-longue-pour-hs256".getBytes());

    @Test
    void jetonValideContientLEmail() {
        JwtService jwt = new JwtService(SECRET, 60);
        String token = jwt.generateToken(new User("Test", "test@test.tn", "x", Role.EMPLOYE));
        assertThat(jwt.extractEmail(token)).contains("test@test.tn");
    }

    @Test
    void jetonExpireEstRefuse() {
        JwtService jwt = new JwtService(SECRET, -1);
        String token = jwt.generateToken(new User("Test", "test@test.tn", "x", Role.EMPLOYE));
        assertThat(jwt.extractEmail(token)).isEmpty();
    }

    @Test
    void jetonSigneAvecUneAutreCleEstRefuse() {
        String autreCle = Base64.getEncoder().encodeToString("une-autre-cle-totalement-differente-de-32-octets".getBytes());
        String token = new JwtService(autreCle, 60).generateToken(new User("T", "t@t.tn", "x", Role.EMPLOYE));
        assertThat(new JwtService(SECRET, 60).extractEmail(token)).isEmpty();
        assertThat(new JwtService(SECRET, 60).extractEmail("n-importe-quoi")).isEmpty();
    }
}
