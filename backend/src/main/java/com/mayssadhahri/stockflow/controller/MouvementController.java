package com.mayssadhahri.stockflow.controller;

import com.mayssadhahri.stockflow.dto.StockDtos.DashboardResponse;
import com.mayssadhahri.stockflow.dto.StockDtos.MouvementRequest;
import com.mayssadhahri.stockflow.dto.StockDtos.MouvementResponse;
import com.mayssadhahri.stockflow.entity.User;
import com.mayssadhahri.stockflow.service.MouvementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MouvementController {

    private final MouvementService service;

    public MouvementController(MouvementService service) {
        this.service = service;
    }

    @PostMapping("/mouvements")
    @ResponseStatus(HttpStatus.CREATED)
    public MouvementResponse enregistrer(@Valid @RequestBody MouvementRequest request, @AuthenticationPrincipal User user) {
        return service.enregistrer(request, user);
    }

    @GetMapping("/mouvements")
    public List<MouvementResponse> historique(@RequestParam(required = false) Long produitId,
                                              @RequestParam(defaultValue = "50") int limite) {
        return service.historique(produitId, limite);
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return service.dashboard();
    }
}
