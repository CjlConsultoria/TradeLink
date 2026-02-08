package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.CarteiraCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarteiraClienteRepository extends JpaRepository<CarteiraCliente, Long> {
    List<CarteiraCliente> findByClienteId(Long clienteId);
    List<CarteiraCliente> findByCarteiraId(Long carteiraId);
    Optional<CarteiraCliente> findByCarteiraIdAndClienteId(Long carteiraId, Long clienteId);
    boolean existsByCarteiraIdAndClienteId(Long carteiraId, Long clienteId);
    long countByCarteiraId(Long carteiraId);
}
