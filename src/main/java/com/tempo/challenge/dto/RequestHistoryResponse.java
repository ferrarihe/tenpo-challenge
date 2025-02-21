package com.tempo.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestHistoryResponse {
    private Instant timestamp;
    private String endpoint;
    private String parameters;
    private String response;
    private String error;
}
