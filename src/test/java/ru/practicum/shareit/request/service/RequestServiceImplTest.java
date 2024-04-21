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
import ru.practicum.shareit.item.comment.storage.CommentRepository;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.RequestItem;
import ru.practicum.shareit.request.storage.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private Item itemModel = Item.builder().id(1L).owner(user).available(true).build();
    private RequestItem requestItem = RequestItem.builder().description("x").build();
    private ItemRequestDto itemRequestDto = ItemRequestDto.builder().owner(id).id(id).available(true).build();


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
        /*
        *  List<User> user = Collections.singletonList(userService.getUserById(userId));
        List<RequestItem> returnItems;
        if (from.isPresent() && size.isPresent()) {
            PageRequest page = PageRequest.of(from.get(), size.get(), Sort.by("created").descending());
            returnItems = requestRepository.findAllByRequesterNotIn(user, page);
        } else {
            returnItems = requestRepository.findAllByRequesterNotInOrderByCreated(user);
        }
        returnItems = setLinkedItems(returnItems);
        return returnItems;*/
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
    void getById() {
  /*      User user = userService.getUserById(userId);
        Optional<RequestItem> outModel = requestRepository.findById(id);
        if (outModel.isPresent()) {
            ArrayList<Item> itemList = itemService.getAllByRequestId(id);
            outModel.get().setItems(itemList);
            return outModel.get();
        }
        throw new NoContentException("Нет вещи");*/
    }

    @Test
    void create() {
    }
}