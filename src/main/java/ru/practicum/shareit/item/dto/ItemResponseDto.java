package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortResponseDto;
import ru.practicum.shareit.item.comment.dto.ItemCommentResponseDto;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponseDto {
    private Long id;
    private Long owner;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private BookingShortResponseDto lastBooking;
    private BookingShortResponseDto nextBooking;
    private List<ItemCommentResponseDto> comments;
}
