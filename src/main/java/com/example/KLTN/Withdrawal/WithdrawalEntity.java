package com.example.KLTN.Withdrawal;

import com.example.KLTN.Enum.Status;
import com.example.KLTN.Entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "withdrawal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "withdrawal", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID withdrawalId;


    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String bankAccountNumber;

    @Column(nullable = false)
    private String accountHolderName;

    private LocalDateTime requestTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
