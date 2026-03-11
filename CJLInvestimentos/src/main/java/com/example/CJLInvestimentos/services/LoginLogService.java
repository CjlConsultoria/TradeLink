package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.LoginLog;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.LoginLogRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;
    private final UserRepository userRepository;
    private final AtividadeLogService atividadeLogService;

    @Async
    public void registrar(Long userId, String email, String ip, String userAgent, boolean sucesso, String motivoFalha) {
        try {
            LoginLog logEntry = LoginLog.builder()
                    .userId(userId)
                    .email(email)
                    .ip(ip)
                    .userAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent)
                    .dataHora(LocalDateTime.now())
                    .sucesso(sucesso)
                    .motivoFalha(motivoFalha)
                    .build();
            loginLogRepository.save(logEntry);

            // Registrar atividade de login bem-sucedido na timeline
            if (sucesso && userId != null) {
                try {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        atividadeLogService.registrar(user, "LOGIN",
                                "Login realizado com sucesso", null);
                    }
                } catch (Exception ex) {
                    log.warn("Falha ao registrar atividade de login: {}", ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao registrar log de login: {}", e.getMessage());
        }
    }

    public List<LoginLog> listarPorUsuario(Long userId, int limit) {
        return loginLogRepository.findByUserIdOrderByDataHoraDesc(userId, PageRequest.of(0, limit));
    }

    public List<LoginLog> listarTodos(int limit) {
        return loginLogRepository.findAllByOrderByDataHoraDesc(PageRequest.of(0, limit));
    }
}
