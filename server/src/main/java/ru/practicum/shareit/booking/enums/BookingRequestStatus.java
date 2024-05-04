package ru.practicum.shareit.booking.enums;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;

import java.util.Objects;

@Slf4j
public enum BookingRequestStatus {
    ALL,
    CURRENT,
    FUTURE,
    PAST,
    WAITING,
    REJECTED;

    public static BookingRequestStatus getValue(String state) throws BadRequestException, NoContentException {
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
            }
        } else {
            return BookingRequestStatus.ALL; //state = "null";
        }
        String msg = String.format("Unknown state: %s", state);
        log.info("Зарос {} недопустим", state);
        throw new BadRequestException(msg); //--400 не подходит для Booking get all for wrong user 100
        //throw new NoContentException(msg);
    }
}
