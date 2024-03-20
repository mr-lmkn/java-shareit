package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private Integer id;  // уникальный идентификатор бронирования;
    private LocalDate start; //дата и время начала бронирования;
    private LocalDate end; //дата и время конца бронирования;
    private Integer item; // вещь, которую пользователь бронирует;
    private Integer booker; // пользователь, который осуществляет бронирование;
    private BookingStatus status;
}
