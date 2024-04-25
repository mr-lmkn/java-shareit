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
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.comment.dto.ItemCommentRequestDto;
import ru.practicum.shareit.item.comment.dto.ItemCommentResponseDto;
import ru.practicum.shareit.item.comment.model.ItemComment;
import ru.practicum.shareit.item.comment.storage.CommentRepository;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.storage.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private final User user = User.builder().id(id).email("xxx@mm.eee").build();
    private final Item itemModel = Item.builder()
            .id(id)
            .owner(user)
            .available(true)
            .lastBooking(new BookingResponseDto())
            .nextBooking(new BookingResponseDto())
            .build();
    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder().owner(id).id(id).available(true).build();
    private final ItemResponseDto itemResponseDto = ItemResponseDto.builder()
            .id(id)
            .owner(id)
            .available(true)
            .lastBooking(new BookingShortResponseDto())
            .nextBooking(new BookingShortResponseDto())
            .build();

    @Test
    @SneakyThrows
    void createItem_request_ok() {
        Item itemModel2 = Item.builder().requestId(id).build();
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().requestId(id).build();
        when(userService.getUserById(id)).thenReturn(user);
        when(modelMapper.map(itemRequestDto, Item.class)).thenReturn(itemModel2);
        when(itemRepository.save(itemModel2)).thenReturn(itemModel2);
        when(modelMapper.map(itemModel2, ItemResponseDto.class)).thenReturn(itemResponseDto);
        ItemResponseDto createdItem = itemService.createItem(id, itemRequestDto);
        assertEquals(itemResponseDto, createdItem);
    }

    @Test
    @SneakyThrows
    void createItem_ok() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().build();
        when(userService.getUserById(id)).thenReturn(user);
        when(modelMapper.map(itemRequestDto, Item.class)).thenReturn(itemModel);
        when(modelMapper.map(itemModel, ItemResponseDto.class)).thenReturn(itemResponseDto);
        when(itemRepository.save(itemModel)).thenReturn(itemModel);
        ItemResponseDto createdItem = itemService.createItem(id, itemRequestDto);
        assertEquals(itemResponseDto, createdItem);
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
        when(modelMapper.map(itemModel, ItemResponseDto.class)).thenReturn(itemResponseDto);
        when(itemRepository.partialUpdate(null, null, true, 1L, 1L))
                .thenReturn(1);
        when(itemRepository.findById(id)).thenReturn(Optional.of(itemModel));
        ItemResponseDto updated = itemService.updateItem(id, id, itemRequestDto);
        assertEquals(itemResponseDto, updated);
    }

    @Test
    @SneakyThrows
    void addComment() {
        ItemCommentRequestDto inComment = ItemCommentRequestDto.builder().text("dd").build();
        ItemComment comment = ItemComment.builder().text("dd").build();
        ItemCommentResponseDto outComment = ItemCommentResponseDto.builder().text("dd").build();
        when(modelMapper.map(inComment, ItemComment.class)).thenReturn(comment);
        when(modelMapper.map(comment, ItemCommentResponseDto.class)).thenReturn(outComment);
        when(bookingRepository.findAllByUserBookings(anyLong(), anyLong())).thenReturn(List.of(new Booking()));
        when(itemRepository.findById(id)).thenReturn(Optional.of(itemModel));
        when(commentRepository.save(comment)).thenReturn(comment);
        ItemCommentResponseDto newItemComment = itemService.addComment(id, inComment, id);
        assertEquals(outComment.getItemId(), newItemComment.getItemId());
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
        LocalDateTime now = now();
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
        List<ItemResponseDto> ret = itemService.searchItemByName(id, "", Optional.of(1), Optional.of(1));
        assertEquals(0, ret.size());
    }

    @Test
    @SneakyThrows
    void searchItemByName_page_ok() {
        ArrayList<Item> itemsList = new ArrayList<>();
        itemsList.add(new Item());
        when(itemRepository
                .findUserItemLikePage("srch", "srch", 1, 1))
                .thenReturn(itemsList);
        List<ItemResponseDto> ret = itemService.searchItemByName(id, "srch", Optional.of(1), Optional.of(1));
        assertEquals(1, ret.size());
    }

    @Test
    @SneakyThrows
    void searchItemByName_ok() {
        ArrayList<Item> itemsList = new ArrayList<>();
        itemsList.add(new Item());
        when(itemRepository.findUserItemLike("srch", "srch")).thenReturn(itemsList);
        List<ItemResponseDto> ret = itemService.searchItemByName(id, "srch", Optional.empty(), Optional.empty());
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
    void getItemDtoById_ok() {
        ItemComment comment = new ItemComment();
        BookingResponseDto bookingDto = BookingResponseDto.builder()
                .start(now().toString()).end(now().toString()).build();
        BookingShortResponseDto bookingShortDto = BookingShortResponseDto.builder()
                .start(now().toString()).end(now().toString()).build();
        Item item = Item.builder()
                .id(id)
                .owner(user)
                .available(true)
                .lastBooking(bookingDto)
                .nextBooking(bookingDto)
                .comments(List.of(new ItemCommentResponseDto()))
                .build();
        ItemResponseDto waitResponse = ItemResponseDto.builder()
                .id(id)
                .owner(user.getId())
                .available(true)
                .lastBooking(bookingShortDto)
                .nextBooking(bookingShortDto)
                .comments(List.of(new ItemCommentResponseDto()))
                .build();
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(id)).thenReturn(List.of(comment));
        when(modelMapper.map(comment, ItemCommentResponseDto.class)).thenReturn(new ItemCommentResponseDto());
        when(modelMapper.map(item, ItemResponseDto.class)).thenReturn(waitResponse);
        assertEquals(true, itemService.getItemDtoById(id, id).getAvailable());
    }

    @Test
    @SneakyThrows
    void deleteItem_ok() {
        itemService.delete(id, id);
    }

    @Test
    @SneakyThrows
    void getAllByRequestId() {
        when(itemRepository.findAllByRequestId(id)).thenReturn(new ArrayList<Item>());
        itemService.getAllByRequestId(id);
    }

    @Test
    @SneakyThrows
    void getAllByRequestIdNotNull() {
        when(itemRepository.findAllByRequestIdNotNull()).thenReturn(new ArrayList<Item>());
        itemService.getAllByRequestIdNotNull();
    }

}