package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.OtpCode;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final NotificationAsyncRunner notificationAsyncRunner;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public void generateAndSend(User user) {
        // Invalida OTPs anteriores não usados
        otpCodeRepository.deleteByUserIdAndUsedFalse(user.getId());

        String code = generateCode();

        OtpCode otp = OtpCode.builder()
                .userId(user.getId())
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES))
                .used(false)
                .build();

        otpCodeRepository.save(otp);

        notificationAsyncRunner.enviarEmailOtpAsync(user.getEmail(), user.getNome(), code);
        log.info("OTP gerado e enviado para userId={}", user.getId());
    }

    @Transactional
    public boolean validate(Long userId, String code) {
        OtpCode otp = otpCodeRepository.findByUserIdAndCodeAndUsedFalse(userId, code)
                .orElse(null);

        if (otp == null) {
            return false;
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        otp.setUsed(true);
        otpCodeRepository.save(otp);
        return true;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
