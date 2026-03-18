package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.Recomendacao;
import com.example.CJLInvestimentos.entities.enums.StatusRecomendacao;
import com.example.CJLInvestimentos.repositories.RecomendacaoRepository;
import com.example.CJLInvestimentos.repositories.CarteiraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CopyTradingService {

    private final RecomendacaoRepository recomendacaoRepository;
    private final CarteiraRepository carteiraRepository;
    private final NotificationAsyncRunner notificationAsyncRunner;

    /**
     * Replica uma recomendação para todas as carteiras de destino.
     * @param recomendacaoOrigem the source recommendation
     * @param carteiraIds target portfolio IDs
     * @return list of created recommendations
     */
    @Transactional
    public List<Recomendacao> replicar(Recomendacao recomendacaoOrigem, List<Long> carteiraIds) {
        List<Recomendacao> replicadas = new ArrayList<>();
        for (Long carteiraId : carteiraIds) {
            var carteira = carteiraRepository.findById(carteiraId).orElse(null);
            if (carteira == null || carteira.getId().equals(recomendacaoOrigem.getCarteira().getId())) continue;

            Recomendacao copia = new Recomendacao();
            copia.setCarteira(carteira);
            copia.setTipo(recomendacaoOrigem.getTipo());
            copia.setMoeda(recomendacaoOrigem.getMoeda());
            copia.setParMoeda(recomendacaoOrigem.getParMoeda());
            copia.setPrecoEntrada(recomendacaoOrigem.getPrecoEntrada());
            copia.setPrecoAlvo(recomendacaoOrigem.getPrecoAlvo());
            copia.setStopLoss(recomendacaoOrigem.getStopLoss());
            copia.setQuantidade(recomendacaoOrigem.getQuantidade());
            copia.setPercentual(recomendacaoOrigem.getPercentual());
            copia.setModoPercentual(recomendacaoOrigem.getModoPercentual());
            copia.setObservacao("[Copy] " + (recomendacaoOrigem.getObservacao() != null ? recomendacaoOrigem.getObservacao() : ""));
            copia.setStatus(StatusRecomendacao.ATIVA);
            replicadas.add(recomendacaoRepository.save(copia));
        }
        log.info("Copy Trading: replicou recomendação {} para {} carteiras", recomendacaoOrigem.getId(), replicadas.size());
        return replicadas;
    }
}
