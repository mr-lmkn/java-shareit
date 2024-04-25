package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingRequestStatus;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class BookingControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ModelMapper modelMapper;
    @MockBean
    private ItemService itemService;
    @MockBean
    private RequestService requestService;
    @MockBean
    private BookingService bookingService;
    @MockBean
    private UserService userService;

    private final ObjectMapper mapper = new ObjectMapper();

    private final long id = 1L;

    private final LocalDateTime from = LocalDateTime.now().plusDays(1);
    private final LocalDateTime to = LocalDateTime.now().plusDays(2);

    private final BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
            .id(id)
            .start(from)
            .end(to)
            .status(BookingStatus.APPROVED)
            .booker(id)
            .itemId(id)
            .build();
    private final BookingResponseDto bookingResponseDto = BookingResponseDto.builder()
            .id(id)
            .start(String.valueOf(from))
            .end(String.valueOf(to))
            .status(BookingStatus.APPROVED)
            .booker(UserResponseDto.builder().id(id).name("A").build())
            .item(ItemResponseDto.builder().id(id).name("B").build())
            .build();
    private final Booking booking = Booking.builder()
            .id(id)
            .start(from)
            .end(to)
            .status(BookingStatus.APPROVED)
            .booker(User.builder().id(id).name("A").build())
            .item(Item.builder().id(id).name("B").build())
            .build();

    @Test
    @SneakyThrows
    void add_time_err() {
        mapper.registerModule(new JavaTimeModule());
        BookingRequestDto errBookingRequest = BookingRequestDto.builder()
                .id(id)
                .end(LocalDateTime.now().plusDays(-1))
                .start(LocalDateTime.now())
                .status(BookingStatus.APPROVED)
                .booker(id)
                .itemId(id)
                .build();
        mapper.registerModule(new JavaTimeModule());
        String requestBody = mapper.writeValueAsString(errBookingRequest);

        mvc.perform(post("/bookings")
                .header("X-Sharer-User-Id", id)
                .content(requestBody)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().is4xxClientError());
    }

    @Test
    @SneakyThrows
    void add_ok() {
        mapper.registerModule(new JavaTimeModule());
        String requestBody = mapper.writeValueAsString(bookingRequestDto);

        mvc.perform(post("/bookings")
                .header("X-Sharer-User-Id", id)
                .content(requestBody)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getFromUser() {
        String waitedResponse = mapper.writeValueAsString(List.of(bookingResponseDto));
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);
        when(bookingService.getFromUserByRequest(
                id,
                BookingRequestStatus.ALL.toString(),
                false,
                Optional.of(1),
                Optional.of(1))
        ).thenReturn(bookings);
        when(modelMapper.map(booking, BookingResponseDto.class)).thenReturn(bookingResponseDto);
        String result = mvc.perform(get("/bookings/?state=ALL&from=1&size=1")
                        .header("X-Sharer-User-Id", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals(waitedResponse, result);
    }

    @Test
    @SneakyThrows
    void getFromOwner() {
        String waitedResponse = mapper.writeValueAsString(List.of(bookingResponseDto));
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);
        when(bookingService.getFromUserByRequest(
                id,
                BookingRequestStatus.ALL.toString(),
                true,
                Optional.of(1),
                Optional.of(1))
        ).thenReturn(bookings);
        when(modelMapper.map(booking, BookingResponseDto.class)).thenReturn(bookingResponseDto);
        String result = mvc.perform(get("/bookings/owner/?state=ALL&from=1&size=1")
                        .header("X-Sharer-User-Id", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals(waitedResponse, result);
    }

    @Test
    @SneakyThrows
    void setAppruve_ok() {
        when(modelMapper.map(booking, BookingResponseDto.class)).thenReturn(bookingResponseDto);
        when(bookingService.setState(id, id, "true")).thenReturn(bookingResponseDto);
        String result = mvc.perform(patch("/bookings/1?bookingId=1&approved=true")
                        .header("X-Sharer-User-Id", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    @SneakyThrows
    void getFromBookerOrOwner_ok() {
        when(bookingService.getFromBookerOrOwner(id, id)).thenReturn(bookingResponseDto);
        when(modelMapper.map(booking, BookingResponseDto.class)).thenReturn(bookingResponseDto);
        bookingResponseDto.getItem();
        String result = mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}