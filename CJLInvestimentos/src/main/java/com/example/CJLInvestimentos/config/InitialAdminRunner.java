package com.example.CJLInvestimentos.config;

import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cria o primeiro usuário AdminMax (Super Admin) se ainda não existir.
 * Uso: defina ADMIN_INITIAL_EMAIL e ADMIN_INITIAL_PASSWORD no ambiente (Rancher/Produção).
 * Se não definidos, cria admin@tradelink.local com senha aleatória (logada uma vez).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InitialAdminRunner implements ApplicationRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@tradelink.local";
    private static final int RANDOM_PASSWORD_BYTES = 24;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.countByRole(Role.AdminMax) > 0) {
            return;
        }
        String email = env.getProperty("ADMIN_INITIAL_EMAIL");
        if (email == null || email.isBlank()) email = System.getenv("ADMIN_INITIAL_EMAIL");
        if (email == null || email.isBlank()) email = DEFAULT_ADMIN_EMAIL;

        String rawPassword = env.getProperty("ADMIN_INITIAL_PASSWORD");
        if (rawPassword == null || rawPassword.isBlank()) rawPassword = System.getenv("ADMIN_INITIAL_PASSWORD");
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = generateSecurePassword();
            log.info("========================================");
            log.info("PRIMEIRO ACESSO ADMIN - Use as credenciais abaixo (troque a senha após o login):");
            log.info("  E-mail: {}", email);
            log.info("  Senha:  {}", rawPassword);
            log.info("========================================");
        }
        String encoded = passwordEncoder.encode(rawPassword);
        User admin = User.builder()
                .nome("Super Admin")
                .email(email)
                .senha(encoded)
                .role(Role.AdminMax)
                .empresa(null)
                .ativo(true)
                .build();
        userRepository.save(admin);
        log.info("Usuário AdminMax inicial criado: {}", email);
    }

    private static String generateSecurePassword() {
        SecureRandom sr = new SecureRandom();
        byte[] bytes = new byte[RANDOM_PASSWORD_BYTES];
        sr.nextBytes(bytes);
        return Base64.getEncoder().withoutPadding().encodeToString(bytes).replace("+", "x").replace("/", "z").substring(0, 20);
    }
}
