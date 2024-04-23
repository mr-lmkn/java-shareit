package ru.practicum.shareit.request;

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
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.RequestItemRequestDto;
import ru.practicum.shareit.request.dto.RequestItemResponseDto;
import ru.practicum.shareit.request.model.RequestItem;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class ItemRequestControllerTest {
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

    @Test
    void getAllUserRequests() {
    }

    private final ObjectMapper mapper = new ObjectMapper();

    private final long id = 1L;

    RequestItemRequestDto requestDto = RequestItemRequestDto.builder().description("d").build();
    RequestItem requestItem = RequestItem.builder().description("d").build();
    RequestItemResponseDto responsetItem = RequestItemResponseDto.builder()
            .description("d")
            .items(new ArrayList<Item>())
            .build();


    @Test
    @SneakyThrows
    void create_ok() {
        mapper.registerModule(new JavaTimeModule());
        String requestBody = mapper.writeValueAsString(requestDto);
        when(requestService.create(id, requestDto)).thenReturn(requestItem);

        mvc.perform(post("/requests")
                .header("X-Sharer-User-Id", id)
                .content(requestBody)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        verify(requestService).create(id, requestDto);
        verify(modelMapper).map(requestItem, RequestItemResponseDto.class);
    }

    @Test
    @SneakyThrows
    void getAllRequests_ok() {
        when(requestService.getAll(id, Optional.empty(), Optional.empty())).thenReturn(List.of(requestItem));

        mvc.perform(get("/requests/all")
                .header("X-Sharer-User-Id", id)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        verify(requestService).getAll(id, Optional.empty(), Optional.empty());
        verify(modelMapper).map(requestItem, RequestItemResponseDto.class);
    }

    @Test
    @SneakyThrows
    void getAllUserRequests_ok() {
        when(requestService.getAllUserItemRequests(id)).thenReturn(List.of(requestItem));
        mvc.perform(get("/requests")
                .header("X-Sharer-User-Id", id)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        verify(requestService).getAllUserItemRequests(id);
        verify(modelMapper).map(requestItem, RequestItemResponseDto.class);
    }

    @Test
    @SneakyThrows
    void getRequest() {
        when(requestService.getById(id, id)).thenReturn(requestItem);
        when(modelMapper.map(requestItem, RequestItemResponseDto.class))
                .thenReturn(responsetItem);
        String waitResponse = mapper.writeValueAsString(responsetItem);

        String getResponse = mvc.perform(
                        get("/requests/1")
                                .header("X-Sharer-User-Id", id)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ;

        verify(requestService).getById(id, id);
        verify(modelMapper).map(requestItem, RequestItemResponseDto.class);

        assertEquals(waitResponse, getResponse);
    }

}