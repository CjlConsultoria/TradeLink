package com.example.CJLInvestimentos.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @GetMapping("/dev")
    public String acessoDev(){
        return "Acesso permitido: Desenvolvedor";
    }
}
