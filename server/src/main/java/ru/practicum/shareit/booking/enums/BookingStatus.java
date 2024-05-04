package ru.practicum.shareit.booking.enums;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum BookingStatus {
    WAITING,
    APPROVED,
    REJECTED,
    CANCELED;
}
