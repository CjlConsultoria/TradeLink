package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.NotificacaoInApp;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.NotificacaoInAppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificacaoInAppService {

    private final NotificacaoInAppRepository repository;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Cria uma notificacao in-app para o usuario. */
    public void criar(User user, String titulo, String mensagem, String tipo, String link) {
        repository.save(NotificacaoInApp.builder()
                .user(user)
                .titulo(titulo)
                .mensagem(mensagem)
                .tipo(tipo != null ? tipo : "SISTEMA")
                .link(link)
                .build());
    }

    /** Lista as ultimas notificacoes (ate 50). */
    public List<Map<String, Object>> listar(Long userId, int limit) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Math.min(limit, 50)))
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    /** Conta nao lidas. */
    public long contarNaoLidas(Long userId) {
        return repository.countByUserIdAndLidaFalse(userId);
    }

    /** Marca uma notificacao como lida. */
    public void marcarComoLida(Long userId, Long notificacaoId) {
        repository.findById(notificacaoId).ifPresent(n -> {
            if (n.getUser().getId().equals(userId)) {
                n.setLida(true);
                repository.save(n);
            }
        });
    }

    /** Marca todas como lidas. */
    public int marcarTodasComoLidas(Long userId) {
        return repository.marcarTodasComoLidas(userId);
    }

    /** Limpa notificacoes com mais de 30 dias. */
    public int limparAntigas() {
        return repository.deletarAntigas(LocalDateTime.now().minusDays(30));
    }

    private Map<String, Object> toMap(NotificacaoInApp n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", n.getId());
        m.put("titulo", n.getTitulo());
        m.put("mensagem", n.getMensagem());
        m.put("tipo", n.getTipo());
        m.put("link", n.getLink());
        m.put("lida", Boolean.TRUE.equals(n.getLida()));
        m.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().format(DTF) : null);
        return m;
    }
}
