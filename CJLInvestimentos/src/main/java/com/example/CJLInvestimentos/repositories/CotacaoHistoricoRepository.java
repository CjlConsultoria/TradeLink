package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.CotacaoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CotacaoHistoricoRepository extends JpaRepository<CotacaoHistorico, Long> {

    List<CotacaoHistorico> findByMoedaAndParMoedaAndIntervaloAndDataHoraBetweenOrderByDataHoraAsc(
            String moeda, String parMoeda, String intervalo, LocalDateTime de, LocalDateTime ate);

    List<CotacaoHistorico> findByMoedaAndParMoedaAndIntervaloOrderByDataHoraAsc(
            String moeda, String parMoeda, String intervalo);

    Optional<CotacaoHistorico> findTopByMoedaAndParMoedaAndIntervaloOrderByDataHoraDesc(
            String moeda, String parMoeda, String intervalo);

    @Modifying
    @Query("DELETE FROM CotacaoHistorico c WHERE c.moeda = :moeda AND c.parMoeda = :parMoeda AND c.intervalo = :intervalo")
    void deleteByMoedaAndParMoedaAndIntervalo(String moeda, String parMoeda, String intervalo);

    long countByMoedaAndParMoedaAndIntervalo(String moeda, String parMoeda, String intervalo);

    // Queries sem filtro de intervalo — retornam qualquer granularidade disponível
    List<CotacaoHistorico> findByMoedaAndParMoedaAndDataHoraBetweenOrderByDataHoraAsc(
            String moeda, String parMoeda, LocalDateTime de, LocalDateTime ate);

    List<CotacaoHistorico> findByMoedaAndParMoedaOrderByDataHoraAsc(
            String moeda, String parMoeda);
}
