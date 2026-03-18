package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.response.ChatConversaResponse;
import com.example.CJLInvestimentos.dtos.response.ChatMensagemResponse;
import com.example.CJLInvestimentos.entities.ChatConversa;
import com.example.CJLInvestimentos.entities.ChatMensagem;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.repositories.ChatConversaRepository;
import com.example.CJLInvestimentos.repositories.ChatMensagemRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatConversaRepository conversaRepository;
    private final ChatMensagemRepository mensagemRepository;
    private final UserRepository userRepository;

    /** Inicia ou retorna conversa aberta existente do usuário. */
    @Transactional
    public ChatConversaResponse iniciarConversa(User user, String assunto) {
        // Se já tem conversa aberta, retorna ela
        var existente = conversaRepository.findByUserIdAndStatus(user.getId(), "ABERTA");
        if (existente.isPresent()) {
            return toConversaResponse(existente.get(), user.getId());
        }

        ChatConversa conversa = ChatConversa.builder()
                .user(user)
                .assunto(assunto != null ? assunto : "Suporte")
                .build();
        conversa = conversaRepository.save(conversa);
        return toConversaResponse(conversa, user.getId());
    }

    /** Envia uma mensagem em uma conversa. */
    @Transactional
    public ChatMensagemResponse enviarMensagem(Long conversaId, User remetente, String conteudo) {
        ChatConversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new BusinessException("Conversa não encontrada."));

        // Validar: somente o dono da conversa ou AdminMax podem enviar
        if (!conversa.getUser().getId().equals(remetente.getId()) && remetente.getRole() != Role.AdminMax) {
            throw new BusinessException("Sem permissão para enviar mensagem nesta conversa.");
        }

        // Reabrir se fechada
        if ("FECHADA".equals(conversa.getStatus())) {
            conversa.setStatus("ABERTA");
        }

        ChatMensagem mensagem = ChatMensagem.builder()
                .conversa(conversa)
                .remetente(remetente)
                .conteudo(conteudo)
                .build();
        mensagem = mensagemRepository.save(mensagem);

        // Atualizar timestamp da conversa
        conversa.setUpdatedAt(java.time.LocalDateTime.now());
        conversaRepository.save(conversa);

        return toMensagemResponse(mensagem);
    }

    /** Lista conversas do usuário. */
    @Transactional(readOnly = true)
    public List<ChatConversaResponse> listarConversasUsuario(Long userId) {
        return conversaRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(c -> toConversaResponse(c, userId))
                .collect(Collectors.toList());
    }

    /** Lista todas conversas (AdminMax). */
    @Transactional(readOnly = true)
    public List<ChatConversaResponse> listarTodasConversas(Long adminUserId) {
        return conversaRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(c -> toConversaResponse(c, adminUserId))
                .collect(Collectors.toList());
    }

    /** Lista mensagens de uma conversa + marca como lidas. */
    @Transactional
    public List<ChatMensagemResponse> listarMensagens(Long conversaId, User user) {
        ChatConversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new BusinessException("Conversa não encontrada."));

        // Validar: somente o dono ou AdminMax podem ver
        if (!conversa.getUser().getId().equals(user.getId()) && user.getRole() != Role.AdminMax) {
            throw new BusinessException("Sem permissão para ver mensagens desta conversa.");
        }

        // Marcar como lidas as mensagens do outro
        mensagemRepository.marcarLidasPorConversa(conversaId, user.getId());

        return mensagemRepository.findByConversaIdOrderByCreatedAtAsc(conversaId).stream()
                .map(this::toMensagemResponse)
                .collect(Collectors.toList());
    }

    /** Fecha uma conversa (AdminMax). */
    @Transactional
    public void fecharConversa(Long conversaId) {
        ChatConversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new BusinessException("Conversa não encontrada."));
        conversa.setStatus("FECHADA");
        conversaRepository.save(conversa);
    }

    /** Conta mensagens não lidas para um usuário. */
    @Transactional(readOnly = true)
    public long contarNaoLidas(Long userId) {
        List<ChatConversa> conversas = conversaRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return conversas.stream()
                .mapToLong(c -> mensagemRepository.countByConversaIdAndLidaFalseAndRemetenteIdNot(c.getId(), userId))
                .sum();
    }

    /** Conta mensagens não lidas para AdminMax (todas conversas). */
    @Transactional(readOnly = true)
    public long contarNaoLidasAdmin(Long adminUserId) {
        return conversaRepository.findAllByOrderByUpdatedAtDesc().stream()
                .mapToLong(c -> mensagemRepository.countByConversaIdAndLidaFalseAndRemetenteIdNot(c.getId(), adminUserId))
                .sum();
    }

    private ChatConversaResponse toConversaResponse(ChatConversa conversa, Long viewerUserId) {
        List<ChatMensagem> mensagens = mensagemRepository.findByConversaIdOrderByCreatedAtAsc(conversa.getId());
        String ultimaMensagem = mensagens.isEmpty() ? null :
                mensagens.get(mensagens.size() - 1).getConteudo();
        if (ultimaMensagem != null && ultimaMensagem.length() > 100) {
            ultimaMensagem = ultimaMensagem.substring(0, 100) + "...";
        }
        long naoLidas = mensagemRepository.countByConversaIdAndLidaFalseAndRemetenteIdNot(conversa.getId(), viewerUserId);

        return ChatConversaResponse.builder()
                .id(conversa.getId())
                .userId(conversa.getUser().getId())
                .nomeUsuario(conversa.getUser().getNome())
                .roleUsuario(conversa.getUser().getRole().name())
                .status(conversa.getStatus())
                .assunto(conversa.getAssunto())
                .createdAt(conversa.getCreatedAt())
                .updatedAt(conversa.getUpdatedAt())
                .ultimaMensagem(ultimaMensagem)
                .naoLidas((int) naoLidas)
                .build();
    }

    private ChatMensagemResponse toMensagemResponse(ChatMensagem mensagem) {
        return ChatMensagemResponse.builder()
                .id(mensagem.getId())
                .conversaId(mensagem.getConversa().getId())
                .remetenteId(mensagem.getRemetente().getId())
                .nomeRemetente(mensagem.getRemetente().getNome())
                .conteudo(mensagem.getConteudo())
                .lida(mensagem.getLida())
                .createdAt(mensagem.getCreatedAt())
                .isAdmin(mensagem.getRemetente().getRole() == Role.AdminMax)
                .build();
    }
}
