package com.example.CJLInvestimentos.scheduler;

import com.example.CJLInvestimentos.services.SnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnapshotScheduler {

    private final SnapshotService snapshotService;

    // Executa todo domingo as 3h da manha
    @Scheduled(cron = "0 0 3 * * SUN")
    public void criarSnapshotsSemanal() {
        log.info("Iniciando criação de snapshots semanais...");
        try {
            snapshotService.criarSnapshotsGeral();
            log.info("Snapshots semanais criados com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao criar snapshots semanais: {}", e.getMessage(), e);
        }
    }
}
