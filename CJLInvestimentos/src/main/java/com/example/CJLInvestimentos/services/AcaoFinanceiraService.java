package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.AcaoFinanceiraRequest;
import com.example.CJLInvestimentos.dtos.response.AcaoFinanceiraResponse;
import com.example.CJLInvestimentos.entities.AcaoFinanceira;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.AcaoFinanceiraRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AcaoFinanceiraService {

    private final AcaoFinanceiraRepository repository;
    private final UserRepository userRepository;

    public AcaoFinanceiraResponse salvar(AcaoFinanceiraRequest request) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        AcaoFinanceira acao = repository.save(
                AcaoFinanceira.builder()
                        .ativo(request.getAtivo())
                        .valor(request.getValor())
                        .quantidadeAcoes(request.getQuantidadeAcoes())
                        .data(request.getData())
                        .lucroPrejuizo(request.getLucroPrejuizo())
                        .user(user)
                        .build()
        );

        return AcaoFinanceiraResponse.builder()
                .id(acao.getId())
                .ativo(acao.getAtivo())
                .valor(acao.getValor())
                .quantidadeAcoes(acao.getQuantidadeAcoes())
                .data(acao.getData())
                .lucroPrejuizo(acao.getLucroPrejuizo())
                .build();
    }
}
