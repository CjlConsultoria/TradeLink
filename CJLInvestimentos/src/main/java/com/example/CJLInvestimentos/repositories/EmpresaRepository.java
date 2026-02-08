package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByCnpj(String cnpj);
    Optional<Empresa> findByStripeCustomerId(String stripeCustomerId);
    Optional<Empresa> findByStripeSubscriptionId(String stripeSubscriptionId);
    List<Empresa> findByAtivoTrue();
}
