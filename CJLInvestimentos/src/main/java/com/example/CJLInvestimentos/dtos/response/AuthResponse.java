package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String mensagem;
    private String role;
    private Long empresaId;
    private Long userId;
    private String nome;
    private Boolean bloqueado;
    /** true quando bloqueio é por decisão do admin ou empresa inativa (não por pagamento). */
    private Boolean bloqueadoPorAdmin;
    private String motivoBloqueio;
    /** true quando cliente foi excluído pelo consultor (empresa_id null, ativo=true). */
    private Boolean clienteExcluido;
    /** true quando cliente tem auto-gestão ativa com subscription válida. */
    private Boolean autoGestaoAtiva;
    /** true quando o trial do usuário ou empresa está ativo. */
    private Boolean trialAtivo;
    /** Data de fim do trial. */
    private String trialFim;
    /** true quando trial expirou e não tem plano pago — precisa escolher plano. */
    private Boolean precisaEscolherPlano;
    /** true quando o usuário foi auto-cadastrado. */
    private Boolean autoCadastro;
    /** true quando login requer verificação 2FA (OTP por e-mail). */
    private Boolean requires2FA;
    /** true quando cliente marketplace tem pagamento em atraso. */
    private Boolean marketplaceBloqueado;
    /** Origem do vínculo do cliente (MARKETPLACE, CONVITE, etc). */
    private String origemVinculo;
    /** Refresh token para renovação silenciosa de sessão (7 dias). */
    private String refreshToken;
}
