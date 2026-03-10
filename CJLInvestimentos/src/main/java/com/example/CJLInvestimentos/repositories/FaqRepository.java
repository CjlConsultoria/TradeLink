package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByAtivoTrueOrderByOrdemAsc();

    List<Faq> findAllByOrderByOrdemAsc();
}
