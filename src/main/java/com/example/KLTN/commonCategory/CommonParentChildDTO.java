package com.example.KLTN.commonCategory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommonParentChildDTO {
    private UUID id;
    private String name;
    private String description;
    private UUID parentId;

}
