package com.example.CJLInvestimentos.scheduler;

import com.example.CJLInvestimentos.entities.AlertaPreco;
import com.example.CJLInvestimentos.entities.Cotacao;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.TipoAlerta;
import com.example.CJLInvestimentos.repositories.AlertaPrecoRepository;
import com.example.CJLInvestimentos.repositories.CotacaoRepository;
import com.example.CJLInvestimentos.services.NotificationAsyncRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertaPrecoScheduler {

    private final AlertaPrecoRepository alertaPrecoRepository;
    private final CotacaoRepository cotacaoRepository;
    private final NotificationAsyncRunner notificationAsyncRunner;

    /** Verifica alertas de preco a cada 2 minutos. */
    @Scheduled(fixedRate = 120_000)
    public void verificarAlertas() {
        List<AlertaPreco> alertasAtivos = alertaPrecoRepository.findByAtivoTrueAndDisparadoFalse();

        if (alertasAtivos.isEmpty()) return;

        for (AlertaPreco alerta : alertasAtivos) {
            try {
                Optional<Cotacao> cotacaoOpt = cotacaoRepository
                        .findTopByMoedaAndParMoedaOrderByDataHoraDesc(alerta.getMoeda(), alerta.getParMoeda());

                if (cotacaoOpt.isEmpty()) continue;

                Cotacao cotacao = cotacaoOpt.get();
                BigDecimal precoAtual = cotacao.getPrecoVenda();
                boolean disparar = false;

                if (alerta.getTipoAlerta() == TipoAlerta.ACIMA) {
                    disparar = precoAtual.compareTo(alerta.getPrecoAlerta()) >= 0;
                } else if (alerta.getTipoAlerta() == TipoAlerta.ABAIXO) {
                    disparar = precoAtual.compareTo(alerta.getPrecoAlerta()) <= 0;
                }

                if (disparar) {
                    alerta.setDisparado(true);
                    alerta.setDataDisparo(LocalDateTime.now());
                    alertaPrecoRepository.save(alerta);

                    User user = alerta.getUser();
                    notificationAsyncRunner.notificarAlertaPrecoAsync(
                            user, alerta, precoAtual
                    );

                    log.info("Alerta de preco disparado: {} {}/{} {} — preco atual: {}",
                            alerta.getTipoAlerta(), alerta.getMoeda(), alerta.getParMoeda(),
                            alerta.getPrecoAlerta(), precoAtual);
                }
            } catch (Exception e) {
                log.error("Erro ao verificar alerta id={}: {}", alerta.getId(), e.getMessage());
            }
        }
    }
}
