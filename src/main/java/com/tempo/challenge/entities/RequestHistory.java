package com.tempo.challenge.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "request_history")
public class RequestHistory {
    @Id
    private Long id;
    private Instant timestamp;
    private String endpoint;
    private String parameters;
    private String response;
    private String error;
}