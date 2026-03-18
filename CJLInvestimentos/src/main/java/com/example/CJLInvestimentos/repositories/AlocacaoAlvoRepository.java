package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.AlocacaoAlvo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlocacaoAlvoRepository extends JpaRepository<AlocacaoAlvo, Long> {
    List<AlocacaoAlvo> findByCarteiraIdOrderByPercentualAlvoDesc(Long carteiraId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM AlocacaoAlvo a WHERE a.carteira.id = :carteiraId")
    void deleteByCarteiraId(Long carteiraId);

    boolean existsByCarteiraId(Long carteiraId);
}
