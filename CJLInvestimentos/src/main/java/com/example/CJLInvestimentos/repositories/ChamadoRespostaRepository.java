package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.ChamadoResposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChamadoRespostaRepository extends JpaRepository<ChamadoResposta, Long> {

    List<ChamadoResposta> findByChamadoIdOrderByCreatedAtAsc(Long chamadoId);

    long countByChamadoIdAndLidaFalseAndRemetenteIdNot(Long chamadoId, Long remetenteId);

    @Modifying
    @Transactional
    @Query("UPDATE ChamadoResposta r SET r.lida = true WHERE r.chamado.id = :chamadoId AND r.remetente.id != :userId")
    void marcarLidasPorChamado(@Param("chamadoId") Long chamadoId, @Param("userId") Long userId);
}
