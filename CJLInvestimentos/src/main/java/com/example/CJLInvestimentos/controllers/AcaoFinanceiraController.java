package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.dtos.request.AcaoFinanceiraRequest;
import com.example.CJLInvestimentos.dtos.response.AcaoFinanceiraResponse;
import com.example.CJLInvestimentos.services.AcaoFinanceiraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/acoes")
@RequiredArgsConstructor
public class AcaoFinanceiraController {

    private final AcaoFinanceiraService service;

    @PostMapping
    public ResponseEntity<AcaoFinanceiraResponse> criar(
            @RequestBody AcaoFinanceiraRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.salvar(request));
    }
}
