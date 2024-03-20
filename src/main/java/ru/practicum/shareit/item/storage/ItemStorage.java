package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    List<Item> getAllItems();

    List<Item> getAllUserItems(int userId);

    Item getItemById(Integer id) throws NoContentException;

    Item createItem(Item item) throws BadRequestException, ConflictException;

    Item updateItem(Integer itemId, Item item) throws BadRequestException, NoContentException;

    void delete(Integer userid, Integer id) throws BadRequestException;

    List<Item> searchItemByName(Integer userId, String text);
}
