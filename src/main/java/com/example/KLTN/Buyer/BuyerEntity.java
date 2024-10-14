package com.example.KLTN.Buyer;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Enum.BuyerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "Buyer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuyerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "User", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID buyerId;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BuyerType buyerType;

    @Column(name = "full_name")
    private String fullName;

    @Column
    private String address;

    @Column
    private String phone;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "personal_id")
    private String personalId;
}
