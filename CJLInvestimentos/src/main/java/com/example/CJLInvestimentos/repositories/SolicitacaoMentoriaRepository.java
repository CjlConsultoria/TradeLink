package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.SolicitacaoMentoria;
import com.example.CJLInvestimentos.entities.enums.StatusSolicitacaoMentoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoMentoriaRepository extends JpaRepository<SolicitacaoMentoria, Long> {

    List<SolicitacaoMentoria> findByClienteIdOrderByCreatedAtDesc(Long clienteId);

    List<SolicitacaoMentoria> findByEmpresaIdOrderByCreatedAtDesc(Long empresaId);

    List<SolicitacaoMentoria> findByStatus(StatusSolicitacaoMentoria status);

    List<SolicitacaoMentoria> findByClienteIdAndEmpresaIdAndStatusIn(
            Long clienteId, Long empresaId, List<StatusSolicitacaoMentoria> statuses);

    long countByStatus(StatusSolicitacaoMentoria status);

    List<SolicitacaoMentoria> findAllByOrderByCreatedAtDesc();

    java.util.Optional<SolicitacaoMentoria> findByStripeCheckoutSessionId(String sessionId);

    java.util.Optional<SolicitacaoMentoria> findByStripeSubscriptionId(String subscriptionId);
}
