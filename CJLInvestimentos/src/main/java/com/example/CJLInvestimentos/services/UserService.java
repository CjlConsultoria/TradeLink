package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.AtivarContaRequest;
import com.example.CJLInvestimentos.dtos.request.AtualizarUsuarioAdminRequest;
import com.example.CJLInvestimentos.dtos.request.PushSubscriptionRequest;
import com.example.CJLInvestimentos.dtos.request.RegisterRequest;
import com.example.CJLInvestimentos.dtos.response.AuthResponse;
import com.example.CJLInvestimentos.dtos.response.LicencaResponse;
import com.example.CJLInvestimentos.dtos.response.UserResponse;
import com.example.CJLInvestimentos.dtos.response.ValidarConviteResponse;
import com.example.CJLInvestimentos.utils.DocumentoUtil;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmpresaRepository empresaRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationAsyncRunner notificationAsyncRunner;
    private final JwtService jwtService;

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

        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.save(
                User.builder()
                        .nome(request.getNome().trim())
                        .email(email)
                        .senha(passwordEncoder.encode(request.getSenha()))
                        .role(role)
                        .empresa(empresa)
                        .telefone(request.getTelefone() != null && !request.getTelefone().isBlank() ? request.getTelefone().trim() : null)
                        .build()
        );
        notificationAsyncRunner.enviarEmailNovoUsuarioAsync(user.getEmail(), user.getNome(), user.getRole());
        notificationAsyncRunner.enviarEmailBoasVindasApresentacaoAsync(user.getId());
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

    /**
     * Consultor "exclui" um cliente: desvincula da empresa, mas mantém ativo.
     * O cliente perde o vínculo e vê a tela de pós-exclusão ao fazer login.
     */
    public void excluirClientePorConsultor(Long clienteId, User consultor) {
        User cliente = userRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        if (cliente.getRole() != Role.Cliente) {
            throw new BusinessException("Usuário não é um cliente");
        }
        if (consultor.getEmpresa() == null || cliente.getEmpresa() == null
                || !cliente.getEmpresa().getId().equals(consultor.getEmpresa().getId())) {
            throw new BusinessException("Cliente não pertence à sua empresa");
        }
        cliente.setEmpresa(null);
        cliente.setAtivo(true);
        cliente.setDataExclusao(LocalDateTime.now());
        userRepository.save(cliente);

        notificationAsyncRunner.enviarEmailExclusaoClienteAsync(
                cliente.getEmail(), cliente.getNome() != null ? cliente.getNome() : cliente.getEmail());
    }

    /**
     * Consultor vincula um cliente excluído (sem empresa) ao seu grupo.
     * Funciona tanto para o consultor original quanto para outro consultor.
     * Reseta o estado de auto-gestão e recoloca o cliente sob a empresa do consultor.
     */
    public UserResponse vincularClienteExistente(String email, User consultor) {
        String emailNorm = email.trim().toLowerCase();
        if (consultor.getEmpresa() == null) {
            throw new BusinessException("Consultor sem empresa vinculada.");
        }

        User cliente = userRepository.findByEmail(emailNorm)
                .orElseThrow(() -> new BusinessException("Nenhum cliente encontrado com esse e-mail."));

        if (cliente.getRole() != Role.Cliente) {
            throw new BusinessException("Esse usuário não é um cliente.");
        }

        // Já está em uma empresa?
        if (cliente.getEmpresa() != null) {
            if (cliente.getEmpresa().getId().equals(consultor.getEmpresa().getId())) {
                throw new BusinessException("Esse cliente já pertence ao seu grupo.");
            }
            throw new BusinessException("Esse cliente já pertence a outro consultor.");
        }

        // Verificar se está ativo (excluído = empresa null + ativo true)
        if (!Boolean.TRUE.equals(cliente.getAtivo())) {
            throw new BusinessException("Essa conta está inativa e não pode ser vinculada.");
        }

        // Verificar limite de licença
        Empresa empresa = consultor.getEmpresa();
        if (empresa.getPlano() != null) {
            long totalUsuarios = userRepository.countByEmpresaId(empresa.getId());
            if (totalUsuarios >= empresa.getPlano().getMaxUsuarios()) {
                throw new BusinessException("Limite de usuários do plano atingido (" + empresa.getPlano().getMaxUsuarios() + ").");
            }
        }

        // Vincular à empresa e resetar estado de auto-gestão
        cliente.setEmpresa(empresa);
        cliente.setAutoGestao(false);
        cliente.setSubscriptionStatus("NONE");
        cliente.setCurrentPeriodEnd(null);
        cliente.setDataExclusao(null);
        userRepository.save(cliente);

        // Notificar o cliente por email
        notificationAsyncRunner.enviarEmailVinculacaoClienteAsync(
                cliente.getEmail(),
                cliente.getNome() != null ? cliente.getNome() : cliente.getEmail(),
                empresa.getNome() != null ? empresa.getNome() : "seu novo consultor");

        return toResponse(cliente);
    }

    /** Consultor reativa um cliente da própria empresa. */
    public void ativarClientePorConsultor(Long clienteId, User consultor) {
        User cliente = userRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        if (cliente.getRole() != Role.Cliente) {
            throw new BusinessException("Usuário não é um cliente");
        }
        if (consultor.getEmpresa() == null || cliente.getEmpresa() == null
                || !cliente.getEmpresa().getId().equals(consultor.getEmpresa().getId())) {
            throw new BusinessException("Cliente não pertence à sua empresa");
        }
        cliente.setAtivo(true);
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
        if (request.getTelefone() != null) {
            user.setTelefone(request.getTelefone().isBlank() ? null : request.getTelefone().trim());
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

    // ─── Sistema de Convite ───

    /** Consultor convida um cliente apenas pelo email. Cria user pendente e envia convite. */
    public UserResponse convidarCliente(String email, Long empresaId) {
        String emailNorm = email.trim().toLowerCase();

        // Se já existe com convite pendente, permite reenvio
        Optional<User> existente = userRepository.findByEmail(emailNorm);
        if (existente.isPresent()) {
            User u = existente.get();
            if (u.getTokenConvite() != null && !Boolean.TRUE.equals(u.getAtivo())) {
                u.setTokenConvite(UUID.randomUUID().toString());
                u.setTokenConviteExpiracao(LocalDateTime.now().plusHours(48));
                userRepository.save(u);
                String empresaNome = u.getEmpresa() != null ? u.getEmpresa().getNome() : "";
                notificationAsyncRunner.enviarEmailConviteAsync(u.getEmail(), u.getTokenConvite(), empresaNome);
                return toResponse(u);
            }
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

        String token = UUID.randomUUID().toString();
        User user = userRepository.save(
                User.builder()
                        .email(emailNorm)
                        .role(Role.Cliente)
                        .empresa(empresa)
                        .ativo(false)
                        .tokenConvite(token)
                        .tokenConviteExpiracao(LocalDateTime.now().plusHours(48))
                        .build()
        );

        notificationAsyncRunner.enviarEmailConviteAsync(emailNorm, token, empresa.getNome());
        return toResponse(user);
    }

    /** Valida um token de convite (público). */
    public ValidarConviteResponse validarConvite(String token) {
        User user = userRepository.findByTokenConvite(token).orElse(null);
        if (user == null) {
            return ValidarConviteResponse.builder()
                    .valido(false)
                    .mensagem("Convite inválido ou já utilizado.")
                    .build();
        }
        if (Boolean.TRUE.equals(user.getAtivo())) {
            return ValidarConviteResponse.builder()
                    .valido(false)
                    .mensagem("Esta conta já foi ativada.")
                    .build();
        }
        if (user.getTokenConviteExpiracao() != null
                && LocalDateTime.now().isAfter(user.getTokenConviteExpiracao())) {
            return ValidarConviteResponse.builder()
                    .valido(false)
                    .mensagem("Convite expirado. Solicite um novo convite ao seu consultor.")
                    .build();
        }
        return ValidarConviteResponse.builder()
                .valido(true)
                .email(user.getEmail())
                .empresaNome(user.getEmpresa() != null ? user.getEmpresa().getNome() : null)
                .build();
    }

    /** Ativa a conta de um cliente convidado (público). Preenche dados e retorna JWT. */
    public AuthResponse ativarConta(AtivarContaRequest request) {
        if (!request.getSenha().equals(request.getConfirmarSenha())) {
            throw new BusinessException("As senhas não coincidem.");
        }

        User user = userRepository.findByTokenConvite(request.getToken())
                .orElseThrow(() -> new BusinessException("Convite inválido ou já utilizado."));

        if (Boolean.TRUE.equals(user.getAtivo())) {
            throw new BusinessException("Esta conta já foi ativada.");
        }
        if (user.getTokenConviteExpiracao() != null
                && LocalDateTime.now().isAfter(user.getTokenConviteExpiracao())) {
            throw new BusinessException("Convite expirado. Solicite um novo convite ao seu consultor.");
        }

        // CPF: validar e verificar unicidade
        String cpfDigits = DocumentoUtil.apenasDigitos(request.getCpf());
        if (!DocumentoUtil.isValidCpf(cpfDigits)) {
            throw new BusinessException("CPF inválido.");
        }
        userRepository.findByCpf(cpfDigits).ifPresent(outro -> {
            if (!outro.getId().equals(user.getId())) {
                throw new BusinessException("CPF já cadastrado para outro usuário.");
            }
        });

        // Preencher todos os dados
        user.setNome(request.getNome().trim());
        user.setCpf(cpfDigits);
        user.setWhatsapp(request.getWhatsapp().trim());
        user.setCep(DocumentoUtil.apenasDigitos(request.getCep()));
        user.setLogradouro(request.getLogradouro().trim());
        user.setNumero(request.getNumero().trim());
        user.setComplemento(request.getComplemento() != null ? request.getComplemento().trim() : null);
        user.setBairro(request.getBairro().trim());
        user.setCidade(request.getCidade().trim());
        user.setEstado(request.getEstado().trim().toUpperCase());
        user.setSenha(passwordEncoder.encode(request.getSenha()));
        user.setTermoAceito(true);
        user.setTermoAceitoEm(LocalDateTime.now());
        user.setAtivo(true);
        user.setTokenConvite(null);
        user.setTokenConviteExpiracao(null);

        userRepository.save(user);

        // Email de boas-vindas
        notificationAsyncRunner.enviarEmailNovoUsuarioAsync(user.getEmail(), user.getNome(), user.getRole());
        // Email de apresentação automático (boas-vindas com guia da plataforma)
        notificationAsyncRunner.enviarEmailBoasVindasApresentacaoAsync(user.getId());

        // Auto-login: gera JWT
        String jwt = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwt)
                .role(user.getRole().name())
                .userId(user.getId())
                .nome(user.getNome())
                .empresaId(user.getEmpresa() != null ? user.getEmpresa().getId() : null)
                .build();
    }

    /** Retorna informações de licença da empresa. */
    public LicencaResponse getLicenca(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        long totalUsuarios = userRepository.countByEmpresaId(empresaId);
        long totalClientes = userRepository.countByEmpresaIdAndRole(empresaId, Role.Cliente);
        Integer max = empresa.getPlano() != null ? empresa.getPlano().getMaxUsuarios() : null;
        return LicencaResponse.builder()
                .maxUsuarios(max)
                .totalUsuarios(totalUsuarios)
                .totalClientes(totalClientes)
                .podeConvidar(max == null || totalUsuarios < max)
                .planoNome(empresa.getPlano() != null ? empresa.getPlano().getNome() : null)
                .build();
    }

    private String deriveStatus(User user) {
        if (user.getTokenConvite() != null && !Boolean.TRUE.equals(user.getAtivo())) {
            return "PENDENTE";
        }
        return Boolean.TRUE.equals(user.getAtivo()) ? "ATIVO" : "INATIVO";
    }

    public UserResponse toResponse(User user) {
        boolean pushInscrito = pushSubscriptionRepository.findByUserId(user.getId()).stream().findAny().isPresent();
        boolean excluido = user.getRole() == Role.Cliente
                && user.getEmpresa() == null
                && Boolean.TRUE.equals(user.getAtivo());
        return UserResponse.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getRole().name())
                .empresaId(user.getEmpresa() != null ? user.getEmpresa().getId() : null)
                .empresaNome(user.getEmpresa() != null ? user.getEmpresa().getNome() : null)
                .ativo(user.getAtivo())
                .status(deriveStatus(user))
                .telefone(user.getTelefone())
                .cpf(user.getCpf())
                .whatsapp(user.getWhatsapp())
                .telegramChatId(user.getTelegramChatId())
                .pushInscrito(pushInscrito)
                .clienteExcluido(excluido)
                .autoGestao(user.getAutoGestao())
                .relatorioComplBaixado(user.getRelatorioComplBaixado())
                .dataExclusao(user.getDataExclusao())
                .build();
    }
}
