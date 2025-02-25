package com.tempo.challenge.service;

import com.tempo.challenge.dto.RequestHistoryResponse;
import com.tempo.challenge.entities.RequestHistory;
import com.tempo.challenge.repository.RequestHistoryRepository;
import com.tempo.challenge.service.impl.RequestHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestHistoryServiceTest {

    @Mock
    private RequestHistoryRepository requestHistoryRepository;

    @InjectMocks
    private RequestHistoryService requestHistoryService;

    // Test 1: Verificar que la consulta paginada devuelve los resultados esperados
    @Test
    void testGetRequestHistoryReturnsPaginatedResults() {
        int page = 0;
        int size = 5;
        PageRequest pageable = PageRequest.of(page, size);

        List<RequestHistory> mockHistoryList = List.of(
                new RequestHistory(1L, Instant.now(), "/api/test", "param=value", "response", null),
                new RequestHistory(2L, Instant.now(), "/api/test2", "param2=value2", "response2", null)
        );

        when(requestHistoryRepository.findAllPaginated(anyInt(), anyInt()))
                .thenReturn(Flux.fromIterable(mockHistoryList));
        when(requestHistoryRepository.count()).thenReturn(Mono.just(2L));

        Mono<Page<RequestHistoryResponse>> result = requestHistoryService.getRequestHistory(pageable);

        StepVerifier.create(result)
                .expectNextMatches(pageResponse ->
                        pageResponse.getContent().size() == 2 &&
                                pageResponse.getTotalElements() == 2)
                .verifyComplete();

        verify(requestHistoryRepository).findAllPaginated(size, page * size);
        verify(requestHistoryRepository).count();
    }

    // Test 2: Verificar que cuando la base de datos está vacía, devuelve una página vacía
    @Test
    void testGetRequestHistoryReturnsEmptyPageWhenNoData() {
        int page = 0;
        int size = 5;
        PageRequest pageable = PageRequest.of(page, size);

        when(requestHistoryRepository.findAllPaginated(anyInt(), anyInt()))
                .thenReturn(Flux.empty());
        when(requestHistoryRepository.count()).thenReturn(Mono.just(0L));

        Mono<Page<RequestHistoryResponse>> result = requestHistoryService.getRequestHistory(pageable);

        StepVerifier.create(result)
                .expectNextMatches(pageResponse ->
                        pageResponse.getContent().isEmpty() &&
                                pageResponse.getTotalElements() == 0)
                .verifyComplete();

        verify(requestHistoryRepository).findAllPaginated(size, page * size);
        verify(requestHistoryRepository).count();
    }

    // Test 3: Verificar que la paginación funcione con múltiples páginas
    @Test
    void testGetRequestHistorySupportsMultiplePages() {
        int page = 1;
        int size = 2;
        PageRequest pageable = PageRequest.of(page, size);

        List<RequestHistory> mockHistoryList = List.of(
                new RequestHistory(3L, Instant.now(), "/api/test3", "param3=value3", "response3", null),
                new RequestHistory(4L, Instant.now(), "/api/test4", "param4=value4", "response4", null)
        );

        when(requestHistoryRepository.findAllPaginated(anyInt(), anyInt()))
                .thenReturn(Flux.fromIterable(mockHistoryList));
        when(requestHistoryRepository.count()).thenReturn(Mono.just(4L));

        Mono<Page<RequestHistoryResponse>> result = requestHistoryService.getRequestHistory(pageable);

        StepVerifier.create(result)
                .expectNextMatches(pageResponse ->
                        pageResponse.getContent().size() == 2 &&
                                pageResponse.getTotalElements() == 4 &&
                                pageResponse.getTotalPages() == 2)
                .verifyComplete();

        verify(requestHistoryRepository).findAllPaginated(size, page * size);
        verify(requestHistoryRepository).count();
    }

    // Test 4: Verificar que si `count()` falla, se propaga el error
    @Test
    void testGetRequestHistoryFailsWhenCountFails() {
        int page = 0;
        int size = 5;
        PageRequest pageable = PageRequest.of(page, size);

        when(requestHistoryRepository.findAllPaginated(anyInt(), anyInt()))
                .thenReturn(Flux.just(new RequestHistory(1L, Instant.now(), "/api/test", "param=value", "response", null)));
        when(requestHistoryRepository.count()).thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<Page<RequestHistoryResponse>> result = requestHistoryService.getRequestHistory(pageable);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Database error"))
                .verify();

        verify(requestHistoryRepository).findAllPaginated(size, page * size);
        verify(requestHistoryRepository).count();
    }
}
