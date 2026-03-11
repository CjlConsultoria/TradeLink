package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.AlertaPreco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaPrecoRepository extends JpaRepository<AlertaPreco, Long> {
    List<AlertaPreco> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AlertaPreco> findByAtivoTrueAndDisparadoFalse();

    long countByUserIdAndAtivoTrue(Long userId);
}
