package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.RecomendacaoResolvidaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecomendacaoResolvidaClienteRepository extends JpaRepository<RecomendacaoResolvidaCliente, Long> {

    Optional<RecomendacaoResolvidaCliente> findByRecomendacaoIdAndClienteId(Long recomendacaoId, Long clienteId);

    boolean existsByRecomendacaoIdAndClienteId(Long recomendacaoId, Long clienteId);

    void deleteByRecomendacaoIdAndClienteId(Long recomendacaoId, Long clienteId);
    void deleteByRecomendacaoId(Long recomendacaoId);

    /** de e ate devem ser sempre não-nulos (use 1970 e 9999 no service se sem filtro). */
    @Query("SELECT rrc FROM RecomendacaoResolvidaCliente rrc WHERE rrc.recomendacao.carteira.consultor.id = :consultorId " +
           "AND rrc.resolvidoEm >= :de AND rrc.resolvidoEm <= :ate " +
           "ORDER BY rrc.resolvidoEm DESC")
    List<RecomendacaoResolvidaCliente> findByConsultorId(@Param("consultorId") Long consultorId,
                                                          @Param("de") LocalDateTime de,
                                                          @Param("ate") LocalDateTime ate);
}
