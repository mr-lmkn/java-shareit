package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storege.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemsStorage;
    private final UserStorage userStorage;
    private final ModelMapper modelMapper;

    @Override
    public List<ItemResponseDto> getAllUserItems(int userId) {
        return itemsStorage.getAllUserItems(userId).stream()
                .map(item -> modelMapper.map(item, ItemResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public ItemResponseDto getItemById(Integer id) throws NoContentException {
        return modelMapper.map(itemsStorage.getItemById(id), ItemResponseDto.class);
    }

    @Override
    public ItemResponseDto createItem(Integer userID, ItemRequestDto itemRequestDto)
            throws BadRequestException, ConflictException, NoContentException {
        User user = userStorage.getUserById(userID);
        itemRequestDto.setOwner(userID);
        Item item = modelMapper.map(itemRequestDto, Item.class);
        return modelMapper.map(itemsStorage.createItem(item), ItemResponseDto.class);
    }

    @Override
    public ItemResponseDto updateItem(Integer userID,
                                      Integer itemId,
                                      ItemRequestDto itemRequestDto) throws BadRequestException, NoContentException {
        Item item = modelMapper.map(itemRequestDto, Item.class);
        item.setOwner(userID);
        return modelMapper.map(itemsStorage.updateItem(itemId, item), ItemResponseDto.class);
    }

    @Override
    public void delete(Integer userid, Integer id) throws BadRequestException {
        itemsStorage.delete(userid, id);
    }

    @Override
    public List<ItemResponseDto> searchItemByName(Integer userId, String text) {
        if (text.isEmpty()) {
            return new ArrayList<ItemResponseDto>();
        }
        return itemsStorage.searchItemByName(userId, text).stream()
                .map(item -> modelMapper.map(item, ItemResponseDto.class))
                .collect(Collectors.toList());
    }

}