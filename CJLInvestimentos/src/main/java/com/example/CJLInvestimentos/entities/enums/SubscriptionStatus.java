package com.example.CJLInvestimentos.entities.enums;

/**
 * Status da assinatura de pagamento da empresa (Stripe ou outro gateway).
 */
public enum SubscriptionStatus {
    /** Sem assinatura ativa (plano pode estar atribuído manualmente). */
    NONE,
    /** Assinatura paga e ativa. */
    ACTIVE,
    /** Pagamento atrasado / falhou. */
    PAST_DUE,
    /** Assinatura cancelada. */
    CANCELLED,
    /** Período de trial. */
    TRIAL
}
