package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.request.AlertaPrecoRequest;
import com.example.CJLInvestimentos.dtos.response.AlertaPrecoResponse;
import com.example.CJLInvestimentos.entities.AlertaPreco;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.TipoAlerta;
import com.example.CJLInvestimentos.repositories.AlertaPrecoRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertaPrecoService {

    private final AlertaPrecoRepository alertaPrecoRepository;
    private final UserRepository userRepository;

    private static final int MAX_ALERTAS_POR_USUARIO = 20;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public List<AlertaPrecoResponse> listarPorUsuario(Long userId) {
        return alertaPrecoRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AlertaPrecoResponse criar(Long userId, AlertaPrecoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        long count = alertaPrecoRepository.countByUserIdAndAtivoTrue(userId);
        if (count >= MAX_ALERTAS_POR_USUARIO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Limite de " + MAX_ALERTAS_POR_USUARIO + " alertas ativos atingido. Desative um alerta antes de criar outro.");
        }

        TipoAlerta tipo;
        try {
            tipo = TipoAlerta.valueOf(request.getTipoAlerta().toUpperCase());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de alerta invalido. Use ACIMA ou ABAIXO.");
        }

        AlertaPreco alerta = AlertaPreco.builder()
                .user(user)
                .moeda(request.getMoeda().toUpperCase())
                .parMoeda(request.getParMoeda().toUpperCase())
                .tipoAlerta(tipo)
                .precoAlerta(request.getPrecoAlerta())
                .observacao(request.getObservacao())
                .build();

        return toResponse(alertaPrecoRepository.save(alerta));
    }

    public AlertaPrecoResponse atualizar(Long userId, Long alertaId, AlertaPrecoRequest request) {
        AlertaPreco alerta = alertaPrecoRepository.findById(alertaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta nao encontrado"));

        if (!alerta.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Voce nao pode editar este alerta");
        }

        TipoAlerta tipo;
        try {
            tipo = TipoAlerta.valueOf(request.getTipoAlerta().toUpperCase());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de alerta invalido. Use ACIMA ou ABAIXO.");
        }

        alerta.setMoeda(request.getMoeda().toUpperCase());
        alerta.setParMoeda(request.getParMoeda().toUpperCase());
        alerta.setTipoAlerta(tipo);
        alerta.setPrecoAlerta(request.getPrecoAlerta());
        alerta.setObservacao(request.getObservacao());
        // Ao editar, resetar o disparo para poder alertar novamente
        alerta.setDisparado(false);
        alerta.setDataDisparo(null);
        alerta.setAtivo(true);

        return toResponse(alertaPrecoRepository.save(alerta));
    }

    public void alternarAtivo(Long userId, Long alertaId) {
        AlertaPreco alerta = alertaPrecoRepository.findById(alertaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta nao encontrado"));

        if (!alerta.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Voce nao pode alterar este alerta");
        }

        alerta.setAtivo(!alerta.getAtivo());
        if (Boolean.TRUE.equals(alerta.getAtivo())) {
            alerta.setDisparado(false);
            alerta.setDataDisparo(null);
        }
        alertaPrecoRepository.save(alerta);
    }

    public void excluir(Long userId, Long alertaId) {
        AlertaPreco alerta = alertaPrecoRepository.findById(alertaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta nao encontrado"));

        if (!alerta.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Voce nao pode excluir este alerta");
        }

        alertaPrecoRepository.delete(alerta);
    }

    private AlertaPrecoResponse toResponse(AlertaPreco alerta) {
        return AlertaPrecoResponse.builder()
                .id(alerta.getId())
                .moeda(alerta.getMoeda())
                .parMoeda(alerta.getParMoeda())
                .tipoAlerta(alerta.getTipoAlerta().name())
                .precoAlerta(alerta.getPrecoAlerta())
                .ativo(Boolean.TRUE.equals(alerta.getAtivo()))
                .disparado(Boolean.TRUE.equals(alerta.getDisparado()))
                .observacao(alerta.getObservacao())
                .dataDisparo(alerta.getDataDisparo() != null ? alerta.getDataDisparo().format(DTF) : null)
                .createdAt(alerta.getCreatedAt() != null ? alerta.getCreatedAt().format(DTF) : null)
                .build();
    }
}
