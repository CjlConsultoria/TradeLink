package com.example.CJLInvestimentos.dtos.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class EnviarEmailApresentacaoRequest {

    @NotEmpty(message = "Selecione pelo menos um usuario")
    private List<Long> userIds;
}
