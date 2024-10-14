package com.example.KLTN.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID userId;
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String roles;
    private boolean status;
    private boolean isDelete;
}
