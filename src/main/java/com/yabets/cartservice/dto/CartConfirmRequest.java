package com.yabets.cartservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CartConfirmRequest {
    @NotEmpty
    @Valid
    private List<ItemDto> items;
    private String customerSegment;
}