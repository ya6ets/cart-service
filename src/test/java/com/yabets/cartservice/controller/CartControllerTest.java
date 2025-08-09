package com.yabets.cartservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yabets.cartservice.dto.CartQuoteRequest;
import com.yabets.cartservice.dto.ItemDto;
import com.yabets.cartservice.dto.QuoteResponse;
import com.yabets.cartservice.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @Test
    void getCartQuote_shouldReturnQuoteResponse() throws Exception {

        UUID productId = UUID.randomUUID();
        CartQuoteRequest request = new CartQuoteRequest();
        request.setItems(List.of(new ItemDto(productId, 2)));

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
}