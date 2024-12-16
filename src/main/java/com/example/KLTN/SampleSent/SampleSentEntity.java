package com.example.KLTN.SampleSent;

import com.example.KLTN.Enum.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sample_sent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SampleSentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Lob
    @Column(name = "pdf_file", columnDefinition = "LONGBLOB")
    private byte[] pdfFile;

    @Column(name = "project_id", columnDefinition = "BINARY(16)")
    private UUID projectId;
    @Column(name = "pdf_file_received", columnDefinition = "LONGBLOB")
    private byte[]  pdfFileReceived;
    @Column(name = "send_date")
    private LocalDateTime sendDate;
    @Column(name = "quantity")
    private Float quantity;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
    @Column(name = "reason")
    private String reason;
}
