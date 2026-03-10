package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.ChatConversa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversaRepository extends JpaRepository<ChatConversa, Long> {

    List<ChatConversa> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<ChatConversa> findByStatusOrderByUpdatedAtDesc(String status);

    List<ChatConversa> findAllByOrderByUpdatedAtDesc();

    Optional<ChatConversa> findByUserIdAndStatus(Long userId, String status);
}
