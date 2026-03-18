package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.FaqRequest;
import com.example.CJLInvestimentos.dtos.response.FaqResponse;
import com.example.CJLInvestimentos.entities.Faq;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.repositories.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    /** Lista FAQs ativas (público). */
    @Transactional(readOnly = true)
    public List<FaqResponse> listarPublico() {
        return faqRepository.findByAtivoTrueOrderByOrdemAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Lista todas FAQs (AdminMax). */
    @Transactional(readOnly = true)
    public List<FaqResponse> listarTodas() {
        return faqRepository.findAllByOrderByOrdemAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FaqResponse criar(FaqRequest request) {
        Faq faq = Faq.builder()
                .pergunta(request.getPergunta())
                .resposta(request.getResposta())
                .categoria(request.getCategoria())
                .ordem(request.getOrdem() != null ? request.getOrdem() : 0)
                .build();
        faq = faqRepository.save(faq);
        return toResponse(faq);
    }

    @Transactional
    public FaqResponse atualizar(Long id, FaqRequest request) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new BusinessException("FAQ não encontrada."));
        faq.setPergunta(request.getPergunta());
        faq.setResposta(request.getResposta());
        faq.setCategoria(request.getCategoria());
        if (request.getOrdem() != null) faq.setOrdem(request.getOrdem());
        faq = faqRepository.save(faq);
        return toResponse(faq);
    }

    @Transactional
    public void excluir(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new BusinessException("FAQ não encontrada."));
        faq.setAtivo(false);
        faqRepository.save(faq);
    }

    @Transactional
    public void reordenar(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            Faq faq = faqRepository.findById(ids.get(i)).orElse(null);
            if (faq != null) {
                faq.setOrdem(i);
                faqRepository.save(faq);
            }
        }
    }

    private FaqResponse toResponse(Faq faq) {
        return FaqResponse.builder()
                .id(faq.getId())
                .pergunta(faq.getPergunta())
                .resposta(faq.getResposta())
                .categoria(faq.getCategoria())
                .ordem(faq.getOrdem())
                .ativo(faq.getAtivo())
                .build();
    }
}
