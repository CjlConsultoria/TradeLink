package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.ConfiguracaoPlataforma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoPlataformaRepository extends JpaRepository<ConfiguracaoPlataforma, Long> {
    Optional<ConfiguracaoPlataforma> findByChave(String chave);
}
