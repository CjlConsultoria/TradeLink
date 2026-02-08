package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.Fatura;
import com.example.CJLInvestimentos.entities.enums.StatusFatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaturaRepository extends JpaRepository<Fatura, Long> {
    List<Fatura> findByEmpresaIdOrderByDataVencimentoDesc(Long empresaId);

    List<Fatura> findByEmpresaIdAndStatusOrderByDataVencimentoDesc(Long empresaId, StatusFatura status);

    boolean existsByEmpresaIdAndReferenciaExterna(Long empresaId, String referenciaExterna);
}
