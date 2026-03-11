package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.EmailApresentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmailApresentacaoRepository extends JpaRepository<EmailApresentacao, Long> {

    @Query("SELECT e FROM EmailApresentacao e JOIN FETCH e.user JOIN FETCH e.enviadoPor ORDER BY e.enviadoEm DESC")
    List<EmailApresentacao> findAllWithUsersOrderByEnviadoEmDesc();

    @Query("SELECT e FROM EmailApresentacao e JOIN FETCH e.user JOIN FETCH e.enviadoPor WHERE e.enviadoPor.id = :enviadoPorId ORDER BY e.enviadoEm DESC")
    List<EmailApresentacao> findByEnviadoPorIdWithUsers(Long enviadoPorId);
}
