package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaResponse {
    private Long id;
    private String nome;
    private String cnpj;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private Long planoId;
    private String planoNome;
    private Integer maxUsuarios;
    private Integer totalUsuarios;
    private Integer totalConsultores;
    private Integer totalClientes;

    /** Canais de notificação habilitados pela empresa. */
    private Boolean notificacaoEmail;
    private Boolean notificacaoTelegram;
    private Boolean notificacaoPush;
    private Boolean notificacaoWhatsApp;
    private Boolean notificacaoSms;

    /** Status da assinatura de pagamento (Stripe). */
    private String subscriptionStatus;
    /** Fim do período atual de cobrança. */
    private Instant currentPeriodEnd;
    /** Se a empresa está com acesso liberado (em dia ou dentro da tolerância de 5 dias). */
    private Boolean acessoPermitido;
    /** Bloqueio manual pelo AdminMax: true = todos os consultores e clientes da empresa sem acesso à plataforma. */
    private Boolean acessoBloqueadoPorAdmin;
    /** Status para AdminMax: EM_DIA, VENCIDO, EM_ATRASO, SEM_ASSINATURA. */
    private String statusPagamento;
}
