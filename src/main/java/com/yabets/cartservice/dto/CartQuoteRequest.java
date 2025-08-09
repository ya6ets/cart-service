package com.yabets.cartservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartQuoteRequest {

    @NotEmpty
    @Valid
    private List<ItemDto> items;
    private String customerSegment;
}