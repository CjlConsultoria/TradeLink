package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.Carteira;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarteiraRepository extends JpaRepository<Carteira, Long> {
    List<Carteira> findByEmpresaId(Long empresaId);
    List<Carteira> findByConsultorId(Long consultorId);
    List<Carteira> findByEmpresaIdAndAtivaTrue(Long empresaId);
    long countByConsultorId(Long consultorId);
}
