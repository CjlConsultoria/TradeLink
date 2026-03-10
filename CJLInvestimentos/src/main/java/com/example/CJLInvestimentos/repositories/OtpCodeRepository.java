package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findByUserIdAndCodeAndUsedFalse(Long userId, String code);

    void deleteByUserIdAndUsedFalse(Long userId);
}
