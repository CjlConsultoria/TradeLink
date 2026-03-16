package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.Chamado;
import com.example.CJLInvestimentos.entities.enums.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    List<Chamado> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<Chamado> findAllByOrderByUpdatedAtDesc();

    List<Chamado> findByStatusOrderByUpdatedAtDesc(StatusChamado status);

    Optional<Chamado> findByNumero(String numero);

    long countByStatus(StatusChamado status);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(c.numero, 4) AS integer)), 0) FROM Chamado c")
    long findMaxNumero();
}
