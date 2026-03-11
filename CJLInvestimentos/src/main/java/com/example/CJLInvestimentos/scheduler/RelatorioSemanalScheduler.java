package com.example.CJLInvestimentos.scheduler;

import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.services.EmailTemplateService;
import com.example.CJLInvestimentos.services.NotificationService;
import com.example.CJLInvestimentos.services.RelatorioClienteService;
import com.example.CJLInvestimentos.dtos.response.ResumoRelatorioClienteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RelatorioSemanalScheduler {

    private final UserRepository userRepository;
    private final RelatorioClienteService relatorioClienteService;
    private final NotificationService notificationService;
    private final EmailTemplateService emailTemplateService;

    /** Executa toda segunda-feira às 8h da manhã. */
    @Scheduled(cron = "0 0 8 * * MON")
    public void enviarRelatorioSemanal() {
        log.info("Iniciando envio de relatórios semanais...");
        List<User> clientes = userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.Cliente && u.getAtivo() != null && u.getAtivo())
            .toList();

        int enviados = 0;
        for (User cliente : clientes) {
            try {
                ResumoRelatorioClienteResponse resumo = relatorioClienteService.resumo(cliente.getId(), null, null);
                if (resumo == null || resumo.getTotalOperacoes() == 0) continue;

                String html = emailTemplateService.buildRelatorioSemanal(cliente, resumo);
                notificationService.enviarEmailHtml(cliente.getEmail(), "TradeLink — Seu resumo semanal", html);
                enviados++;
            } catch (Exception e) {
                log.warn("Falha ao enviar relatório semanal para {}: {}", cliente.getEmail(), e.getMessage());
            }
        }
        log.info("Relatórios semanais enviados: {}/{}", enviados, clientes.size());
    }
}
