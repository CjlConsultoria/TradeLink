package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.CarteiraRequest;
import com.example.CJLInvestimentos.dtos.response.CarteiraResponse;
import com.example.CJLInvestimentos.dtos.response.UserResponse;
import com.example.CJLInvestimentos.entities.Carteira;
import com.example.CJLInvestimentos.entities.CarteiraCliente;
import com.example.CJLInvestimentos.entities.Recomendacao;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.exceptions.ResourceNotFoundException;
import com.example.CJLInvestimentos.repositories.AlocacaoAlvoRepository;
import com.example.CJLInvestimentos.repositories.CarteiraClienteRepository;
import com.example.CJLInvestimentos.repositories.CarteiraRepository;
import com.example.CJLInvestimentos.repositories.OperacaoClienteRepository;
import com.example.CJLInvestimentos.repositories.RecomendacaoRepository;
import com.example.CJLInvestimentos.repositories.RecomendacaoResolvidaClienteRepository;
import com.example.CJLInvestimentos.repositories.SnapshotPortfolioRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarteiraService {

    private final CarteiraRepository carteiraRepository;
    private final CarteiraClienteRepository carteiraClienteRepository;
    private final RecomendacaoRepository recomendacaoRepository;
    private final RecomendacaoResolvidaClienteRepository resolvidaClienteRepository;
    private final OperacaoClienteRepository operacaoClienteRepository;
    private final UserRepository userRepository;
    private final AlocacaoAlvoRepository alocacaoAlvoRepository;
    private final SnapshotPortfolioRepository snapshotPortfolioRepository;

    public CarteiraResponse criar(CarteiraRequest request, User consultor) {
        Carteira carteira = carteiraRepository.save(
                Carteira.builder()
                        .nome(request.getNome())
                        .descricao(request.getDescricao())
                        .empresa(consultor.getEmpresa())
                        .consultor(consultor)
                        .build()
        );
        return toResponse(carteira);
    }

    public CarteiraResponse atualizar(Long id, CarteiraRequest request, User consultor) {
        Carteira carteira = getCarteiraDoConsultor(id, consultor);
        carteira.setNome(request.getNome());
        carteira.setDescricao(request.getDescricao());
        carteiraRepository.save(carteira);
        return toResponse(carteira);
    }

    public void desativar(Long id, User consultor) {
        Carteira carteira = getCarteiraDoConsultor(id, consultor);
        carteira.setAtiva(false);
        carteiraRepository.save(carteira);
    }

    @Transactional
    public void excluir(Long id, User consultor) {
        Carteira carteira = getCarteiraDoConsultor(id, consultor);
        long totalClientes = carteiraClienteRepository.countByCarteiraId(id);
        if (totalClientes > 0) {
            throw new BusinessException("Não é possível excluir carteira com clientes associados. Remova os clientes antes.");
        }
        List<Recomendacao> recomendacoes = recomendacaoRepository.findByCarteiraId(id);
        for (Recomendacao rec : recomendacoes) {
            resolvidaClienteRepository.deleteByRecomendacaoId(rec.getId());
            operacaoClienteRepository.deleteByRecomendacaoId(rec.getId());
        }
        recomendacaoRepository.deleteAll(recomendacoes);
        alocacaoAlvoRepository.deleteByCarteiraId(id);
        snapshotPortfolioRepository.deleteByCarteiraId(id);
        carteiraRepository.delete(carteira);
    }

    public List<CarteiraResponse> listarPorConsultor(User consultor) {
        return carteiraRepository.findByConsultorId(consultor.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CarteiraResponse> listarPorCliente(User cliente) {
        return carteiraClienteRepository.findByClienteId(cliente.getId()).stream()
                .map(cc -> toResponse(cc.getCarteira()))
                .collect(Collectors.toList());
    }

    public CarteiraResponse buscarPorId(Long id, User user) {
        Carteira carteira = carteiraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada"));

        if (user.getRole() == Role.Admin) {
            if (!carteira.getConsultor().getId().equals(user.getId())) {
                throw new AccessDeniedException("Sem acesso a esta carteira");
            }
        } else if (user.getRole() == Role.Cliente) {
            if (!carteiraClienteRepository.existsByCarteiraIdAndClienteId(id, user.getId())) {
                throw new AccessDeniedException("Sem acesso a esta carteira");
            }
        }

        return toResponse(carteira);
    }

    public void atribuirCliente(Long carteiraId, Long clienteId, User consultor) {
        Carteira carteira = getCarteiraDoConsultor(carteiraId, consultor);

        User cliente = userRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        if (cliente.getRole() != Role.Cliente) {
            throw new BusinessException("Usuário não é um cliente");
        }

        if (!cliente.getEmpresa().getId().equals(consultor.getEmpresa().getId())) {
            throw new BusinessException("Cliente não pertence à mesma empresa");
        }

        if (carteiraClienteRepository.existsByCarteiraIdAndClienteId(carteiraId, clienteId)) {
            throw new BusinessException("Cliente já atribuído a esta carteira");
        }

        carteiraClienteRepository.save(
                CarteiraCliente.builder()
                        .carteira(carteira)
                        .cliente(cliente)
                        .build()
        );
    }

    public void removerCliente(Long carteiraId, Long clienteId, User consultor) {
        getCarteiraDoConsultor(carteiraId, consultor);

        CarteiraCliente cc = carteiraClienteRepository.findByCarteiraIdAndClienteId(carteiraId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não está atribuído a esta carteira"));

        carteiraClienteRepository.delete(cc);
    }

    public List<UserResponse> listarClientesDaCarteira(Long carteiraId, User consultor) {
        getCarteiraDoConsultor(carteiraId, consultor);

        return carteiraClienteRepository.findByCarteiraId(carteiraId).stream()
                .map(cc -> UserResponse.builder()
                        .id(cc.getCliente().getId())
                        .nome(cc.getCliente().getNome())
                        .email(cc.getCliente().getEmail())
                        .role(cc.getCliente().getRole().name())
                        .build())
                .collect(Collectors.toList());
    }

    private Carteira getCarteiraDoConsultor(Long carteiraId, User consultor) {
        Carteira carteira = carteiraRepository.findById(carteiraId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada"));

        if (!carteira.getConsultor().getId().equals(consultor.getId())) {
            throw new AccessDeniedException("Sem acesso a esta carteira");
        }

        return carteira;
    }

    private CarteiraResponse toResponse(Carteira carteira) {
        int totalClientes = (int) carteiraClienteRepository.countByCarteiraId(carteira.getId());
        int totalRecomendacoes = (int) recomendacaoRepository.countByCarteiraId(carteira.getId());

        return CarteiraResponse.builder()
                .id(carteira.getId())
                .nome(carteira.getNome())
                .descricao(carteira.getDescricao())
                .empresaId(carteira.getEmpresa().getId())
                .empresaNome(carteira.getEmpresa().getNome())
                .consultorId(carteira.getConsultor().getId())
                .consultorNome(carteira.getConsultor().getNome())
                .ativa(carteira.getAtiva())
                .createdAt(carteira.getCreatedAt())
                .totalClientes(totalClientes)
                .totalRecomendacoes(totalRecomendacoes)
                .margemErro(carteira.getMargemErro())
                .moedaReferenciaRebalance(carteira.getMoedaReferenciaRebalance())
                .rebalanceAtivo(carteira.getRebalanceAtivo())
                .temAlocacoes(alocacaoAlvoRepository.existsByCarteiraId(carteira.getId()))
                .build();
    }
}
