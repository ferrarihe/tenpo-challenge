package com.tempo.challenge.service;

import com.tempo.challenge.dto.RequestHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface IRequestHistoryService {
     Mono<Page<RequestHistoryResponse>> getRequestHistory(Pageable pageable);
}
