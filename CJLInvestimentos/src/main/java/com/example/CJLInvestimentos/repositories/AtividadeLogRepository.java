package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.AtividadeLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AtividadeLogRepository extends JpaRepository<AtividadeLog, Long> {
    List<AtividadeLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM AtividadeLog a WHERE a.createdAt < CURRENT_TIMESTAMP - 90 DAY")
    void deletarAntigas();
}
