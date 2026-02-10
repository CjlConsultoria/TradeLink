package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.Recomendacao;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.repositories.CarteiraClienteRepository;
import com.example.CJLInvestimentos.repositories.RecomendacaoRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Executa envio de e-mails e notificações em background para não bloquear a resposta da API.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAsyncRunner {

    private final NotificationService notificationService;
    private final RecomendacaoRepository recomendacaoRepository;
    private final CarteiraClienteRepository carteiraClienteRepository;
    private final UserRepository userRepository;

    @Async
    public void notificarClientesNovaRecomendacaoAsync(Long recomendacaoId) {
        try {
            Recomendacao rec = recomendacaoRepository.findById(recomendacaoId).orElse(null);
            if (rec == null || rec.getCarteira() == null || rec.getCarteira().getEmpresa() == null) return;
            var empresa = rec.getCarteira().getEmpresa();
            List<User> clientes = carteiraClienteRepository.findByCarteiraId(rec.getCarteira().getId()).stream()
                    .map(cc -> cc.getCliente())
                    .filter(u -> u != null && Boolean.TRUE.equals(u.getAtivo()))
                    .collect(Collectors.toList());
            log.info("Nova recomendação criada (carteira id={}); notificando {} cliente(s) em background. Empresa notificacao_email={}",
                    rec.getCarteira().getId(), clientes.size(), Boolean.TRUE.equals(empresa.getNotificacaoEmail()));
            if (!clientes.isEmpty()) {
                notificationService.notificarNovaRecomendacaoParaClientes(empresa, clientes, rec);
            }
        } catch (Exception e) {
            log.warn("Falha ao notificar clientes da nova recomendação (async): {}", e.getMessage());
        }
    }

    @Async
    public void notificarConsultorClienteResolveuAsync(Long recomendacaoId, Long clienteId) {
        try {
            Recomendacao rec = recomendacaoRepository.findById(recomendacaoId).orElse(null);
            User cliente = userRepository.findById(clienteId).orElse(null);
            if (rec == null || rec.getCarteira() == null || rec.getCarteira().getConsultor() == null || rec.getCarteira().getEmpresa() == null || cliente == null) return;
            notificationService.notificarClienteResolveuParaConsultor(
                    rec.getCarteira().getEmpresa(),
                    rec.getCarteira().getConsultor(),
                    cliente,
                    rec);
        } catch (Exception e) {
            log.warn("Falha ao notificar consultor (cliente resolveu) em background: {}", e.getMessage());
        }
    }

    @Async
    public void enviarEmailNovoUsuarioAsync(String email, String nome, Role role) {
        try {
            notificationService.enviarEmailNovoUsuario(email, nome, role);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail de novo usuário em background: {}", e.getMessage());
        }
    }

    @Async
    public void enviarEmailSenhaAlteradaAsync(String email, String nome) {
        try {
            notificationService.enviarEmailSenhaAlterada(email, nome);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail de senha alterada em background: {}", e.getMessage());
        }
    }
}
