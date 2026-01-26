package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.AcaoFinanceira;
import com.example.CJLInvestimentos.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcaoFinanceiraRepository extends JpaRepository<AcaoFinanceira, Long> {

    List<AcaoFinanceira> findByUser(User user);
}
