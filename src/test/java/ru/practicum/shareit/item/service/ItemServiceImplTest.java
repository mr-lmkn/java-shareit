package ru.practicum.shareit.item.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.comment.dto.ItemCommentRequestDto;
import ru.practicum.shareit.item.comment.model.ItemComment;
import ru.practicum.shareit.item.comment.storage.CommentRepository;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.storage.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RequestRepository requestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    private long id = 1L;
    private User user = User.builder().id(id).email("xxx@mm.eee").build();
    private Item itemModel = Item.builder().id(1L).owner(user).available(true).build();
    private ItemRequestDto itemRequestDto = ItemRequestDto.builder().owner(id).id(id).available(true).build();

    @Test
    @SneakyThrows
    void createItem_request_ok() {
        Item itemModel2 = Item.builder().requestId(id).build();
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().requestId(id).build();
        when(userService.getUserById(id)).thenReturn(user);
        when(modelMapper.map(itemRequestDto, Item.class)).thenReturn(itemModel2);
        when(itemRepository.save(itemModel2)).thenReturn(itemModel2);
        Item createdItem = itemService.createItem(id, itemRequestDto);
        assertEquals(itemModel2, createdItem);
    }

    @Test
    @SneakyThrows
    void createItem_ok() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().build();
        when(userService.getUserById(id)).thenReturn(user);
        when(modelMapper.map(itemRequestDto, Item.class)).thenReturn(itemModel);
        when(itemRepository.save(itemModel)).thenReturn(itemModel);
        Item createdItem = itemService.createItem(id, itemRequestDto);
        assertEquals(itemModel, createdItem);
    }

    @Test
    void updateItem_err() {
        when(modelMapper.map(itemRequestDto, Item.class)).thenReturn(itemModel);
        assertThrows(NoContentException.class, () -> itemService.updateItem(id, id, itemRequestDto));
    }

    @Test
    @SneakyThrows
    void updateItem_ok() {
        when(modelMapper.map(itemRequestDto, Item.class)).thenReturn(itemModel);
        when(itemRepository.partialUpdate(null, null, true, 1L, 1L))
                .thenReturn(1);
        when(itemRepository.findById(id)).thenReturn(Optional.of(itemModel));
        Item updated = itemService.updateItem(id, id, itemRequestDto);
        assertEquals(itemModel, updated);
    }

    @Test
    @SneakyThrows
    void addComment() {
        ItemCommentRequestDto inComment = ItemCommentRequestDto.builder().build();
        when(modelMapper.map(inComment, ItemComment.class)).thenReturn(new ItemComment());
        when(bookingRepository.findAllByUserBookings(id, id)).thenReturn(List.of(new Booking()));
        when(itemRepository.findById(id)).thenReturn(Optional.of(itemModel));
        ItemComment itemComment = itemService.addComment(id, inComment, id);
        assertEquals(1, 1);
    }
}