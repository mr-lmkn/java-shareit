package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestItemResponseDto {
    private Long id;
    private String description;
    private Long requester;
    private LocalDateTime created;
    private List<Item> items;

    public List<Item> getItems() {
        if (Objects.isNull(items)) return new ArrayList<Item>();
        return items;
    }
}
