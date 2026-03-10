package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.MovimentacaoCarteira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MovimentacaoCarteiraRepository extends JpaRepository<MovimentacaoCarteira, Long> {

    List<MovimentacaoCarteira> findByClienteIdAndCarteiraIdOrderByDataMovimentacaoDescCreatedAtDesc(Long clienteId, Long carteiraId);

    List<MovimentacaoCarteira> findByCarteiraIdOrderByDataMovimentacaoDescCreatedAtDesc(Long carteiraId);

    List<MovimentacaoCarteira> findByClienteIdOrderByDataMovimentacaoDescCreatedAtDesc(Long clienteId);

    @Query("SELECT COALESCE(SUM(CASE WHEN m.tipo = 'APORTE' THEN m.valor ELSE 0 END), 0) FROM MovimentacaoCarteira m WHERE m.cliente.id = :clienteId AND m.carteira.id = :carteiraId")
    BigDecimal somarAportes(@Param("clienteId") Long clienteId, @Param("carteiraId") Long carteiraId);

    @Query("SELECT COALESCE(SUM(CASE WHEN m.tipo = 'SAQUE' THEN m.valor ELSE 0 END), 0) FROM MovimentacaoCarteira m WHERE m.cliente.id = :clienteId AND m.carteira.id = :carteiraId")
    BigDecimal somarSaques(@Param("clienteId") Long clienteId, @Param("carteiraId") Long carteiraId);

    void deleteByCarteiraId(Long carteiraId);
}
