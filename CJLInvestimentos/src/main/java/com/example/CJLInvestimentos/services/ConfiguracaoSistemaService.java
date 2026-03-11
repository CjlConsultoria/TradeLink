package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.ConfiguracaoSistema;
import com.example.CJLInvestimentos.repositories.ConfiguracaoSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfiguracaoSistemaService {

    private final ConfiguracaoSistemaRepository repository;

    /** Retorna a configuracao (cria se nao existir). */
    public ConfiguracaoSistema get() {
        return repository.findAll().stream().findFirst()
                .orElseGet(() -> repository.save(ConfiguracaoSistema.builder().doisFatoresAtivo(true).build()));
    }

    /** Verifica se 2FA esta ativo globalmente. */
    public boolean isDoisFatoresAtivo() {
        return Boolean.TRUE.equals(get().getDoisFatoresAtivo());
    }

    /** Liga/desliga 2FA global. */
    public ConfiguracaoSistema setDoisFatoresAtivo(boolean ativo) {
        ConfiguracaoSistema config = get();
        config.setDoisFatoresAtivo(ativo);
        return repository.save(config);
    }
}
