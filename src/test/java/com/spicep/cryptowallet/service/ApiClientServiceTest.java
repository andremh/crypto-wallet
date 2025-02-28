package com.spicep.cryptowallet.service;

import com.spicep.cryptowallet.dto.coincap.CoinCapAssetDto;
import com.spicep.cryptowallet.dto.coincap.CoinCapAssetHistoryDto;
import com.spicep.cryptowallet.dto.coincap.CoinCapAssetHistoryResponseDto;
import com.spicep.cryptowallet.dto.coincap.CoinCapAssetResponseDto;
import com.spicep.cryptowallet.exception.asset.AssetNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class that tests the external api client
 */
class ApiClientServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private ApiClientService apiClientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }


    @Test
    void shouldGetAssetBySymbol() {
        CoinCapAssetDto assetDto = new CoinCapAssetDto();
        assetDto.setPriceUsd(BigDecimal.valueOf(100));

        CoinCapAssetResponseDto responseDto = new CoinCapAssetResponseDto();
        responseDto.setData(assetDto);
        when(responseSpec.bodyToMono(CoinCapAssetResponseDto.class)).thenReturn(Mono.just(responseDto));

        CoinCapAssetDto result = apiClientService.getCurrentAssetDataBySymbol("bitcoin");
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100), result.getPriceUsd());
    }

    @Test
    void shouldThrowAssetNotFoundExceptionForNonexistentSymbol() {
        when(responseSpec.bodyToMono(CoinCapAssetResponseDto.class)).thenReturn(Mono.empty());

        assertThrows(AssetNotFoundException.class, () -> apiClientService.getCurrentAssetDataBySymbol("AMHCOIN"));
    }


    @Test
    void shouldGetAssetPrice() {
        CoinCapAssetDto assetDto = new CoinCapAssetDto();
        assetDto.setPriceUsd(BigDecimal.valueOf(100));
        CoinCapAssetResponseDto responseDto = new CoinCapAssetResponseDto();
        responseDto.setData(assetDto);

        when(responseSpec.bodyToMono(CoinCapAssetResponseDto.class)).thenReturn(Mono.just(responseDto));

        BigDecimal price = apiClientService.getAssetPrice("bitcoin");

        assertEquals(BigDecimal.valueOf(100), price);
    }

    @Test
    void shouldGetAssetPriceForDateReturnPriceHistory() {
        CoinCapAssetHistoryDto historyDto = new CoinCapAssetHistoryDto();
        historyDto.setPriceUsd(BigDecimal.TEN);
        historyDto.setTime(System.currentTimeMillis());

        CoinCapAssetHistoryResponseDto responseDto = new CoinCapAssetHistoryResponseDto();
        responseDto.setData(Collections.singletonList(historyDto));

        LocalDate testDate = LocalDate.now();
        String symbol = "bitcoin";
        String interval = "d1";

        //webclient flow
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CoinCapAssetHistoryResponseDto.class)).thenReturn(Mono.just(responseDto));

        List<CoinCapAssetHistoryDto> history = apiClientService.getAssetPriceForDate(symbol, interval, testDate);

        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertEquals(1, history.size());
        assertEquals(BigDecimal.TEN, history.get(0).getPriceUsd());
        assertEquals(historyDto.getTime(), history.get(0).getTime());
    }

    @Test
    void shouldHandleExceptionWhenGettingAssetPriceForDate() {
        when(responseSpec.bodyToMono(CoinCapAssetHistoryResponseDto.class)).thenThrow(WebClientResponseException.class);

        List<CoinCapAssetHistoryDto> history = apiClientService.getAssetPriceForDate("bitcoin", "d1", LocalDate.now());

        assertNotNull(history);
        assertTrue(history.isEmpty());
    }


    @Test
    void shouldHandleExceptionWhenGettingAssetPriceForNonexistentCoin() {
        when(responseSpec.bodyToMono(CoinCapAssetHistoryResponseDto.class)).thenThrow(WebClientResponseException.class);

        List<CoinCapAssetHistoryDto> history = apiClientService.getAssetPriceForDate("MYCOIN", "d1", LocalDate.now());

        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldGetAssetPriceForDateReturnEmptyListWhenExceptionThrownForFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(30);

        when(responseSpec.bodyToMono(CoinCapAssetHistoryResponseDto.class))
                .thenReturn(Mono.error(new WebClientResponseException(400, "Bad Request", null, null, null)));

        List<CoinCapAssetHistoryDto> history = apiClientService.getAssetPriceForDate("bitcoin", "d1", futureDate);

        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

}