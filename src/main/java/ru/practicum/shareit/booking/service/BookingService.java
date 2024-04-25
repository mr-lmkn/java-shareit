package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingService {

    Booking getById(Long bookingId) throws NoContentException;

    BookingResponseDto getDtoById(Long bookingId) throws NoContentException;

    BookingResponseDto add(Long userId, BookingRequestDto booking) throws NoContentException, BadRequestException;

    BookingResponseDto setState(Long userId, Long bookingId, String approved)
            throws BadRequestException, NoContentException;

    BookingResponseDto getFromBookerOrOwner(long userId, long bookingId) throws NoContentException;

    List<BookingResponseDto> getFromUserByRequest(long userId, String state, Boolean ownerOnly, Optional<Integer> from, Optional<Integer> size)
            throws BadRequestException, NoContentException;

    boolean isBookingAvailable(Long itemId, LocalDateTime start, LocalDateTime end);

    BookingStatus getStateByUser(Booking booking, Long userId, String state) throws BadRequestException, NoContentException;

}
