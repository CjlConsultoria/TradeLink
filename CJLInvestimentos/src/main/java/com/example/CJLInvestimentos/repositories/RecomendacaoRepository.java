package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.Recomendacao;
import com.example.CJLInvestimentos.entities.enums.StatusRecomendacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecomendacaoRepository extends JpaRepository<Recomendacao, Long> {

    @Query("SELECT r FROM Recomendacao r LEFT JOIN FETCH r.carteira c LEFT JOIN FETCH c.empresa WHERE r.id = :id")
    Optional<Recomendacao> findByIdWithCarteiraAndEmpresa(@Param("id") Long id);

    @Query("SELECT r FROM Recomendacao r LEFT JOIN FETCH r.carteira c LEFT JOIN FETCH c.empresa LEFT JOIN FETCH c.consultor WHERE r.id = :id")
    Optional<Recomendacao> findByIdWithCarteiraEmpresaAndConsultor(@Param("id") Long id);

    List<Recomendacao> findByCarteiraId(Long carteiraId);
    List<Recomendacao> findByCarteiraIdAndStatus(Long carteiraId, StatusRecomendacao status);
    List<Recomendacao> findByCarteiraIdIn(List<Long> carteiraIds);
    long countByCarteiraId(Long carteiraId);
    long countByCarteiraIdAndStatus(Long carteiraId, StatusRecomendacao status);
}
