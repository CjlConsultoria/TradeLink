package com.example.CJLInvestimentos.repositories;
import com.example.CJLInvestimentos.entities.Plano;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PlanoRepository extends JpaRepository<Plano, Long> {
    Optional<Plano> findByNome(String nome);
    List<Plano> findByAtivoTrue();
}