package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.EmailMarketing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmailMarketingRepository extends JpaRepository<EmailMarketing, Long> {

    @Query("SELECT e FROM EmailMarketing e LEFT JOIN FETCH e.enviadoPor ORDER BY e.enviadoEm DESC")
    List<EmailMarketing> findAllWithEnviadoPorOrderByEnviadoEmDesc();

    List<EmailMarketing> findByCampanhaOrderByEnviadoEmDesc(String campanha);

    long countByStatusAndCampanha(String status, String campanha);

    long countByStatus(String status);
}
