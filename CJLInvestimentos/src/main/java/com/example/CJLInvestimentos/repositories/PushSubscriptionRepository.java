package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUserId(Long userId);
    void deleteByUserIdAndEndpoint(Long userId, String endpoint);
}
