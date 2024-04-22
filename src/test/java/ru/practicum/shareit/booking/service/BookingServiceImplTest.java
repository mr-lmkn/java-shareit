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
    private final Booking booking = Booking.builder().id(id).start(from).end(to).item(item).booker(user).build();
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

}