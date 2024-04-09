package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingShortResponseDto {
    private Long id;
    private String start;
    private String end;
    private ItemResponseDto item;
    private UserResponseDto booker;
    private BookingStatus status;
    private Long bookerId;
}
