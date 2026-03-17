package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByCnpj(String cnpj);
    Optional<Empresa> findByStripeCustomerId(String stripeCustomerId);
    Optional<Empresa> findByStripeSubscriptionId(String stripeSubscriptionId);
    List<Empresa> findByAtivoTrue();

    /** Consultores visíveis no marketplace (ativos). */
    List<Empresa> findByMarketplaceVisivelTrueAndAtivoTrueOrderByNomeAsc();

    /** Busca no marketplace por nome ou especialização. */
    @Query("SELECT e FROM Empresa e WHERE e.marketplaceVisivel = true AND e.ativo = true " +
           "AND (LOWER(e.nome) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR LOWER(e.marketplaceEspecializacao) LIKE LOWER(CONCAT('%', :termo, '%')))")
    List<Empresa> buscarNoMarketplace(@Param("termo") String termo);
}
