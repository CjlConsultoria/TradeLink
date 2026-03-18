package com.example.CJLInvestimentos.scheduler;

import com.example.CJLInvestimentos.repositories.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {

    private final OtpCodeRepository otpCodeRepository;

    /** Limpa OTPs expirados e usados a cada 6 horas. */
    @Scheduled(cron = "0 0 */6 * * *")
    public void limparOtpsExpirados() {
        try {
            int removidos = otpCodeRepository.deleteExpiredOrUsed(LocalDateTime.now());
            if (removidos > 0) {
                log.info("OTP cleanup: {} código(s) expirado(s)/usado(s) removido(s)", removidos);
            }
        } catch (Exception e) {
            log.warn("Falha na limpeza de OTPs: {}", e.getMessage());
        }
    }
}
