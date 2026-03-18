package com.example.CJLInvestimentos.services;
import com.example.CJLInvestimentos.dtos.request.PlanoRequest;
import com.example.CJLInvestimentos.dtos.response.PlanoResponse;
import com.example.CJLInvestimentos.entities.Plano;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.exceptions.ResourceNotFoundException;
import com.example.CJLInvestimentos.repositories.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class PlanoService {
    private final PlanoRepository planoRepository;
    public PlanoResponse criar(PlanoRequest request) {
        if (planoRepository.findByNome(request.getNome()).isPresent()) {
            throw new BusinessException("Já existe um plano com este nome");
        }
        String tipo = request.getTipo() != null ? request.getTipo() : "CONSULTOR";
        Plano plano = planoRepository.save(
                Plano.builder()
                        .nome(request.getNome())
                        .tipo(tipo)
                        .maxUsuarios(request.getMaxUsuarios())
                        .preco(request.getPreco())
                        .stripePriceId(request.getStripePriceId())
                        .build()
        );
        return toResponse(plano);
    }
    public PlanoResponse atualizar(Long id, PlanoRequest request) {
        Plano plano = planoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado"));
        planoRepository.findByNome(request.getNome())
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> { throw new BusinessException("Já existe outro plano com este nome"); });
        plano.setNome(request.getNome());
        if (request.getTipo() != null) plano.setTipo(request.getTipo());
        plano.setMaxUsuarios(request.getMaxUsuarios());
        plano.setPreco(request.getPreco());
        plano.setStripePriceId(request.getStripePriceId());
        planoRepository.save(plano);
        return toResponse(plano);
    }
    public void desativar(Long id) {
        Plano plano = planoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado"));
        plano.setAtivo(false);
        planoRepository.save(plano);
    }
    public void ativar(Long id) {
        Plano plano = planoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado"));
        plano.setAtivo(true);
        planoRepository.save(plano);
    }
    public List<PlanoResponse> listarTodos() {
        return planoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    public List<PlanoResponse> listarAtivos() {
        return planoRepository.findByAtivoTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    public PlanoResponse buscarPorId(Long id) {
        Plano plano = planoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado"));
        return toResponse(plano);
    }
    /** Retorna o preço atual do plano Auto-Gestão cadastrado no banco, ou fallback R$9,99. */
    public BigDecimal getPrecoAutoGestao() {
        return planoRepository.findFirstByTipoAndAtivoTrue("AUTO_GESTAO")
                .map(Plano::getPreco)
                .orElse(new BigDecimal("9.99"));
    }

    /** Retorna o preço atual em centavos (para Stripe) do plano Auto-Gestão. */
    public long getPrecoAutoGestaoCentavos() {
        BigDecimal preco = getPrecoAutoGestao();
        return preco.multiply(new BigDecimal("100")).longValue();
    }

    /** Retorna o nome do plano Auto-Gestão cadastrado no banco. */
    public String getNomeAutoGestao() {
        return planoRepository.findFirstByTipoAndAtivoTrue("AUTO_GESTAO")
                .map(Plano::getNome)
                .orElse("Auto-Gestão");
    }

    public PlanoResponse toResponse(Plano plano) {
        return PlanoResponse.builder()
                .id(plano.getId())
                .nome(plano.getNome())
                .tipo(plano.getTipo())
                .maxUsuarios(plano.getMaxUsuarios())
                .preco(plano.getPreco())
                .ativo(plano.getAtivo())
                .createdAt(plano.getCreatedAt())
                .stripePriceId(plano.getStripePriceId())
                .build();
    }
}