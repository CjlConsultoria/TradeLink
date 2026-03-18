package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.response.ClienteExcluidoStatusResponse;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Serviço central para gerenciar o estado pós-exclusão do cliente.
 * Controla a lógica de auto-gestão, download de relatório e verificação de subscription.
 */
@Service
@RequiredArgsConstructor
public class AutoGestaoService {

    private static final BigDecimal PRECO_RELATORIO = new BigDecimal("19.90");
    private static final int DIAS_TOLERANCIA = 5;

    private final UserRepository userRepository;
    private final RelatorioCompletoService relatorioCompletoService;
    private final PlanoService planoService;

    /** Verifica se o cliente está no estado "excluído" (sem empresa, mas ativo). */
    public boolean isClienteExcluido(User user) {
        return user.getRole() == Role.Cliente
                && user.getEmpresa() == null
                && Boolean.TRUE.equals(user.getAtivo());
    }

    /** Verifica se o cliente tem subscription de auto-gestão ativa. */
    public boolean hasActiveSubscription(User user) {
        if (!Boolean.TRUE.equals(user.getAutoGestao())) return false;
        if (user.getCurrentPeriodEnd() == null) return false;
        Instant limite = user.getCurrentPeriodEnd().plusSeconds(DIAS_TOLERANCIA * 86400L);
        return Instant.now().isBefore(limite) || Instant.now().equals(limite);
    }

    /** Retorna o status completo para a tela pós-exclusão. */
    @Transactional(readOnly = true)
    public ClienteExcluidoStatusResponse getStatus(User user) {
        return ClienteExcluidoStatusResponse.builder()
                .excluido(isClienteExcluido(user))
                .autoGestaoAtiva(hasActiveSubscription(user))
                .relatorioGratisBaixado(Boolean.TRUE.equals(user.getRelatorioComplBaixado()))
                .dataExclusao(user.getDataExclusao())
                .nomeCliente(user.getNome())
                .cpf(user.getCpf())
                .precoAutoGestao(planoService.getPrecoAutoGestao())
                .precoRelatorio(PRECO_RELATORIO)
                .build();
    }

    /** Gera e baixa o relatório gratuito (apenas 1x). */
    @Transactional
    public byte[] gerarRelatorioGratuito(User user) {
        if (Boolean.TRUE.equals(user.getRelatorioComplBaixado())) {
            throw new BusinessException("Relatório gratuito já foi baixado. Pague R$ 19,90 para baixar novamente.");
        }
        byte[] pdf = relatorioCompletoService.gerarRelatorioCompleto(user.getId());
        user.setRelatorioComplBaixado(true);
        user.setRelatorioComplBaixadoEm(LocalDateTime.now());
        userRepository.save(user);
        return pdf;
    }

    /** Gera relatório após pagamento (re-download). */
    @Transactional(readOnly = true)
    public byte[] gerarRelatorioPago(User user) {
        return relatorioCompletoService.gerarRelatorioCompleto(user.getId());
    }

    public BigDecimal getPrecoAutoGestao() { return planoService.getPrecoAutoGestao(); }
    public BigDecimal getPrecoRelatorio() { return PRECO_RELATORIO; }
}
