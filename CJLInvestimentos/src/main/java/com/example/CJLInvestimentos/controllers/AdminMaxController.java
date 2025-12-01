package com.example.CJLInvestimentos.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/adminmax")
public class AdminMaxController {

    @GetMapping
    public String acessoDev(){
        return "Acesso permitido: Desenvolvedor";

    }
}
