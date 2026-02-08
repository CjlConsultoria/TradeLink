package com.example.CJLInvestimentos.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_push_subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "endpoint"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1024)
    private String endpoint;

    @Column(name = "p256dh_key", nullable = false, length = 500)
    private String p256dhKey;

    @Column(name = "auth_key", nullable = false, length = 500)
    private String authKey;
}
