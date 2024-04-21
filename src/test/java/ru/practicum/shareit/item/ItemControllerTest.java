package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class ItemControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ModelMapper modelMapper;
    @MockBean
    private ItemService itemService;
    @MockBean
    private BookingService bookingService;
    @MockBean
    private RequestService requestService;
    @MockBean
    private UserService userService;

    private final ObjectMapper mapper = new ObjectMapper();

    private long id = 1L;

    @Test
    @SneakyThrows
    void getItem() {
        mvc.perform(get("/items/1").header("X-Sharer-User-Id", id))
                .andExpect(status().isOk());
        verify(itemService).getItemById(id, id);
    }

    @Test
    @SneakyThrows
    void create_empty_err() {
        ItemRequestDto requestDto = new ItemRequestDto();
        String body = mapper.writeValueAsString(requestDto);
        mvc.perform(post("/items")
                .header("X-Sharer-User-Id", id)
                .content(body)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().is4xxClientError());
    }

    @Test
    @SneakyThrows
    void create_available_err() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .name("n")
                .description("d")
                .owner(id)
                .build();
        String body = mapper.writeValueAsString(requestDto);
        mvc.perform(post("/items")
                .header("X-Sharer-User-Id", id)
                .content(body)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().is4xxClientError());
    }


    @Test
    @SneakyThrows
    void create_ok() {
        Item newItem = Item.builder()
                .name("n")
                .available(true)
                .description("d")
                .owner(User.builder().email("bbb@bb.ru").name("bbb").build())
                .build();
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .name("n")
                .description("d")
                .available(true)
                .owner(id)
                .build();
        ItemResponseDto responseDto = ItemResponseDto.builder()
                .name("n")
                .description("d")
                .available(true)
                .owner(id)
                .build();
        String response = mapper.writeValueAsString(responseDto);
        String body = mapper.writeValueAsString(requestDto);
        when(itemService.createItem(id, requestDto)).thenReturn(newItem);
        when(modelMapper.map(newItem, ItemResponseDto.class)).thenReturn(responseDto);
        String result = mvc.perform(post("/items")
                .header("X-Sharer-User-Id", id)
                .content(body)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertEquals(response, result);
        verify(modelMapper).map(newItem, ItemResponseDto.class);
    }

}