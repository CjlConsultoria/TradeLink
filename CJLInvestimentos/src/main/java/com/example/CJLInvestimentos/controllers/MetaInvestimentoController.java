package com.example.CJLInvestimentos.controllers;

import com.example.CJLInvestimentos.entities.MetaInvestimento;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.repositories.MetaInvestimentoRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/me/metas")
@RequiredArgsConstructor
public class MetaInvestimentoController {

    private final MetaInvestimentoRepository metaRepo;
    private final UserRepository userRepo;

    @GetMapping
    public ResponseEntity<List<MetaInvestimento>> listar(@AuthenticationPrincipal UserDetails ud) {
        User user = userRepo.findByEmail(ud.getUsername()).orElseThrow();
        return ResponseEntity.ok(metaRepo.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> criar(@AuthenticationPrincipal UserDetails ud, @RequestBody Map<String, Object> body) {
        User user = userRepo.findByEmail(ud.getUsername()).orElseThrow();
        if (metaRepo.countByUserIdAndConcluidaFalse(user.getId()) >= 10)
            return ResponseEntity.badRequest().body(Map.of("message", "Máximo de 10 metas ativas"));

        MetaInvestimento meta = MetaInvestimento.builder()
            .user(user)
            .titulo((String) body.get("titulo"))
            .descricao((String) body.get("descricao"))
            .valorAlvo(new BigDecimal(body.get("valorAlvo").toString()))
            .valorAtual(body.containsKey("valorAtual") ? new BigDecimal(body.get("valorAtual").toString()) : BigDecimal.ZERO)
            .dataLimite(body.containsKey("dataLimite") ? LocalDate.parse((String) body.get("dataLimite")) : null)
            .build();
        return ResponseEntity.ok(metaRepo.save(meta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = userRepo.findByEmail(ud.getUsername()).orElseThrow();
        MetaInvestimento meta = metaRepo.findById(id).orElseThrow();
        if (!meta.getUser().getId().equals(user.getId())) return ResponseEntity.status(403).build();

        if (body.containsKey("titulo")) meta.setTitulo((String) body.get("titulo"));
        if (body.containsKey("descricao")) meta.setDescricao((String) body.get("descricao"));
        if (body.containsKey("valorAlvo")) meta.setValorAlvo(new BigDecimal(body.get("valorAlvo").toString()));
        if (body.containsKey("valorAtual")) meta.setValorAtual(new BigDecimal(body.get("valorAtual").toString()));
        if (body.containsKey("dataLimite")) meta.setDataLimite(body.get("dataLimite") != null ? LocalDate.parse((String) body.get("dataLimite")) : null);
        if (body.containsKey("concluida")) meta.setConcluida((Boolean) body.get("concluida"));
        return ResponseEntity.ok(metaRepo.save(meta));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        User user = userRepo.findByEmail(ud.getUsername()).orElseThrow();
        MetaInvestimento meta = metaRepo.findById(id).orElseThrow();
        if (!meta.getUser().getId().equals(user.getId())) return ResponseEntity.status(403).build();
        metaRepo.delete(meta);
        return ResponseEntity.ok().build();
    }
}
