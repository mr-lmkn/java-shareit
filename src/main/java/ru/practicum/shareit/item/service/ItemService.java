package ru.practicum.shareit.item.service;

import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {

    List<ItemResponseDto> getAllUserItems(int userId);

    ItemResponseDto getItemById(Integer id) throws NoContentException;

    ItemResponseDto createItem(Integer userId, ItemRequestDto item) throws BadRequestException, ConflictException, NoContentException;

    ItemResponseDto updateItem(Integer userID, Integer itemId, ItemRequestDto item) throws BadRequestException, NoContentException;

    void delete(Integer userId, Integer id) throws BadRequestException;

    List<ItemResponseDto> searchItemByName(Integer userId, String text);

}
