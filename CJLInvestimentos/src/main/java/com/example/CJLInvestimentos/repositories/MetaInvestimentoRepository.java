package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.MetaInvestimento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MetaInvestimentoRepository extends JpaRepository<MetaInvestimento, Long> {
    List<MetaInvestimento> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndConcluidaFalse(Long userId);
}
