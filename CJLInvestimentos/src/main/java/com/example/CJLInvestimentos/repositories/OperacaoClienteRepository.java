package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.OperacaoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OperacaoClienteRepository extends JpaRepository<OperacaoCliente, Long> {
    List<OperacaoCliente> findByRecomendacaoIdOrderByDataExecucaoDesc(Long recomendacaoId);
    List<OperacaoCliente> findByClienteIdOrderByDataExecucaoDesc(Long clienteId);

    @Query("SELECT op FROM OperacaoCliente op WHERE op.cliente.id = :clienteId " +
           "AND op.dataExecucao >= :de AND op.dataExecucao <= :ate ORDER BY op.dataExecucao DESC")
    List<OperacaoCliente> findByClienteIdAndDataExecucaoBetween(@Param("clienteId") Long clienteId,
                                                                 @Param("de") LocalDateTime de,
                                                                 @Param("ate") LocalDateTime ate);
    long countByRecomendacaoId(Long recomendacaoId);
    void deleteByRecomendacaoId(Long recomendacaoId);

    @Query("SELECT op FROM OperacaoCliente op WHERE op.recomendacao.carteira.consultor.id = :consultorId")
    List<OperacaoCliente> findByConsultorId(@Param("consultorId") Long consultorId);

    @Query("SELECT op FROM OperacaoCliente op " +
           "JOIN op.recomendacao r JOIN r.carteira c " +
           "WHERE c.consultor.id = :consultorId " +
           "AND (:carteiraId IS NULL OR c.id = :carteiraId) " +
           "AND (:clienteId IS NULL OR op.cliente.id = :clienteId) " +
           "AND (:de IS NULL OR op.dataExecucao >= :de) AND (:ate IS NULL OR op.dataExecucao <= :ate) " +
           "ORDER BY op.dataExecucao DESC")
    List<OperacaoCliente> findByConsultorFiltros(@Param("consultorId") Long consultorId,
                                                  @Param("carteiraId") Long carteiraId,
                                                  @Param("clienteId") Long clienteId,
                                                  @Param("de") LocalDateTime de,
                                                  @Param("ate") LocalDateTime ate);

    /** Filtros: clienteId pode ser null; de/ate são sempre não-nulos (use valor mínimo/máximo no service se sem filtro). */
    @Query("SELECT op FROM OperacaoCliente op JOIN op.recomendacao r WHERE r.carteira.id IN :carteiraIds " +
           "AND (:clienteId IS NULL OR op.cliente.id = :clienteId) " +
           "AND op.dataExecucao >= :de AND op.dataExecucao <= :ate " +
           "ORDER BY op.dataExecucao DESC")
    List<OperacaoCliente> findByCarteiraIdsAndFiltros(@Param("carteiraIds") List<Long> carteiraIds,
                                                      @Param("clienteId") Long clienteId,
                                                      @Param("de") LocalDateTime de,
                                                      @Param("ate") LocalDateTime ate);
}
