package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.Recomendacao;
import com.example.CJLInvestimentos.entities.enums.StatusRecomendacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecomendacaoRepository extends JpaRepository<Recomendacao, Long> {
    List<Recomendacao> findByCarteiraId(Long carteiraId);
    List<Recomendacao> findByCarteiraIdAndStatus(Long carteiraId, StatusRecomendacao status);
    List<Recomendacao> findByCarteiraIdIn(List<Long> carteiraIds);
    long countByCarteiraId(Long carteiraId);
    long countByCarteiraIdAndStatus(Long carteiraId, StatusRecomendacao status);
}
