package com.example.KLTN.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailDTO {
    private String to;
    private String subject;
    private String content;
    private Map<String,Object> props;
}
