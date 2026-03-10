package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Serviço para gerenciar o período de trial (5 dias gratuitos) de usuários auto-cadastrados.
 */
@Service
@RequiredArgsConstructor
public class TrialService {

    /** Verifica se o trial do User (cliente auto-cadastro) está ativo. */
    public boolean isTrialAtivo(User user) {
        if (user.getTrialFim() == null) return false;
        return LocalDateTime.now().isBefore(user.getTrialFim());
    }

    /** Verifica se o trial da Empresa (consultor auto-cadastro) está ativo. */
    public boolean isTrialAtivoEmpresa(Empresa empresa) {
        if (empresa == null || empresa.getTrialFim() == null) return false;
        return LocalDateTime.now().isBefore(empresa.getTrialFim());
    }

    /** Verifica se o trial do User existiu e expirou (sem plano pago). */
    public boolean isTrialExpirado(User user) {
        if (user.getTrialFim() == null) return false;
        return LocalDateTime.now().isAfter(user.getTrialFim())
                && !"ACTIVE".equals(user.getSubscriptionStatus());
    }

    /** Verifica se o trial da Empresa existiu e expirou (sem plano pago). */
    public boolean isTrialExpiradoEmpresa(Empresa empresa) {
        if (empresa == null || empresa.getTrialFim() == null) return false;
        return LocalDateTime.now().isAfter(empresa.getTrialFim())
                && empresa.getSubscriptionStatus() != com.example.CJLInvestimentos.entities.enums.SubscriptionStatus.ACTIVE;
    }

    /** Retorna dias restantes do trial do User. */
    public long getDiasRestantes(User user) {
        if (user.getTrialFim() == null) return 0;
        long dias = ChronoUnit.DAYS.between(LocalDateTime.now(), user.getTrialFim());
        return Math.max(0, dias);
    }

    /** Retorna dias restantes do trial da Empresa. */
    public long getDiasRestantesEmpresa(Empresa empresa) {
        if (empresa == null || empresa.getTrialFim() == null) return 0;
        long dias = ChronoUnit.DAYS.between(LocalDateTime.now(), empresa.getTrialFim());
        return Math.max(0, dias);
    }
}
