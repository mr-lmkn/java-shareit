package ru.practicum.shareit.item;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
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
import ru.practicum.shareit.item.comment.dto.ItemCommentRequestDto;
import ru.practicum.shareit.item.comment.dto.ItemCommentResponseDto;
import ru.practicum.shareit.item.comment.model.ItemComment;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        verify(itemService).getItemDtoById(id, id);
    }

    @Test
    @SneakyThrows
    void create_null_err() {
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
        when(itemService.createItem(id, requestDto)).thenReturn(responseDto);
        String result = mvc.perform(post("/items")
                .header("X-Sharer-User-Id", id)
                .content(body)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertEquals(response, result);
    }


    @Test
    @SneakyThrows
    void getAllUserItems() {
        ItemResponseDto responseDto = ItemResponseDto.builder()
                .name("n")
                .description("d")
                .available(true)
                .owner(id)
                .build();
        when(itemService.getAllUserItems(id, Optional.of(1), Optional.of(1)))
                .thenReturn(List.of(responseDto));
        String result = mvc.perform(get("/items?userId=1&from=1&size=1")
                .header("X-Sharer-User-Id", id)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        List<ItemResponseDto> resp = List.of(responseDto);
        String response = mapper.writeValueAsString(resp);
        assertEquals(response, result);
    }


    @Test
    @SneakyThrows
    void getSerchItems() {
        ItemResponseDto responseDto = ItemResponseDto.builder()
                .name("n")
                .description("d")
                .available(true)
                .owner(id)
                .build();
        when(itemService.searchItemByName(id, "fff", Optional.of(1), Optional.of(1)))
                .thenReturn(List.of(responseDto));
        String result = mvc.perform(get("/items/search?text=fff&from=1&size=1")
                .header("X-Sharer-User-Id", id)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        List<ItemResponseDto> resp = List.of(responseDto);
        String response = mapper.writeValueAsString(resp);
        assertEquals(response, result);
    }

    @Test
    @SneakyThrows
    void create_comment_ok() {
        ItemCommentRequestDto requestDto = ItemCommentRequestDto.builder()
                .text("texttt")
                .build();
        ItemCommentResponseDto responseDto = ItemCommentResponseDto.builder()
                .itemId(id)
                .authorName("ffff")
                .id(id)
                .text("texttt")
                .build();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String response = mapper.writeValueAsString(responseDto);

        System.out.println("--> 1" + requestDto);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String body = mapper.writeValueAsString(requestDto);
        System.out.println("--> 2" + body);
        when(itemService.addComment(id, requestDto, 1L)).thenReturn(responseDto);
        String result = mvc.perform(post("/items/1/comment")
                .header("X-Sharer-User-Id", id)
                .content(body)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertEquals(response, result);
    }

    @Test
    @SneakyThrows
    void create_empty_err() {
        ItemComment comment = ItemComment.builder()
                .text("")
                .build();
        ItemCommentRequestDto requestDto = ItemCommentRequestDto.builder()
                .text("")
                .build();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String body = mapper.writeValueAsString(requestDto);
        mvc.perform(post("/items/1/comment")
                .header("X-Sharer-User-Id", id)
                .content(body)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().is4xxClientError());
    }

    @Test
    @SneakyThrows
    void delete_ok() {
        mvc.perform(delete("/items/1")
                .header("X-Sharer-User-Id", id)
        ).andExpect(status().isOk());
        verify(itemService).delete(id, id);
    }

    @Test
    @SneakyThrows
    void update_ok() {
        ItemRequestDto requestDto = new ItemRequestDto();
        String body = mapper.writeValueAsString(requestDto);
        Item newItem = Item.builder()
                .name("n")
                .available(true)
                .description("d")
                .owner(User.builder().email("bbb@bb.ru").name("bbb").build())
                .build();
        ItemResponseDto responseDto = ItemResponseDto.builder()
                .name("n")
                .description("d")
                .available(true)
                .owner(id)
                .build();
        when(modelMapper.map(newItem, ItemResponseDto.class)).thenReturn(responseDto);
        when(itemService.getAllUserItems(id, Optional.of(1), Optional.of(1)))
                .thenReturn(List.of(responseDto));

        mvc.perform(patch("/items/1")
                .header("X-Sharer-User-Id", id)
                .content(body)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

}