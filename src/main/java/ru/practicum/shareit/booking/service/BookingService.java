package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking getById(Long bookingId) throws NoContentException, BadRequestException;

    Booking add(Long userId, BookingRequestDto booking) throws NoContentException, BadRequestException;

    Booking setState(Long userId, Long bookingId, String approved)
            throws BadRequestException, NoContentException;

    Booking getFromBookerOrOwner(long userId, long bookingId) throws BadRequestException, NoContentException;

    List<Booking> getFromUserByRequest(long userId, String state, Boolean ownerOnly, Optional<Integer> from, Optional<Integer> size)
            throws BadRequestException, NoContentException;

    // List<Booking> getAllBookingsByItemIdOrderByEndAsc(Item item, BookingStatus bs);

    boolean isBookingAvailable(Long itemId, LocalDateTime start, LocalDateTime end);

    BookingStatus getStateByUser(Booking booking, Long userId, String state) throws BadRequestException, NoContentException;
}
