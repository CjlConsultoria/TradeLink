package com.example.CJLInvestimentos.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cliente")
public class ClienteController {

    @GetMapping
    public String acessoCliente(){
        return "Acesso permitido: Cliente, Admin ou desenvolvedor";
    }
}
