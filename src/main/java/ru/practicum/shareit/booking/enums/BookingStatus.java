package ru.practicum.shareit.booking.enums;

public enum BookingStatus {
    WAITING,  // новое бронирование, ожидает одобрения
    APPROVED,
    REJECTED, // бронирование отклонено владельцем
    CANCELED  // бронирование отменено создателем.
}
