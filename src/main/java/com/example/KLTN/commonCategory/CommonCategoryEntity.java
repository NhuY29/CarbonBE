package com.example.KLTN.commonCategory;

import com.example.KLTN.Enum.Category;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "common_category")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommonCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "unit", nullable = true)
    private String unit;

    @Column(name = "conversion_price", nullable = true)
    private String conversionPrice;
}
