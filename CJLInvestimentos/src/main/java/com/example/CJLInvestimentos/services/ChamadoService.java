package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.AtualizarStatusChamadoRequest;
import com.example.CJLInvestimentos.dtos.request.CriarChamadoRequest;
import com.example.CJLInvestimentos.dtos.request.ResponderChamadoRequest;
import com.example.CJLInvestimentos.dtos.response.ChamadoRespostaResponse;
import com.example.CJLInvestimentos.dtos.response.ChamadoResponse;
import com.example.CJLInvestimentos.entities.Chamado;
import com.example.CJLInvestimentos.entities.ChamadoResposta;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.entities.enums.StatusChamado;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.repositories.ChamadoRepository;
import com.example.CJLInvestimentos.repositories.ChamadoRespostaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final ChamadoRespostaRepository respostaRepository;
    private final NotificationService notificationService;
    private final EmailTemplateService emailTemplateService;

    private String gerarNumero() {
        long max = chamadoRepository.findMaxNumero();
        return String.format("TL-%04d", max + 1);
    }

    @Transactional
    public ChamadoResponse criarChamado(User user, CriarChamadoRequest request) {
        String numero = gerarNumero();

        Chamado chamado = Chamado.builder()
                .numero(numero)
                .assunto(request.getAssunto())
                .descricao(request.getDescricao())
                .categoria(request.getCategoria())
                .prioridade(request.getPrioridade())
                .user(user)
                .build();

        chamado = chamadoRepository.save(chamado);
        log.info("Chamado {} criado por {} ({})", numero, user.getNome(), user.getEmail());

        // Email de confirmacao
        try {
            String categoriaDisplay = request.getCategoria().name().replace("_", " ");
            String prioridadeDisplay = request.getPrioridade().name().replace("_", " ");
            String html = emailTemplateService.buildChamadoCriado(
                    user.getNome(), numero, request.getAssunto(), categoriaDisplay, prioridadeDisplay);
            notificationService.enviarEmailHtml(user.getEmail(), "Chamado " + numero + " criado - TradeLink", html);
        } catch (Exception e) {
            log.warn("Erro ao enviar email de chamado criado: {}", e.getMessage());
        }

        return toChamadoResponse(chamado, user.getId());
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponse> listarChamadosUsuario(Long userId) {
        return chamadoRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(c -> toChamadoResponse(c, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponse> listarTodosChamados(Long adminUserId) {
        return chamadoRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(c -> toChamadoResponse(c, adminUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponse> listarChamadosPorStatus(StatusChamado status, Long adminUserId) {
        return chamadoRepository.findByStatusOrderByUpdatedAtDesc(status).stream()
                .map(c -> toChamadoResponse(c, adminUserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public ChamadoResponse buscarChamado(Long id, User user) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Chamado não encontrado."));

        // Validar permissao: dono ou AdminMax
        if (!chamado.getUser().getId().equals(user.getId()) && user.getRole() != Role.AdminMax) {
            throw new BusinessException("Sem permissão para ver este chamado.");
        }

        // Marcar respostas como lidas
        respostaRepository.marcarLidasPorChamado(id, user.getId());

        return toChamadoResponse(chamado, user.getId());
    }

    @Transactional
    public ChamadoRespostaResponse responderChamado(Long chamadoId, User remetente, ResponderChamadoRequest request) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new BusinessException("Chamado não encontrado."));

        // Validar: dono ou AdminMax
        if (!chamado.getUser().getId().equals(remetente.getId()) && remetente.getRole() != Role.AdminMax) {
            throw new BusinessException("Sem permissão para responder este chamado.");
        }

        ChamadoResposta resposta = ChamadoResposta.builder()
                .chamado(chamado)
                .remetente(remetente)
                .conteudo(request.getConteudo())
                .build();
        resposta = respostaRepository.save(resposta);

        // Atualizar timestamp do chamado
        chamado.setUpdatedAt(LocalDateTime.now());
        chamadoRepository.save(chamado);

        // Se admin respondeu, notificar o dono do chamado
        if (remetente.getRole() == Role.AdminMax && !chamado.getUser().getId().equals(remetente.getId())) {
            try {
                User dono = chamado.getUser();
                String html = emailTemplateService.buildChamadoNovaResposta(
                        dono.getNome(), chamado.getNumero(), chamado.getAssunto(),
                        remetente.getNome(), request.getConteudo());
                notificationService.enviarEmailHtml(dono.getEmail(),
                        "Nova resposta no chamado " + chamado.getNumero() + " - TradeLink", html);
            } catch (Exception e) {
                log.warn("Erro ao enviar email de nova resposta: {}", e.getMessage());
            }
        }

        return toRespostaResponse(resposta);
    }

    @Transactional
    public List<ChamadoRespostaResponse> listarRespostas(Long chamadoId, User user) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new BusinessException("Chamado não encontrado."));

        if (!chamado.getUser().getId().equals(user.getId()) && user.getRole() != Role.AdminMax) {
            throw new BusinessException("Sem permissão para ver respostas deste chamado.");
        }

        // Marcar como lidas
        respostaRepository.marcarLidasPorChamado(chamadoId, user.getId());

        return respostaRepository.findByChamadoIdOrderByCreatedAtAsc(chamadoId).stream()
                .map(this::toRespostaResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChamadoResponse atualizarStatus(Long chamadoId, AtualizarStatusChamadoRequest request) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new BusinessException("Chamado não encontrado."));

        StatusChamado novoStatus = request.getStatus();
        chamado.setStatus(novoStatus);
        chamado.setUpdatedAt(LocalDateTime.now());
        chamado = chamadoRepository.save(chamado);

        log.info("Chamado {} status alterado para {}", chamado.getNumero(), novoStatus);

        // Notificar dono por email
        try {
            User dono = chamado.getUser();
            String html = emailTemplateService.buildChamadoStatusAlterado(
                    dono.getNome(), chamado.getNumero(), chamado.getAssunto(), novoStatus.name());
            notificationService.enviarEmailHtml(dono.getEmail(),
                    "Chamado " + chamado.getNumero() + " atualizado - TradeLink", html);
        } catch (Exception e) {
            log.warn("Erro ao enviar email de status alterado: {}", e.getMessage());
        }

        return toChamadoResponse(chamado, null);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> contarPorStatus() {
        Map<String, Long> stats = new HashMap<>();
        for (StatusChamado status : StatusChamado.values()) {
            stats.put(status.name(), chamadoRepository.countByStatus(status));
        }
        return stats;
    }

    private ChamadoResponse toChamadoResponse(Chamado chamado, Long viewerUserId) {
        List<ChamadoResposta> respostas = respostaRepository.findByChamadoIdOrderByCreatedAtAsc(chamado.getId());
        String ultimaResposta = respostas.isEmpty() ? null :
                respostas.get(respostas.size() - 1).getConteudo();
        if (ultimaResposta != null && ultimaResposta.length() > 100) {
            ultimaResposta = ultimaResposta.substring(0, 100) + "...";
        }

        long naoLidas = viewerUserId != null ?
                respostaRepository.countByChamadoIdAndLidaFalseAndRemetenteIdNot(chamado.getId(), viewerUserId) : 0;

        return ChamadoResponse.builder()
                .id(chamado.getId())
                .numero(chamado.getNumero())
                .assunto(chamado.getAssunto())
                .descricao(chamado.getDescricao())
                .categoria(chamado.getCategoria().name())
                .prioridade(chamado.getPrioridade().name())
                .status(chamado.getStatus().name())
                .userId(chamado.getUser().getId())
                .nomeUsuario(chamado.getUser().getNome())
                .roleUsuario(chamado.getUser().getRole().name())
                .createdAt(chamado.getCreatedAt())
                .updatedAt(chamado.getUpdatedAt())
                .ultimaResposta(ultimaResposta)
                .totalRespostas(respostas.size())
                .naoLidas((int) naoLidas)
                .build();
    }

    private ChamadoRespostaResponse toRespostaResponse(ChamadoResposta resposta) {
        return ChamadoRespostaResponse.builder()
                .id(resposta.getId())
                .chamadoId(resposta.getChamado().getId())
                .remetenteId(resposta.getRemetente().getId())
                .nomeRemetente(resposta.getRemetente().getNome())
                .conteudo(resposta.getConteudo())
                .lida(resposta.getLida())
                .createdAt(resposta.getCreatedAt())
                .isAdmin(resposta.getRemetente().getRole() == Role.AdminMax)
                .build();
    }
}
