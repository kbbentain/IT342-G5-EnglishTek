package com.nekobyte.englishtek.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_badges")
public class UserBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "badge_id")
    private Badge badge;

    @Column(name = "date_obtained", nullable = false)
    private LocalDateTime dateObtained;

    @PrePersist
    protected void onCreate() {
        dateObtained = LocalDateTime.now();
    }
}
