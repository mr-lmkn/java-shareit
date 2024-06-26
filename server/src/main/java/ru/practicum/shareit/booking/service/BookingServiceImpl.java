package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingRequestStatus;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemService itemService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Booking getById(Long bookingId) throws NoContentException {
        if (bookingRepository.existsById(bookingId)) {
            return bookingRepository.findById(bookingId).get();
        }
        String msg = "Вещь недоступна";
        log.info(msg);
        throw new NoContentException(msg);
    }

    @Override
    @Transactional
    public BookingResponseDto getDtoById(Long bookingId) throws NoContentException {
        return modelMapper.map(getById(bookingId), BookingResponseDto.class);
    }

    @Override
    @Transactional
    public BookingResponseDto add(Long userId, BookingRequestDto bookingRequestDto)
            throws NoContentException, BadRequestException {
        Item item = itemService.getItemById(bookingRequestDto.getItemId(), userId);
        User booker = userService.getUserById(userId);
        Booking booking = modelMapper.map(bookingRequestDto, Booking.class);
        String msg;
        log.info("---> {}", booking.toString());
        if (!item.getAvailable()) {
            msg = "Вещь недоступна к бронированию";
            log.info(msg);
            throw new BadRequestException(msg);
        } else if (booker.equals(item.getOwner())) {
            msg = "Попытка забронировать свою вещь";
            log.info(msg);
            throw new NoContentException(msg);
        } else if (booking.getStatus() != BookingStatus.WAITING && Objects.nonNull(booking.getStatus())) {
            msg = "Этот статус запрещен при создании бронирования";
            log.info(msg);
            throw new BadRequestException(msg);
        } else if (!isBookingAvailable(item.getId(), booking.getStart(), booking.getEnd())) {
            msg = String.format("Вещь недоступна к бронированию, период занят"
                            + "status=%s, start=%s, end=%s",
                    booking.getStatus(), booking.getStart(), booking.getEnd());
            log.info(msg);
            throw new NoContentException(msg);
        } else {
            booking.setBooker(booker);
            booking.setStatus(BookingStatus.WAITING);
            Booking saved = bookingRepository.save(booking);
            entityManager.clear();
            msg = String.format("Создание бронирования id=%s, status=%s, start=%s, end=%s",
                    saved.getId(), saved.getStatus(), saved.getStart(), saved.getEnd());
            log.info(msg);
            return getDtoById(saved.getId());
        }
    }

    @Override
    @Transactional
    public BookingResponseDto setState(Long userId, Long bookingId, String state)
            throws BadRequestException, NoContentException {
        Booking booking = getById(bookingId);
        BookingStatus status = getStateByUser(booking, userId, state);
        LocalDateTime start = booking.getStart();
        LocalDateTime end = booking.getEnd();
        String msg = String.format("Подтверждение бронирования id=%s, status=%s, start=%s, end=%s",
                bookingId, status, start, end);
        log.info(msg);
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            msg = String.format("Статус бронирования %s уже менять нельзя!", bookingId);
            log.info(msg);
            throw new BadRequestException(msg);
        }
        bookingRepository.updateStatus(status.ordinal(), bookingId, userId);
        entityManager.clear();
        return getDtoById(bookingId);
    }

    @Override
    @Transactional
    public BookingResponseDto getFromBookerOrOwner(long userId, long bookingId)
            throws NoContentException {
        Booking booking = getById(bookingId);
        if (booking.getBooker().getId() == userId || booking.getItem().getOwner().getId() == userId) {
            return modelMapper.map(booking, BookingResponseDto.class);
        }
        String msg = "Нет доступа";
        log.info(msg);
        throw new NoContentException(msg);
    }

    @Override
    @Transactional
    public List<BookingResponseDto> getFromUserByRequest(
            long userId, String state, Boolean ownerOnly,
            Optional<Integer> from,
            Optional<Integer> size
    ) throws BadRequestException, NoContentException {
        if (from.orElse(1) <= 0 || size.orElse(1) <= 0) {
            String msg = "Нет такой страницы";
            log.info(msg);
            throw new BadRequestException(msg);
        }
        String strState = String.valueOf(BookingRequestStatus.getValue(state));
        List<Booking> bookingList;
        User user = userService.getUserById(userId);
        if (from.isEmpty() || size.isEmpty()) {
            bookingList = bookingRepository.getFromUserByState(userId, strState, ownerOnly);
        } else {
            bookingList = bookingRepository.getFromUserByStatePage(userId, strState, ownerOnly, from.get(), size.get());
        }

        return bookingList.stream()
                .map(Booking -> modelMapper.map(Booking, BookingResponseDto.class))
                .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public boolean isBookingAvailable(Long itemId, LocalDateTime start, LocalDateTime end) {
        boolean isAllowed = bookingRepository.isBookingAvailable(itemId, start, end);
        String msg = String.format("Бронь %s - %s вещь id{%s} свободна: {%s} ", start, end, itemId, isAllowed);
        log.info(msg);
        return isAllowed;
    }

    @Override
    @Transactional
    public BookingStatus getStateByUser(Booking booking, Long userId, String state) throws NoContentException {
        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        boolean bState = false;

        if (state.equalsIgnoreCase("true")) {
            bState = true;
        } else if (state.equalsIgnoreCase("false")) {
            bState = false;
        } else {
            String msg = String.format("Unknown state: %s", state);
            log.info(msg);
            throw new NoContentException(msg);
        }

        if (bState && Objects.equals(ownerId, userId)) {
            return BookingStatus.APPROVED;
        } else if (!bState && Objects.equals(ownerId, userId)) {
            return BookingStatus.REJECTED;
        } else if (!bState && Objects.equals(bookerId, userId)) {
            return BookingStatus.CANCELED;
        } else {
            String msg = String.format("Статус %s не применим к userId = %s, ownerId = %s, bookerId = %s",
                    state, userId, ownerId, bookerId);
            log.info(msg);
            throw new NoContentException(msg);
        }
    }
}
