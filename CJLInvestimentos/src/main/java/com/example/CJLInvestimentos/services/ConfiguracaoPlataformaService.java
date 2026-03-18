package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.ConfiguracaoPlataforma;
import com.example.CJLInvestimentos.repositories.ConfiguracaoPlataformaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ConfiguracaoPlataformaService {

    private final ConfiguracaoPlataformaRepository repository;

    public static final String CHAVE_TAXA_MARKETPLACE = "MARKETPLACE_TAXA_PERCENTUAL";
    private static final BigDecimal TAXA_DEFAULT = new BigDecimal("15");

    /**
     * Retorna a taxa do marketplace em percentual (ex: 15 = 15%).
     */
    public BigDecimal getTaxaMarketplace() {
        return repository.findByChave(CHAVE_TAXA_MARKETPLACE)
                .map(c -> new BigDecimal(c.getValor()))
                .orElse(TAXA_DEFAULT);
    }

    /**
     * Atualiza a taxa do marketplace. Cria o registro se não existir.
     */
    public BigDecimal setTaxaMarketplace(BigDecimal novaTaxa) {
        ConfiguracaoPlataforma config = repository.findByChave(CHAVE_TAXA_MARKETPLACE)
                .orElseGet(() -> ConfiguracaoPlataforma.builder()
                        .chave(CHAVE_TAXA_MARKETPLACE)
                        .valor(TAXA_DEFAULT.toPlainString())
                        .build());
        config.setValor(novaTaxa.toPlainString());
        repository.save(config);
        return novaTaxa;
    }

    /**
     * Retorna o valor de qualquer configuração por chave.
     */
    public String getValor(String chave, String defaultValue) {
        return repository.findByChave(chave)
                .map(ConfiguracaoPlataforma::getValor)
                .orElse(defaultValue);
    }
}
