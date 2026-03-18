package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.CarteiraCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CarteiraClienteRepository extends JpaRepository<CarteiraCliente, Long> {
    List<CarteiraCliente> findByClienteId(Long clienteId);
    List<CarteiraCliente> findByCarteiraId(Long carteiraId);

    @Query("SELECT cc FROM CarteiraCliente cc JOIN FETCH cc.cliente WHERE cc.carteira.id = :carteiraId")
    List<CarteiraCliente> findByCarteiraIdWithCliente(@Param("carteiraId") Long carteiraId);
    Optional<CarteiraCliente> findByCarteiraIdAndClienteId(Long carteiraId, Long clienteId);
    boolean existsByCarteiraIdAndClienteId(Long carteiraId, Long clienteId);
    long countByCarteiraId(Long carteiraId);

    @Query("SELECT cc FROM CarteiraCliente cc JOIN FETCH cc.carteira c JOIN FETCH c.consultor WHERE cc.cliente.id = :clienteId")
    List<CarteiraCliente> findByClienteIdWithCarteiraConsultor(@Param("clienteId") Long clienteId);
}
