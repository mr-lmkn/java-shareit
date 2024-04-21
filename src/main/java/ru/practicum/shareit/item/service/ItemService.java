package ru.practicum.shareit.item.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.comment.dto.ItemCommentRequestDto;
import ru.practicum.shareit.item.comment.model.ItemComment;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ItemService {

    List<Item> getAllUserItems(Long userId, Optional<Integer> from, Optional<Integer> size) throws NoContentException;

    Item getItemById(Long id, Long userId) throws NoContentException;

    ArrayList<Item> getAllByRequestId(Long requestId);

    ArrayList<Item> getAllByRequestIdNotNull();

    @Transactional
    Item setBookings(Item item, List<Booking> bookings);

    Item createItem(Long userId, ItemRequestDto item) throws BadRequestException, ConflictException, NoContentException;

    Item updateItem(Long userID, Long itemId, ItemRequestDto item) throws BadRequestException, NoContentException;

    void delete(Long userId, Long id) throws BadRequestException;

    List<Item> searchItemByName(Long userId, String text, Optional<Integer> from, Optional<Integer> size);

    ItemComment addComment(Long userId, ItemCommentRequestDto inComment, Long itemId)
            throws NoContentException, BadRequestException;

    List<ItemComment> getAllItemComments(Long itemId);
}
