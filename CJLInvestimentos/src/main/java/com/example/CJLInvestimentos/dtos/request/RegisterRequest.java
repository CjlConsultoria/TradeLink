package com.example.CJLInvestimentos.dtos.request;

import com.example.CJLInvestimentos.entities.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String nome;
    private String email;
    private String senha;
    private Role role;
}
