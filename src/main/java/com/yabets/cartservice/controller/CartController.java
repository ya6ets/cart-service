package com.yabets.cartservice.controller;

import com.yabets.cartservice.dto.CartConfirmRequest;
import com.yabets.cartservice.dto.CartQuoteRequest;
import com.yabets.cartservice.dto.QuoteResponse;
import com.yabets.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/quote")
    @ResponseStatus(HttpStatus.OK)
    public QuoteResponse getCartQuote(@RequestBody @Valid CartQuoteRequest request) {

        return cartService.getCartQuote(request);
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    public String confirmCart(@RequestBody @Valid CartConfirmRequest request,
                              @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader) {

        return cartService.confirmCart(request, idempotencyKeyHeader);
    }
}