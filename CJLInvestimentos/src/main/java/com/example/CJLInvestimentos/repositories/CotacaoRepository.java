package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.Cotacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CotacaoRepository extends JpaRepository<Cotacao, Long>, JpaSpecificationExecutor<Cotacao> {
    Optional<Cotacao> findTopByMoedaAndParMoedaOrderByDataHoraDesc(String moeda, String parMoeda);

    @Query("SELECT c FROM Cotacao c WHERE c.dataHora = " +
           "(SELECT MAX(c2.dataHora) FROM Cotacao c2 WHERE c2.moeda = c.moeda AND c2.parMoeda = c.parMoeda)")
    List<Cotacao> findLatestQuotes();

    List<Cotacao> findByMoedaAndParMoedaAndDataHoraAfterOrderByDataHoraAsc(
        String moeda, String parMoeda, LocalDateTime after);

    @Modifying
    @Query("DELETE FROM Cotacao c WHERE c.fonte = :fonte")
    void deleteByFonte(String fonte);
}
