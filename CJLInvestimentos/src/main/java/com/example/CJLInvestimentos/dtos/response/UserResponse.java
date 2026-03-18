package com.example.CJLInvestimentos.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String nome;
    private String email;
    private String role;
    private Long empresaId;
    private String empresaNome;
    private Boolean ativo;
    /** Status derivado: PENDENTE, ATIVO, INATIVO. */
    private String status;
    /** Telefone para WhatsApp. */
    private String telefone;
    /** CPF (somente dígitos). */
    private String cpf;
    /** WhatsApp. */
    private String whatsapp;
    /** Chat ID do Telegram (se vinculado). */
    private String telegramChatId;
    /** Se o usuário tem pelo menos uma inscrição de push ativa. */
    private Boolean pushInscrito;
    /** Quando true, o acesso à plataforma está bloqueado (ex.: por admin ou pagamento). */
    private Boolean bloqueado;
    /** Mensagem a exibir na tela de bloqueio (consultor: responsável pelo sistema; cliente: sua empresa). */
    private String motivoBloqueio;
    /** true se cliente foi excluído pelo consultor (empresa_id null, ativo=true). */
    private Boolean clienteExcluido;
    /** Se o cliente está em modo auto-gestão. */
    private Boolean autoGestao;
    /** Se o relatório completo gratuito já foi baixado. */
    private Boolean relatorioComplBaixado;
    /** Data em que o cliente foi excluído pelo consultor. */
    private LocalDateTime dataExclusao;
}
