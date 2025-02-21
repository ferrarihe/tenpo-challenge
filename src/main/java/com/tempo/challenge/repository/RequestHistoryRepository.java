package com.tempo.challenge.repository;

import com.tempo.challenge.entities.RequestHistory;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RequestHistoryRepository extends ReactiveCrudRepository<RequestHistory, Long> {

    @Query("SELECT * FROM request_history ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    Flux<RequestHistory> findAllPaginated(int limit, int offset);

    @Query("SELECT COUNT(*) FROM request_history")
    Mono<Long> count();

}