package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.Fatura;
import com.example.CJLInvestimentos.entities.enums.StatusFatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FaturaRepository extends JpaRepository<Fatura, Long> {
    List<Fatura> findByEmpresaIdOrderByDataVencimentoDesc(Long empresaId);

    List<Fatura> findByEmpresaIdAndStatusOrderByDataVencimentoDesc(Long empresaId, StatusFatura status);

    boolean existsByEmpresaIdAndReferenciaExterna(Long empresaId, String referenciaExterna);

    // Faturas de auto-gestão (por usuário individual)
    List<Fatura> findByUserIdOrderByDataVencimentoDesc(Long userId);

    boolean existsByUserIdAndReferenciaExterna(Long userId, String referenciaExterna);

    /** Lista global de faturas de empresas (exclui auto-gestão) com JOIN FETCH para evitar N+1. */
    @Query("SELECT f FROM Fatura f JOIN FETCH f.empresa WHERE f.empresa IS NOT NULL ORDER BY f.dataVencimento DESC")
    List<Fatura> findAllWithEmpresa();
}
