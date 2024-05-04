package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingRequestStatus;
import ru.practicum.shareit.error.exceptions.BadRequestException;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    private static final String OWNER_ID_HOLDER = "X-Sharer-User-Id";


    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader(OWNER_ID_HOLDER) long userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @RequestParam(name = "from", required = false)
                                              Optional<Integer> from,
                                              @RequestParam(name = "size", required = false)
                                              Optional<Integer> size) throws BadRequestException {
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        try {
            BookingRequestStatus state = BookingRequestStatus.getValue(stateParam);
            return bookingClient.getBookings(userId, state, from, size, false);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader(OWNER_ID_HOLDER) long userId,
                                                   @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                   @RequestParam(name = "from", required = false)
                                                   Optional<Integer> from,
                                                   @RequestParam(name = "size", required = false)
                                                   Optional<Integer> size) throws BadRequestException {
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        try {
            BookingRequestStatus state = BookingRequestStatus.getValue(stateParam);
            return bookingClient.getBookings(userId, state, from, size, true);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(OWNER_ID_HOLDER) long userId,
                                      @RequestBody @Valid BookItemRequestDto requestDto) {
        log.info("Creating booking {}, userId={}", requestDto, userId);
        return bookingClient.add(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getFromBookerOrOwner(@RequestHeader(OWNER_ID_HOLDER) long userId,
                                                       @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getFromBookerOrOwner(userId, bookingId);
    }

    @PatchMapping(path = "/{bookingId}")
    public ResponseEntity<Object> setState(@RequestHeader(OWNER_ID_HOLDER) long userId,
                                           @PathVariable long bookingId,
                                           @RequestParam(value = "approved") String approved) {
        log.info("Got booking approve update request userId = {}, bookingId = {}, approved = {}",
                userId, bookingId, approved);
        return bookingClient.setState(userId, bookingId, approved);
    }
/*
    @GetMapping()
    public ResponseEntity<Object> getFromUser(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "from", required = false)
            @Min(value = 0, message = "The value must be positive")
            Optional<Integer> from,
            @Positive
            @RequestParam(value = "size", required = false) Optional<Integer> size
    ) throws BadRequestException {
        log.info("Got booking by state request from userId = {}, state = {}, from = {}, size = {}",
                userId, state, from, size);
        BookingRequestStatus stateBRS = BookingRequestStatus.getValue(state);
        return bookingClient.getFromUser(userId, stateBRS, from, size);
    }

    @GetMapping(path = "/owner")
    public ResponseEntity<Object> getFromOwner(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "from", required = false)
            @Min(value = 0, message = "The value must be positive")
            Optional<Integer> from,
            @Positive
            @RequestParam(value = "size", required = false) Optional<Integer> size
    ) throws BadRequestException {
        log.info("Got booking by state request from owner id = {}, state = {}", userId, state);
        BookingRequestStatus stateBRS = BookingRequestStatus.getValue(state);
        return bookingClient.getFromOwner(userId, stateBRS, from, size);
    }
*/
}
