package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.AtivoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AtivoClienteRepository extends JpaRepository<AtivoCliente, Long> {

    List<AtivoCliente> findByClienteIdOrderBySimboloAsc(Long clienteId);

    @Query("SELECT a FROM AtivoCliente a WHERE a.cliente.id = :clienteId AND UPPER(a.simbolo) = UPPER(:simbolo)")
    Optional<AtivoCliente> findByClienteAndSimbolo(@Param("clienteId") Long clienteId, @Param("simbolo") String simbolo);

    boolean existsByClienteIdAndSimboloIgnoreCase(Long clienteId, String simbolo);

    List<AtivoCliente> findByClienteIdIn(List<Long> clienteIds);
}
