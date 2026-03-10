package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.Carteira;
import com.example.CJLInvestimentos.entities.Recomendacao;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.repositories.CarteiraClienteRepository;
import com.example.CJLInvestimentos.repositories.CarteiraRepository;
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
    private final CarteiraRepository carteiraRepository;
    private final UserRepository userRepository;

    @Async
    public void notificarClientesNovaRecomendacaoAsync(Long recomendacaoId) {
        try {
            Recomendacao rec = recomendacaoRepository.findByIdWithCarteiraAndEmpresa(recomendacaoId).orElse(null);
            if (rec == null || rec.getCarteira() == null || rec.getCarteira().getEmpresa() == null) return;
            var empresa = rec.getCarteira().getEmpresa();
            List<User> clientes = carteiraClienteRepository.findByCarteiraIdWithCliente(rec.getCarteira().getId()).stream()
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
            Recomendacao rec = recomendacaoRepository.findByIdWithCarteiraEmpresaAndConsultor(recomendacaoId).orElse(null);
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

    @Async
    public void notificarPortfolioCriticoAsync(Long carteiraId, Long clienteId, String nomeCliente,
                                                int totalDesvios, String maiorDesvio,
                                                java.util.List<String[]> ativos) {
        try {
            Carteira carteira = carteiraRepository.findById(carteiraId).orElse(null);
            if (carteira == null || carteira.getConsultor() == null || carteira.getEmpresa() == null) return;
            notificationService.notificarPortfolioCritico(
                    carteira.getEmpresa(), carteira.getConsultor(),
                    nomeCliente, carteira.getNome(),
                    totalDesvios, maiorDesvio, ativos);
        } catch (Exception e) {
            log.warn("Falha ao notificar portfolio critico em background: {}", e.getMessage());
        }
    }

    @Async
    public void notificarNovaMovimentacaoAsync(Long carteiraId, Long clienteId,
                                                String tipo, String valor, String data) {
        try {
            User cliente = userRepository.findById(clienteId).orElse(null);
            Carteira carteira = carteiraRepository.findById(carteiraId).orElse(null);
            if (cliente == null || carteira == null || carteira.getConsultor() == null || carteira.getEmpresa() == null) return;
            notificationService.notificarNovaMovimentacao(
                    carteira.getEmpresa(), carteira.getConsultor(),
                    cliente.getNome() != null ? cliente.getNome() : cliente.getEmail(),
                    carteira.getNome(), tipo, valor, data);
        } catch (Exception e) {
            log.warn("Falha ao notificar nova movimentacao em background: {}", e.getMessage());
        }
    }

    @Async
    public void notificarRecomendacoesGeradasAsync(Long carteiraId, int totalRecomendacoes, int totalClientes) {
        try {
            Carteira carteira = carteiraRepository.findById(carteiraId).orElse(null);
            if (carteira == null || carteira.getConsultor() == null || carteira.getEmpresa() == null) return;
            notificationService.notificarRecomendacoesGeradas(
                    carteira.getEmpresa(), carteira.getConsultor(),
                    totalRecomendacoes, totalClientes, carteira.getNome());
        } catch (Exception e) {
            log.warn("Falha ao notificar recomendacoes geradas em background: {}", e.getMessage());
        }
    }

    @Async
    public void enviarEmailExclusaoClienteAsync(String email, String nome) {
        try {
            notificationService.enviarEmailExclusaoCliente(email, nome);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail de exclusão de cliente em background: {}", e.getMessage());
        }
    }

    @Async
    public void enviarEmailVinculacaoClienteAsync(String email, String nome, String empresaNome) {
        try {
            notificationService.enviarEmailVinculacaoCliente(email, nome, empresaNome);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail de vinculação de cliente em background: {}", e.getMessage());
        }
    }

    @Async
    public void enviarBoasVindasAutoCadastroAsync(String email, String nome, String tipo, java.time.LocalDateTime trialFim) {
        try {
            notificationService.enviarBoasVindasAutoCadastro(email, nome, tipo, trialFim);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail de boas-vindas auto-cadastro em background: {}", e.getMessage());
        }
    }

    @Async
    public void enviarEmailConviteAsync(String email, String token, String empresaNome) {
        try {
            notificationService.enviarEmailConvite(email, token, empresaNome);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail de convite em background: {}", e.getMessage());
        }
    }

    @Async
    public void enviarEmailOtpAsync(String email, String nome, String code) {
        try {
            notificationService.enviarEmailOtp(email, nome, code);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail OTP em background: {}", e.getMessage());
        }
    }

    @Async
    public void notificarOperacaoRegistradaAsync(Long recomendacaoId, Long clienteId,
                                                  String tipoOp, String ativo,
                                                  String quantidade, String precoExecutado) {
        try {
            var rec = recomendacaoRepository.findByIdWithCarteiraAndEmpresa(recomendacaoId).orElse(null);
            User cliente = userRepository.findById(clienteId).orElse(null);
            if (rec == null || rec.getCarteira() == null || rec.getCarteira().getConsultor() == null
                    || rec.getCarteira().getEmpresa() == null || cliente == null) return;
            notificationService.notificarOperacaoRegistrada(
                    rec.getCarteira().getEmpresa(), rec.getCarteira().getConsultor(),
                    cliente.getNome() != null ? cliente.getNome() : cliente.getEmail(),
                    rec.getCarteira().getNome(), tipoOp, ativo, quantidade, precoExecutado);
        } catch (Exception e) {
            log.warn("Falha ao notificar operacao registrada em background: {}", e.getMessage());
        }
    }
}
