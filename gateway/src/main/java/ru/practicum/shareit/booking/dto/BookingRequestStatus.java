package ru.practicum.shareit.booking.dto;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.error.exceptions.BadRequestException;

import java.util.Objects;

@Slf4j
public enum BookingRequestStatus {
    ALL,
    CURRENT,
    FUTURE,
    PAST,
    WAITING,
    REJECTED;

    public static BookingRequestStatus getValue(String state) throws BadRequestException {
        if (Objects.nonNull(state)) {
            switch (state.toLowerCase()) {
                case ("all"):
                    return BookingRequestStatus.ALL;
                case ("current"):
                    return BookingRequestStatus.CURRENT;
                case ("future"):
                    return BookingRequestStatus.FUTURE;
                case ("past"):
                    return BookingRequestStatus.PAST;
                case ("rejected"):
                    return BookingRequestStatus.REJECTED;
                case ("waiting"):
                    return BookingRequestStatus.WAITING;
                default:
                    break;
            }
        } else {
            return BookingRequestStatus.ALL;
        }
        String msg = String.format("Unknown state: %s", state);
        log.info("Зарос {} недопустим", state);
        throw new BadRequestException(msg);
    }
}
