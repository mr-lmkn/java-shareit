package ru.practicum.shareit.booking.enums;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;

import java.util.Objects;

@Slf4j
public enum BookingStatus {
    WAITING,
    APPROVED,
    REJECTED,
    CANCELED;

    public static BookingStatus getValue(String state) throws BadRequestException, NoContentException {
        if (Objects.nonNull(state)) {
            switch (state.toLowerCase()) {
                case ("approved"):
                    return BookingStatus.APPROVED;
                case ("waiting"):
                    return BookingStatus.WAITING;
                case ("rejected"):
                    return BookingStatus.REJECTED;
                case ("canceled"):
                    return BookingStatus.CANCELED;
            }
        } else {
            state = "null";
        }
        String msg = String.format("Unknown state: %s", state);
        log.info("Статус {} недопустим", state);
        // throw new BadRequestException(msg); --400 не подходит для Booking get all for wrong user 100
        throw new NoContentException(msg);
    }
}
