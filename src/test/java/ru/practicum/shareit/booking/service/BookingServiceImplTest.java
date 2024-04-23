package ru.practicum.shareit.booking.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.comment.storage.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.request.storage.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private EntityManager entityManager;
    @Mock
    private ItemService itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RequestRepository requestRepository;
    @Mock
    private RequestServiceImpl requestService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final long id = 1L;
    private final long id2 = 2L;
    private final LocalDateTime from = LocalDateTime.now().plusDays(1);
    private final LocalDateTime to = LocalDateTime.now().plusDays(2);
    private final User user = User.builder().id(id).email("xxx@mm.eee").build();

    private final Item item = Item.builder().id(id).owner(user).name("ff").available(true).build();
    private final Booking booking = Booking.builder()
            .id(id).start(from).end(to)
            .item(item).booker(user)
            .build();
    private final BookingRequestDto bookingRequestDto = BookingRequestDto.builder().itemId(id).build();

    @Test
    @SneakyThrows
    void add_no_item_err() {
        when(itemService.getItemById(id, id)).thenReturn(item);
        when(userService.getUserById(id)).thenReturn(user);
        when(modelMapper.map(bookingRequestDto, Booking.class)).thenReturn(booking);
        assertThrows(NoContentException.class, () -> bookingService.add(id, bookingRequestDto));
    }

    @Test
    @SneakyThrows
    void add_item_disabled_err() {
        Item itemX = Item.builder().id(id).owner(user).name("ff").available(false).build();
        when(itemService.getItemById(id, id)).thenReturn(itemX);
        when(userService.getUserById(id)).thenReturn(user);
        when(modelMapper.map(bookingRequestDto, Booking.class)).thenReturn(booking);
        assertThrows(BadRequestException.class, () -> bookingService.add(id, bookingRequestDto));
    }

    @Test
    @SneakyThrows
    void add_booking_owner_err() {
        when(itemService.getItemById(id, id)).thenReturn(item);
        when(userService.getUserById(id)).thenReturn(user);
        when(modelMapper.map(bookingRequestDto, Booking.class)).thenReturn(booking);
        assertThrows(NoContentException.class, () -> bookingService.add(id, bookingRequestDto));
    }

    @Test
    @SneakyThrows
    void add_booking_status_err() {
        Booking bookingX = Booking.builder().status(BookingStatus.APPROVED).build();
        when(itemService.getItemById(id, id2)).thenReturn(item);
        when(userService.getUserById(id2)).thenReturn(User.builder().id(id2).build());
        when(modelMapper.map(bookingRequestDto, Booking.class)).thenReturn(bookingX);
        assertThrows(BadRequestException.class, () -> bookingService.add(id2, bookingRequestDto));
    }

    @Test
    @SneakyThrows
    void add_not_available_err() {
        when(itemService.getItemById(id, id2)).thenReturn(item);
        when(userService.getUserById(id2)).thenReturn(User.builder().id(id2).build());
        when(modelMapper.map(bookingRequestDto, Booking.class)).thenReturn(booking);
        when(bookingService.isBookingAvailable(id, from, to)).thenReturn(false);
        assertThrows(NoContentException.class, () -> bookingService.add(id2, bookingRequestDto).getId());
    }

    @Test
    @SneakyThrows
    void add_ok() {
        when(itemService.getItemById(id, id2)).thenReturn(item);
        when(userService.getUserById(id2)).thenReturn(User.builder().id(id2).build());
        when(modelMapper.map(bookingRequestDto, Booking.class)).thenReturn(booking);
        when(bookingService.isBookingAvailable(id, from, to)).thenReturn(true);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));
        assertEquals(id, bookingService.add(id2, bookingRequestDto).getId());
    }

    @Test
    @SneakyThrows
    void getStateByUser_err() {
        assertThrows(NoContentException.class, () -> bookingService.getStateByUser(booking, id, "AAAA"));
    }

    @Test
    @SneakyThrows
    void getStateByUser_ok() {
        assertEquals("APPROVED", bookingService.getStateByUser(booking, id, "true").toString());
    }

    @Test
    @SneakyThrows
    void getStateByUser_rej_ok() {
        assertEquals("REJECTED", bookingService.getStateByUser(booking, id, "false").toString());
    }

    @Test
    @SneakyThrows
    void getStateByUser_cansel_err() {
        assertThrows(NoContentException.class, () -> bookingService.getStateByUser(booking, 2L, "false"));
    }

    @Test
    @SneakyThrows
    void getStateByUser_cansel_ok() {
        Long bookerId = 2L;
        Booking booking = Booking.builder()
                .id(id).start(from).end(to)
                .item(Item.builder().owner(User.builder().id(id).build()).build()).booker(user)
                .booker(User.builder().id(bookerId).build())
                .status(BookingStatus.APPROVED)
                .build();
        assertEquals("CANCELED", bookingService.getStateByUser(booking, bookerId, "false").toString());
    }

    @Test
    @SneakyThrows
    void setState_Err() {
        Booking booking = Booking.builder()
                .id(id).start(from).end(to)
                .item(item).booker(user)
                .status(BookingStatus.APPROVED)
                .build();
        when(bookingRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findById(1L)).thenReturn(Optional.ofNullable(booking));

        assertThrows(BadRequestException.class, () -> bookingService.setState(1L, 1L, "true"));
    }

    @Test
    @SneakyThrows
    void setState_Ok() {
        Booking booking = Booking.builder()
                .id(id).start(from).end(to)
                .item(item).booker(user)
                .status(BookingStatus.CANCELED)
                .build();
        when(bookingRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findById(1L)).thenReturn(Optional.ofNullable(booking));

        Booking updBooking = bookingService.setState(1L, 1L, "true");

        assertEquals(1L, updBooking.getId());
    }

    @Test
    @SneakyThrows
    void getFromBookerOrOwner_Ok() {
        Booking booking = Booking.builder()
                .id(id).start(from).end(to)
                .item(item).booker(user)
                .status(BookingStatus.CANCELED)
                .build();
        when(bookingRepository.existsById(id)).thenReturn(true);
        when(bookingRepository.findById(id)).thenReturn(Optional.ofNullable(booking));

        Booking updBooking = bookingService.getFromBookerOrOwner(id, id);
        assertEquals(id, updBooking.getId());
    }

    @Test
    @SneakyThrows
    void getFromBookerOrOwner_no_access_err() {
        Booking booking = Booking.builder()
                .id(id).start(from).end(to)
                .item(item).booker(user)
                .status(BookingStatus.CANCELED)
                .build();
        when(bookingRepository.existsById(id)).thenReturn(true);
        when(bookingRepository.findById(id)).thenReturn(Optional.ofNullable(booking));

        assertThrows(NoContentException.class, () -> bookingService.getFromBookerOrOwner(10L, id));
    }

    @Test
    @SneakyThrows
    void getFromUserByRequest_err() {
        assertThrows(BadRequestException.class,
                () -> bookingService.getFromUserByRequest(id, "",
                        true, Optional.of(-1), Optional.of(-1)
                ));
    }

    @Test
    @SneakyThrows
    void getFromUserByRequest_all_ok() {
        when(userService.getUserById(id)).thenReturn(user);
        when(bookingRepository.getFromUserByStatePage(id, "ALL", true, 1, 1))
                .thenReturn(List.of(booking));
        assertEquals(1,
                bookingService.getFromUserByRequest(id, "ALL",
                        true, Optional.of(1), Optional.of(1)
                ).size()
        );
    }

    @Test
    @SneakyThrows
    void getFromUserByRequest_current_ok() {
        when(userService.getUserById(id)).thenReturn(user);
        when(bookingRepository.getFromUserByStatePage(id, "CURRENT", true, 1, 1))
                .thenReturn(List.of(booking));
        assertEquals(1,
                bookingService.getFromUserByRequest(id, "CURRENT",
                        true, Optional.of(1), Optional.of(1)
                ).size()
        );
    }

    @Test
    @SneakyThrows
    void getFromUserByRequest_past_ok() {
        when(userService.getUserById(id)).thenReturn(user);
        when(bookingRepository.getFromUserByStatePage(id, "PAST", true, 1, 1))
                .thenReturn(List.of(booking));
        assertEquals(1,
                bookingService.getFromUserByRequest(id, "PAST",
                        true, Optional.of(1), Optional.of(1)
                ).size()
        );
    }

    @Test
    @SneakyThrows
    void getFromUserByRequest_rejected_ok() {
        when(userService.getUserById(id)).thenReturn(user);
        when(bookingRepository.getFromUserByStatePage(id, "REJECTED", true, 1, 1))
                .thenReturn(List.of(booking));
        assertEquals(1,
                bookingService.getFromUserByRequest(id, "REJECTED",
                        true, Optional.of(1), Optional.of(1)
                ).size()
        );
    }

    @Test
    @SneakyThrows
    void getFromUserByRequest_waiting_ok() {
        when(userService.getUserById(id)).thenReturn(user);
        when(bookingRepository.getFromUserByStatePage(id, "WAITING", true, 1, 1))
                .thenReturn(List.of(booking));
        assertEquals(1,
                bookingService.getFromUserByRequest(id, "WAITING",
                        true, Optional.of(1), Optional.of(1)
                ).size()
        );
    }

    @Test
    @SneakyThrows
    void getById_err_ok() {
        when(bookingRepository.existsById(id)).thenReturn(false);
        assertThrows(NoContentException.class,
                () -> bookingService.getById(id)
        );
    }

}