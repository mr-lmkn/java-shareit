package ru.practicum.shareit.item.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.comment.dto.ItemCommentRequestDto;
import ru.practicum.shareit.item.comment.dto.ItemCommentResponseDto;
import ru.practicum.shareit.item.comment.model.ItemComment;
import ru.practicum.shareit.item.comment.storage.CommentRepository;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.storage.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.enums.BookingStatus.APPROVED;

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
    @SneakyThrows
    void createItem_user_err() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().build();
        when(userService.getUserById(id)).thenReturn(null);
        assertThrows(NoContentException.class, () -> itemService.createItem(id, itemRequestDto));
    }

    @Test
    @SneakyThrows
    void createItem_item_err() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().build();
        assertThrows(BadRequestException.class, () -> itemService.createItem(id, null));
    }

    @Test
    void updateItem_err() {
        when(modelMapper.map(itemRequestDto, Item.class)).thenReturn(itemModel);
        assertThrows(NoContentException.class, () -> itemService.updateItem(id, id, itemRequestDto));
    }

    @Test
    void updateItem_id_err() {
        assertThrows(BadRequestException.class, () -> itemService.updateItem(id, null, itemRequestDto));
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

    @Test
    @SneakyThrows
    void addComment_err() {
        ItemCommentRequestDto inComment = ItemCommentRequestDto.builder().build();
        when(modelMapper.map(inComment, ItemComment.class)).thenReturn(new ItemComment());
        when(bookingRepository.findAllByUserBookings(id, id)).thenReturn(new ArrayList<>());
        when(itemRepository.findById(id)).thenReturn(Optional.of(itemModel));
        assertThrows(BadRequestException.class, () -> itemService.addComment(id, inComment, id));
    }

    @Test
    @SneakyThrows
    void getAllUserItems() {
        ArrayList<Item> itemsList = new ArrayList<>();
        Item item = Item.builder().id(1L).build();
        // add comment
        itemsList.add(item);
        ItemComment itemComment = ItemComment.builder().id(1L).item(item).build();
        ItemCommentResponseDto itemCommentResponseDto = ItemCommentResponseDto.builder().id(1L).itemId(1L).build();
        when(itemRepository.findAllByOwner(
                        userService.getUserById(1L),
                        PageRequest.of(1, 1, Sort.by("created").descending())
                )
        ).thenReturn(itemsList);
        when(commentRepository.findAllByItemIdIn(List.of(1L)))
                .thenReturn(List.of(itemComment));
        when(modelMapper.map(itemComment, ItemCommentResponseDto.class))
                .thenReturn(itemCommentResponseDto);
        // add booking
        LocalDateTime now = LocalDateTime.now();
        Booking booking = Booking.builder().item(item)
                .start(now)
                .end(now)
                .build();
        List<Booking> bookingList = new ArrayList<>();
        bookingList.add(booking);
        when(bookingRepository.findAllByItemInAndStatusOrderByStartAsc(itemsList, APPROVED))
                .thenReturn(bookingList);
        assertEquals(1,
                itemService.getAllUserItems(1L, Optional.of(1), Optional.of(1)).size()
        );
    }

    @Test
    @SneakyThrows
    void searchItemByName_no_text() {
        List<Item> ret = itemService.searchItemByName(id, "", Optional.of(1), Optional.of(1));
        assertEquals(0, ret.size());
    }

    @Test
    @SneakyThrows
    void searchItemByName_page_ok() {
        ArrayList<Item> itemsList = new ArrayList<>();
        itemsList.add(new Item());
        when(itemRepository.findUserItemLikePage("srch", "srch", 1, 1)).thenReturn(itemsList);
        List<Item> ret = itemService.searchItemByName(id, "srch", Optional.of(1), Optional.of(1));
        assertEquals(1, ret.size());
    }

    @Test
    @SneakyThrows
    void searchItemByName_ok() {
        ArrayList<Item> itemsList = new ArrayList<>();
        itemsList.add(new Item());
        when(itemRepository.findUserItemLike("srch", "srch")).thenReturn(itemsList);
        List<Item> ret = itemService.searchItemByName(id, "srch", Optional.empty(), Optional.empty());
        assertEquals(1, ret.size());
    }

    @Test
    @SneakyThrows
    void getItemById_err() {
        when(itemRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NoContentException.class, () -> itemService.getItemById(id, id));
    }

    @Test
    @SneakyThrows
    void getItemById_delete_ok() {
        itemService.delete(id, id);
    }
}