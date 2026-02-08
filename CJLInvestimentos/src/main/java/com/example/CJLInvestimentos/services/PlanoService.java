package com.example.CJLInvestimentos.services;
import com.example.CJLInvestimentos.dtos.request.PlanoRequest;
import com.example.CJLInvestimentos.dtos.response.PlanoResponse;
import com.example.CJLInvestimentos.entities.Plano;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.exceptions.ResourceNotFoundException;
import com.example.CJLInvestimentos.repositories.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
        Plano plano = planoRepository.save(
                Plano.builder()
                        .nome(request.getNome())
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
    public PlanoResponse toResponse(Plano plano) {
        return PlanoResponse.builder()
                .id(plano.getId())
                .nome(plano.getNome())
                .maxUsuarios(plano.getMaxUsuarios())
                .preco(plano.getPreco())
                .ativo(plano.getAtivo())
                .createdAt(plano.getCreatedAt())
                .stripePriceId(plano.getStripePriceId())
                .build();
    }
}