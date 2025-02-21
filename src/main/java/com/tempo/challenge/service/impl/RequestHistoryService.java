package com.tempo.challenge.service.impl;

import com.tempo.challenge.dto.RequestHistoryResponse;
import com.tempo.challenge.repository.RequestHistoryRepository;
import com.tempo.challenge.service.IRequestHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RequestHistoryService implements IRequestHistoryService {

    private final RequestHistoryRepository requestHistoryRepository;

    public RequestHistoryService(RequestHistoryRepository requestHistoryRepository) {
        this.requestHistoryRepository = requestHistoryRepository;
    }

    @Override
    public Mono<Page<RequestHistoryResponse>> getRequestHistory(Pageable pageable) {
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();

        return requestHistoryRepository.findAllPaginated(limit, offset)
                .map(history -> new RequestHistoryResponse(
                        history.getTimestamp(),
                        history.getEndpoint(),
                        history.getParameters(),
                        history.getResponse(),
                        history.getError())
                )
                .collectList()
                .zipWith(requestHistoryRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }


}
