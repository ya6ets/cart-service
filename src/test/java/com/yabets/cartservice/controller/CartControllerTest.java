package com.yabets.cartservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yabets.cartservice.dto.CartConfirmRequest;
import com.yabets.cartservice.dto.CartQuoteRequest;
import com.yabets.cartservice.dto.ItemDto;
import com.yabets.cartservice.dto.QuoteResponse;
import com.yabets.cartservice.exception.ResourceNotFoundException;
import com.yabets.cartservice.exception.StockConflictException;
import com.yabets.cartservice.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
public class CartControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private final UUID productId = UUID.randomUUID();
    private final ItemDto itemDto = new ItemDto(productId, 2);

    @Test
    void getCartQuote_shouldReturnQuoteResponse_onSuccess() throws Exception {
        CartQuoteRequest request = new CartQuoteRequest();
        request.setItems(List.of(itemDto));
        QuoteResponse response = QuoteResponse.builder()
                .totalAmount(new BigDecimal("100.00"))
                .itemizedPrices(Map.of(productId.toString(), new BigDecimal("50.00")))
                .appliedPromotions(List.of("10% Off Electronics"))
                .build();

        when(cartService.getCartQuote(any(CartQuoteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/cart/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(100.00))
                .andExpect(jsonPath("$.appliedPromotions[0]").value("10% Off Electronics"));
    }

    @Test
    void confirmCart_shouldReturnOrderId_onSuccess() throws Exception {
        CartConfirmRequest request = new CartConfirmRequest();
        request.setItems(List.of(itemDto));
        String orderId = UUID.randomUUID().toString();

        when(cartService.confirmCart(any(CartConfirmRequest.class), any())).thenReturn(orderId);

        mockMvc.perform(post("/cart/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(orderId));
    }

    @Test
    void confirmCart_shouldReturnOrderId_withIdempotencyKey() throws Exception {
        CartConfirmRequest request = new CartConfirmRequest();
        request.setItems(List.of(itemDto));
        String orderId = UUID.randomUUID().toString();
        String idempotencyKey = UUID.randomUUID().toString();

        when(cartService.confirmCart(any(CartConfirmRequest.class), any())).thenReturn(orderId);

        mockMvc.perform(post("/cart/confirm")
                        .header(HttpHeaders.IF_MATCH, idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(orderId));
    }

    @Test
    void getCartQuote_shouldReturnBadRequest_onValidationFailure() throws Exception {
        CartQuoteRequest request = new CartQuoteRequest(); // items list is null

        mockMvc.perform(post("/cart/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmCart_shouldReturnNotFound_onResourceNotFoundException() throws Exception {
        CartConfirmRequest request = new CartConfirmRequest();
        request.setItems(List.of(itemDto));

        when(cartService.confirmCart(any(CartConfirmRequest.class), any()))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(post("/cart/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirmCart_shouldReturnConflict_onStockConflictException() throws Exception {
        CartConfirmRequest request = new CartConfirmRequest();
        request.setItems(List.of(itemDto));

        when(cartService.confirmCart(any(CartConfirmRequest.class), any()))
                .thenThrow(new StockConflictException("Insufficient stock"));

        mockMvc.perform(post("/cart/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}