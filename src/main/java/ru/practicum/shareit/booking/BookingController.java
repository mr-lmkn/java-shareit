package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final ModelMapper modelMapper;
    private static final String OWNER_ID_HOLDER = "X-Sharer-User-Id";

    @PostMapping
    public BookingResponseDto add(@RequestHeader(OWNER_ID_HOLDER) long userId,
                                  @Valid @RequestBody BookingRequestDto bookingRequestDto)
            throws NoContentException, BadRequestException {
        log.info("Got booking add request {}", bookingRequestDto);
        return modelMapper.map(bookingService.add(userId, bookingRequestDto), BookingResponseDto.class);
    }

    @PatchMapping(path = "/{bookingId}")
    public BookingResponseDto setAppruve(@RequestHeader(OWNER_ID_HOLDER) long userId,
                                         @PathVariable long bookingId,
                                         @RequestParam(value = "approved") String approved)
            throws NoContentException, BadRequestException {
        log.info("Got booking approve update request userId = {}, bookingId = {}, approved = {}",
                userId, bookingId, approved);
        return modelMapper.map(bookingService
                .setState(userId, bookingId, approved), BookingResponseDto.class);
    }


    @GetMapping(path = "{bookingId}")
    public BookingResponseDto getFromBookerOrOwner(@RequestHeader(OWNER_ID_HOLDER) long userId,
                                                   @PathVariable long bookingId)
            throws NoContentException, BadRequestException {
        log.info("Got all user booking request from userId = {}, bookingId = {}", userId, bookingId);
        return modelMapper.map(bookingService.getFromBookerOrOwner(userId, bookingId), BookingResponseDto.class);
    }

    @GetMapping()
    public List<BookingResponseDto> getFromUserByRequest(@RequestHeader(OWNER_ID_HOLDER) long userId,
                                                         @RequestParam(value = "state", required = false) String state)
            throws NoContentException, BadRequestException {
        log.info("Got booking by state request from userId = {}, state = {}", userId, state);
        return bookingService.getFromUserByRequest(userId, state, false)
                .stream()
                .map(Booking -> modelMapper.map(Booking, BookingResponseDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/owner")
    public List<BookingResponseDto> getFromOwnerByReqest(@RequestHeader(OWNER_ID_HOLDER) long userId,
                                                         @RequestParam(value = "state", required = false) String state)
            throws NoContentException, BadRequestException {
        log.info("Got booking by state request from owner id = {}, state = {}", userId, state);
        return bookingService.getFromUserByRequest(userId, state, true)
                .stream()
                .map(Booking -> modelMapper.map(Booking, BookingResponseDto.class))
                .collect(Collectors.toList());
    }

}
