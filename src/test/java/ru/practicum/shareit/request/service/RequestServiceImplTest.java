package ru.practicum.shareit.request.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.comment.storage.CommentRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.RequestItemRequestDto;
import ru.practicum.shareit.request.model.RequestItem;
import ru.practicum.shareit.request.storage.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
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
    @InjectMocks
    private RequestServiceImpl requestService;

    private long id = 1L;
    private User user = User.builder().id(id).email("xxx@mm.eee").build();
    private RequestItem requestItem = RequestItem.builder().description("x").build();
    private RequestItemRequestDto itemRequestDto = RequestItemRequestDto.builder().build();


    @Test
    @SneakyThrows
    void getAll_no_page_ok() {
        List<RequestItem> wait = List.of(requestItem);
        when(userService.getUserById(id)).thenReturn(user);
        when(requestRepository
                .findAllByRequesterNotInOrderByCreated(List.of(user))
        ).thenReturn(List.of(new RequestItem()));
        when(requestRepository.findAllByRequesterNotInOrderByCreated(List.of(user))).thenReturn(wait);

        List<RequestItem> ret = requestService.getAll(id, Optional.empty(), Optional.empty());
        assertEquals(wait.get(0).getDescription(), ret.get(0).getDescription());
    }

    @Test
    @SneakyThrows
    void getAll_page_ok() {
        List<RequestItem> wait = List.of(requestItem);
        when(userService.getUserById(id)).thenReturn(user);
        when(requestRepository
                .findAllByRequesterNotIn(List.of(user),
                        PageRequest.of(1, 1, Sort.by("created").descending())
                )
        ).thenReturn(wait);
        List<RequestItem> ret = requestService.getAll(id, Optional.of(1), Optional.of(1));
        assertEquals(wait.get(0).getDescription(), ret.get(0).getDescription());
    }

    @Test
    void getAllUserItemRequests() {
    }

    @Test
    @SneakyThrows
    void getById_ok() {
        when(userService.getUserById(id)).thenReturn(user);
        when(requestRepository.findById(id)).thenReturn(Optional.of(requestItem));
        assertEquals(requestItem.getDescription(), requestService.getById(id, id).getDescription());
    }

    @Test
    @SneakyThrows
    void getById_err() {
        when(userService.getUserById(id)).thenReturn(user);
        when(requestRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NoContentException.class, () -> requestService.getById(id, id).getDescription());
    }

    @Test
    @SneakyThrows
    void create_ok() {
        when(userService.getUserById(id)).thenReturn(user);
        when(modelMapper.map(itemRequestDto, RequestItem.class)).thenReturn(requestItem);
        when(requestRepository.save(requestItem)).thenReturn(requestItem);
        assertEquals(requestItem, requestService.create(id, itemRequestDto));
    }

    @Test
    @SneakyThrows
    void create_no_user_err() {
        when(userService.getUserById(id)).thenReturn(null);
        assertThrows(NoContentException.class, () -> requestService.create(id, itemRequestDto));
    }

    @Test
    @SneakyThrows
    void create_no_data_err() {
        assertThrows(BadRequestException.class, () -> requestService.create(id, null));
    }
}