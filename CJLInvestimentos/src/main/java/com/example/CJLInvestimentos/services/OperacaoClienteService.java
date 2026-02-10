package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.OperacaoClienteRequest;
import com.example.CJLInvestimentos.dtos.response.OperacaoClienteResponse;
import com.example.CJLInvestimentos.entities.OperacaoCliente;
import com.example.CJLInvestimentos.entities.Recomendacao;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.exceptions.ResourceNotFoundException;
import com.example.CJLInvestimentos.repositories.CarteiraClienteRepository;
import com.example.CJLInvestimentos.repositories.OperacaoClienteRepository;
import com.example.CJLInvestimentos.repositories.RecomendacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperacaoClienteService {

    private final OperacaoClienteRepository operacaoClienteRepository;
    private final RecomendacaoRepository recomendacaoRepository;
    private final CarteiraClienteRepository carteiraClienteRepository;

    public OperacaoClienteResponse registrar(Long recomendacaoId, OperacaoClienteRequest request, User cliente) {
        Recomendacao rec = recomendacaoRepository.findById(recomendacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendação não encontrada"));

        if (!carteiraClienteRepository.existsByCarteiraIdAndClienteId(rec.getCarteira().getId(), cliente.getId())) {
            throw new AccessDeniedException("Você não tem acesso a esta recomendação");
        }

        OperacaoCliente op = operacaoClienteRepository.save(
                OperacaoCliente.builder()
                        .recomendacao(rec)
                        .cliente(cliente)
                        .tipo(request.getTipo())
                        .precoExecutado(request.getPrecoExecutado())
                        .quantidade(request.getQuantidade())
                        .dataExecucao(request.getDataExecucao())
                        .observacao(request.getObservacao())
                        .build()
        );
        return toResponse(op);
    }

    public List<OperacaoClienteResponse> listarPorRecomendacao(Long recomendacaoId, User cliente) {
        Recomendacao rec = recomendacaoRepository.findById(recomendacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendação não encontrada"));
        if (!carteiraClienteRepository.existsByCarteiraIdAndClienteId(rec.getCarteira().getId(), cliente.getId())) {
            throw new AccessDeniedException("Sem acesso a esta recomendação");
        }
        return operacaoClienteRepository.findByRecomendacaoIdOrderByDataExecucaoDesc(recomendacaoId).stream()
                .filter(op -> op.getCliente().getId().equals(cliente.getId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<OperacaoClienteResponse> listarPorCliente(User cliente) {
        return operacaoClienteRepository.findByClienteIdOrderByDataExecucaoDesc(cliente.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Consultor: lista operações dos clientes em uma recomendação (da sua carteira). */
    public List<OperacaoClienteResponse> listarPorRecomendacaoComoConsultor(Long recomendacaoId, User consultor) {
        Recomendacao rec = recomendacaoRepository.findById(recomendacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendação não encontrada"));
        if (consultor.getRole() != Role.Admin || !rec.getCarteira().getConsultor().getId().equals(consultor.getId())) {
            throw new AccessDeniedException("Sem acesso a esta recomendação");
        }
        return operacaoClienteRepository.findByRecomendacaoIdOrderByDataExecucaoDesc(recomendacaoId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public long countByRecomendacaoId(Long recomendacaoId) {
        return operacaoClienteRepository.countByRecomendacaoId(recomendacaoId);
    }

    /** Cliente edita uma operação própria. */
    public OperacaoClienteResponse atualizar(Long operacaoId, OperacaoClienteRequest request, User cliente) {
        OperacaoCliente op = operacaoClienteRepository.findById(operacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Operação não encontrada"));
        if (!op.getCliente().getId().equals(cliente.getId())) {
            throw new AccessDeniedException("Só é possível editar suas próprias operações");
        }
        op.setTipo(request.getTipo());
        op.setPrecoExecutado(request.getPrecoExecutado());
        op.setQuantidade(request.getQuantidade());
        op.setDataExecucao(request.getDataExecucao());
        op.setObservacao(request.getObservacao());
        operacaoClienteRepository.save(op);
        return toResponse(op);
    }

    /** Cliente exclui uma operação própria. */
    public void excluir(Long operacaoId, User cliente) {
        OperacaoCliente op = operacaoClienteRepository.findById(operacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Operação não encontrada"));
        if (!op.getCliente().getId().equals(cliente.getId())) {
            throw new AccessDeniedException("Só é possível excluir suas próprias operações");
        }
        operacaoClienteRepository.delete(op);
    }

    private OperacaoClienteResponse toResponse(OperacaoCliente op) {
        return OperacaoClienteResponse.builder()
                .id(op.getId())
                .recomendacaoId(op.getRecomendacao().getId())
                .clienteId(op.getCliente().getId())
                .clienteNome(op.getCliente().getNome())
                .tipo(op.getTipo())
                .precoExecutado(op.getPrecoExecutado())
                .quantidade(op.getQuantidade())
                .dataExecucao(op.getDataExecucao())
                .observacao(op.getObservacao())
                .createdAt(op.getCreatedAt())
                .build();
    }
}
