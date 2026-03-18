package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.MoedaFavorita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MoedaFavoritaRepository extends JpaRepository<MoedaFavorita, Long> {
    List<MoedaFavorita> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<MoedaFavorita> findByUserIdAndMoedaAndParMoeda(Long userId, String moeda, String parMoeda);
    boolean existsByUserIdAndMoedaAndParMoeda(Long userId, String moeda, String parMoeda);
    void deleteByUserIdAndMoedaAndParMoeda(Long userId, String moeda, String parMoeda);
    long countByUserId(Long userId);
}
