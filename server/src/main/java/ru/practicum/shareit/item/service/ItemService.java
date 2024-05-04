package ru.practicum.shareit.item.service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.comment.dto.ItemCommentRequestDto;
import ru.practicum.shareit.item.comment.dto.ItemCommentResponseDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ItemService {

    List<ItemResponseDto> getAllUserItems(Long userId, Optional<Integer> from, Optional<Integer> size) throws NoContentException;

    Item getItemById(Long id, Long userId) throws NoContentException;

    ItemResponseDto getItemDtoById(Long id, Long userId) throws NoContentException;

    ArrayList<Item> getAllByRequestId(Long requestId);

    ArrayList<Item> getAllByRequestIdNotNull();

    Item setBookingsDto(Item item, List<Booking> bookings);

    ItemResponseDto createItem(Long userId, ItemRequestDto item) throws BadRequestException, ConflictException, NoContentException;

    ItemResponseDto updateItem(Long userID, Long itemId, ItemRequestDto item) throws BadRequestException, NoContentException;

    void delete(Long userId, Long id) throws BadRequestException;

    List<ItemResponseDto> searchItemByName(Long userId, String text, Optional<Integer> from, Optional<Integer> size);

    ItemCommentResponseDto addComment(Long userId, ItemCommentRequestDto inComment, Long itemId)
            throws NoContentException, BadRequestException;

    List<ItemCommentResponseDto> getAllItemComments(Long itemId);
}
