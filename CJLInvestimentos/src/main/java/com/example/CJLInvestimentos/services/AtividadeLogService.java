package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.AtividadeLog;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.AtividadeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtividadeLogService {
    private final AtividadeLogRepository repository;

    @Async
    @Transactional
    public void registrar(User user, String tipo, String descricao, String link) {
        repository.save(AtividadeLog.builder()
                .user(user).tipo(tipo).descricao(descricao).link(link).build());
    }

    public List<AtividadeLog> listar(Long userId, int limit) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit));
    }

    @Transactional
    public void limparAntigas() { repository.deletarAntigas(); }
}
