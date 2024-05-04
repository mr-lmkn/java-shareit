package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import ru.practicum.shareit.booking.dto.validation.BookingDatesValidator;
import ru.practicum.shareit.booking.enums.BookingStatus;

import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BookingDatesValidator
public class BookingRequestDto {
    @Nullable
    private Long id;
    @FutureOrPresent(message = "Значение поля 'start' не может быть в прошлом")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime start;
    @FutureOrPresent(message = "Значение поля 'end' не может быть в прошлом")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime end;
    private Long itemId;
    private Long booker;
    @Nullable
    private BookingStatus status;
}
