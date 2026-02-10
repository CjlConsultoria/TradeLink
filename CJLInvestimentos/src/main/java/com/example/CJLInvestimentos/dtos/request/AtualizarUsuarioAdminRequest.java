package com.example.CJLInvestimentos.dtos.request;

import com.example.CJLInvestimentos.entities.enums.Role;
import lombok.Data;

@Data
public class AtualizarUsuarioAdminRequest {
    private String nome;
    private String email;
    private Boolean ativo;
    private Role role;
    private Long empresaId;
}
