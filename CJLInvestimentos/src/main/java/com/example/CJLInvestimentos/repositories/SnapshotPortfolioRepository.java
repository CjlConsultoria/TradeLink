package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.SnapshotPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SnapshotPortfolioRepository extends JpaRepository<SnapshotPortfolio, Long> {

    List<SnapshotPortfolio> findByClienteIdAndCarteiraIdOrderByDataSnapshotAsc(Long clienteId, Long carteiraId);

    List<SnapshotPortfolio> findByCarteiraIdOrderByDataSnapshotAsc(Long carteiraId);

    boolean existsByClienteIdAndCarteiraIdAndDataSnapshot(Long clienteId, Long carteiraId, LocalDate dataSnapshot);

    @Modifying
    @Query("DELETE FROM SnapshotPortfolio s WHERE s.carteira.id = :carteiraId")
    void deleteByCarteiraId(Long carteiraId);
}
