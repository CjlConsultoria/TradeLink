package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.AtualizarUsuarioAdminRequest;
import com.example.CJLInvestimentos.dtos.request.PushSubscriptionRequest;
import com.example.CJLInvestimentos.dtos.request.RegisterRequest;
import com.example.CJLInvestimentos.dtos.response.UserResponse;
import com.example.CJLInvestimentos.entities.PushSubscription;
import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.exceptions.BusinessException;
import com.example.CJLInvestimentos.exceptions.ResourceNotFoundException;
import com.example.CJLInvestimentos.repositories.EmpresaRepository;
import com.example.CJLInvestimentos.repositories.PushSubscriptionRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmpresaRepository empresaRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationAsyncRunner notificationAsyncRunner;

    public UserResponse criarConsultor(RegisterRequest request, Long empresaId) {
        return criarUsuario(request, empresaId, Role.Admin);
    }

    public UserResponse criarCliente(RegisterRequest request, Long empresaId) {
        return criarUsuario(request, empresaId, Role.Cliente);
    }

    private UserResponse criarUsuario(RegisterRequest request, Long empresaId, Role role) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("E-mail já cadastrado");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        if (empresa.getPlano() != null) {
            long totalUsuarios = userRepository.countByEmpresaId(empresaId);
            if (totalUsuarios >= empresa.getPlano().getMaxUsuarios()) {
                throw new BusinessException("Limite de usuários do plano atingido (" + empresa.getPlano().getMaxUsuarios() + ")");
            }
        }

        User user = userRepository.save(
                User.builder()
                        .nome(request.getNome())
                        .email(request.getEmail())
                        .senha(passwordEncoder.encode(request.getSenha()))
                        .role(role)
                        .empresa(empresa)
                        .build()
        );
        notificationAsyncRunner.enviarEmailNovoUsuarioAsync(user.getEmail(), user.getNome(), user.getRole());
        return toResponse(user);
    }

    public List<UserResponse> listarPorEmpresa(Long empresaId) {
        return userRepository.findByEmpresaId(empresaId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> listarClientesDaEmpresa(Long empresaId) {
        return userRepository.findByEmpresaIdAndRole(empresaId, Role.Cliente).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> listarTodos() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse buscarPorId(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return toResponse(user);
    }

    public void desativar(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        user.setAtivo(false);
        userRepository.save(user);
    }

    /** Consultor inativa um cliente da própria empresa. */
    public void inativarClientePorConsultor(Long clienteId, User consultor) {
        User cliente = userRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        if (cliente.getRole() != Role.Cliente) {
            throw new BusinessException("Usuário não é um cliente");
        }
        if (consultor.getEmpresa() == null || cliente.getEmpresa() == null
                || !cliente.getEmpresa().getId().equals(consultor.getEmpresa().getId())) {
            throw new BusinessException("Cliente não pertence à sua empresa");
        }
        cliente.setAtivo(false);
        userRepository.save(cliente);
    }

    public void ativar(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        user.setAtivo(true);
        userRepository.save(user);
    }

    /** Super Admin: atualiza qualquer usuário (nome, email, ativo, role, empresa). */
    public UserResponse atualizarPorAdminMax(Long id, AtualizarUsuarioAdminRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (request.getNome() != null && !request.getNome().isBlank()) {
            user.setNome(request.getNome().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim().toLowerCase();
            userRepository.findByEmail(email).ifPresent(outro -> {
                if (!outro.getId().equals(id)) {
                    throw new BusinessException("E-mail já cadastrado para outro usuário");
                }
            });
            user.setEmail(email);
        }
        if (request.getAtivo() != null) {
            user.setAtivo(request.getAtivo());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
            if (request.getRole() == Role.AdminMax) {
                user.setEmpresa(null);
            }
        }
        if (request.getEmpresaId() != null) {
            if (request.getEmpresaId() <= 0) {
                user.setEmpresa(null);
            } else {
                Empresa empresa = empresaRepository.findById(request.getEmpresaId())
                        .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
                user.setEmpresa(empresa);
            }
        }
        userRepository.save(user);
        return toResponse(user);
    }

    /** Super Admin: altera a senha de qualquer usuário. */
    public void alterarSenhaPorAdminMax(Long id, String novaSenha) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new BusinessException("Senha deve ter no mínimo 6 caracteres");
        }
        user.setSenha(passwordEncoder.encode(novaSenha));
        userRepository.save(user);
    }

    public UserResponse atualizarTelegramChatId(User user, String telegramChatId) {
        user.setTelegramChatId(telegramChatId == null || telegramChatId.isBlank() ? null : telegramChatId.trim());
        userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse registrarPushSubscription(User user, PushSubscriptionRequest request) {
        if (request == null || request.getEndpoint() == null || request.getEndpoint().isBlank()
                || request.getKeys() == null || request.getKeys().getP256dh() == null || request.getKeys().getAuth() == null) {
            throw new BusinessException("Inscrição push inválida (endpoint e keys obrigatórios)");
        }
        pushSubscriptionRepository.findByUserId(user.getId()).stream()
                .filter(s -> request.getEndpoint().equals(s.getEndpoint()))
                .findFirst()
                .ifPresentOrElse(
                        s -> {
                            s.setP256dhKey(request.getKeys().getP256dh());
                            s.setAuthKey(request.getKeys().getAuth());
                            pushSubscriptionRepository.save(s);
                        },
                        () -> pushSubscriptionRepository.save(PushSubscription.builder()
                                .user(user)
                                .endpoint(request.getEndpoint())
                                .p256dhKey(request.getKeys().getP256dh())
                                .authKey(request.getKeys().getAuth())
                                .build())
                );
        return toResponse(user);
    }

    public void removerPushSubscription(User user, String endpoint) {
        if (endpoint != null && !endpoint.isBlank()) {
            pushSubscriptionRepository.deleteByUserIdAndEndpoint(user.getId(), endpoint);
        }
    }

    public UserResponse toResponse(User user) {
        boolean pushInscrito = pushSubscriptionRepository.findByUserId(user.getId()).stream().findAny().isPresent();
        return UserResponse.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getRole().name())
                .empresaId(user.getEmpresa() != null ? user.getEmpresa().getId() : null)
                .empresaNome(user.getEmpresa() != null ? user.getEmpresa().getNome() : null)
                .ativo(user.getAtivo())
                .telegramChatId(user.getTelegramChatId())
                .pushInscrito(pushInscrito)
                .build();
    }
}
