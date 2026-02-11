package com.example.CJLInvestimentos.dtos.request;

import com.example.CJLInvestimentos.entities.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AtualizarUsuarioAdminRequest {
    @Size(max = 150)
    private String nome;
    @Email(message = "E-mail inválido")
    @Size(max = 150)
    private String email;
    /** Telefone para WhatsApp. */
    @Size(max = 30)
    private String telefone;
    private Boolean ativo;
    private Role role;
    private Long empresaId;
}
