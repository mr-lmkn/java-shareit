package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Integer id;  // уникальный идентификатор бронирования;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate start; //дата и время начала бронирования;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate end; //дата и время конца бронирования;
    private Integer item; // вещь, которую пользователь бронирует;
    private Integer booker; // пользователь, который осуществляет бронирование;
    private BookingStatus status;
}
