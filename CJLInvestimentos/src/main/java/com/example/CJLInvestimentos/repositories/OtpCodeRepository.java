package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findByUserIdAndCodeAndUsedFalse(Long userId, String code);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM OtpCode o WHERE o.userId = :userId AND o.used = false")
    void deleteByUserIdAndUsedFalse(Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now OR o.used = true")
    int deleteExpiredOrUsed(LocalDateTime now);
}
