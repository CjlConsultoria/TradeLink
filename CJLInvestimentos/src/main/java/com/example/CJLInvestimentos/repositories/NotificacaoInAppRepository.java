package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.NotificacaoInApp;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacaoInAppRepository extends JpaRepository<NotificacaoInApp, Long> {

    List<NotificacaoInApp> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndLidaFalse(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE NotificacaoInApp n SET n.lida = true WHERE n.user.id = :userId AND n.lida = false")
    int marcarTodasComoLidas(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM NotificacaoInApp n WHERE n.createdAt < :antes")
    int deletarAntigas(LocalDateTime antes);
}
