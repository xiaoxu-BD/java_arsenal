package org.xiaoxu.web_boot.entity.practice;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class Order {
    @NotBlank
    private String orderId;
    private User user;
    private List<Item> items;
}
