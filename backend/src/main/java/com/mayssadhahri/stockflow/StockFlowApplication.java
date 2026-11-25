package com.mayssadhahri.stockflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// L'authentification est gérée par JWT : pas besoin de l'utilisateur en mémoire créé par défaut
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class StockFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockFlowApplication.class, args);
    }
}
