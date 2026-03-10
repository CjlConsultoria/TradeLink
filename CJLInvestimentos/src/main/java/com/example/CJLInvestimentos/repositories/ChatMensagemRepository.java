package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.ChatMensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMensagemRepository extends JpaRepository<ChatMensagem, Long> {

    List<ChatMensagem> findByConversaIdOrderByCreatedAtAsc(Long conversaId);

    long countByConversaIdAndLidaFalseAndRemetenteIdNot(Long conversaId, Long remetenteId);

    @Modifying
    @Transactional
    @Query("UPDATE ChatMensagem m SET m.lida = true WHERE m.conversa.id = :conversaId AND m.remetente.id != :userId")
    void marcarLidasPorConversa(@Param("conversaId") Long conversaId, @Param("userId") Long userId);
}
