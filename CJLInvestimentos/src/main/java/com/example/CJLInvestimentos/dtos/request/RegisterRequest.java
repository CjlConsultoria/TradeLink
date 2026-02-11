package com.example.CJLInvestimentos.dtos.request;

import com.example.CJLInvestimentos.entities.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150)
    private String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;

    /** Telefone para WhatsApp (ex: 11999999999). */
    @Size(max = 30)
    private String telefone;

    private Role role;
    private Long empresaId;
}
