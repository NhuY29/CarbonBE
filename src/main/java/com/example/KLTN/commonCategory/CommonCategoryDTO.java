package com.example.KLTN.commonCategory;

import com.example.KLTN.Enum.Category;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommonCategoryDTO {
    private UUID id;
    private String code;
    private Category category;
    private String name;
    private String description;
    private String unit;
    private String conversionPrice;
}
