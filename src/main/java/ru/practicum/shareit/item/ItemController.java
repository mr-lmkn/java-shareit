package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dtoValidateGroups.GroupCreate;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemsService;
    private static final String OWNER_ID = "X-Sharer-User-Id";

    @GetMapping()
    public List<ItemResponseDto> getAllUserItems(@RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Got all Items request");
        return itemsService.getAllUserItems(userId);
    }

    @GetMapping(value = "/{id}")
    public ItemResponseDto getItem(@PathVariable Integer id) throws NoContentException {
        log.info("Got Item request");
        return itemsService.getItemById(id);
    }

    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public ItemResponseDto create(@RequestHeader(OWNER_ID) int userId,
                                  @Validated(GroupCreate.class) @RequestBody ItemRequestDto itemRequestDto)
            throws BadRequestException, ConflictException, NoContentException {
        log.info("Got Item create request: {}", itemRequestDto);
        return itemsService.createItem(userId, itemRequestDto);
    }

    @PatchMapping(path = "/{itemId}", consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public ItemResponseDto update(@RequestHeader(OWNER_ID) int userId,
                                  @PathVariable Integer itemId,
                                  @Valid @RequestBody ItemRequestDto itemRequestDto)
            throws BadRequestException, NoContentException {
        log.info("Got update Item id '{}' request: {}", itemId, itemRequestDto);
        return itemsService.updateItem(userId, itemId, itemRequestDto);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json;")
    public void delete(@RequestHeader(OWNER_ID) int userId, @PathVariable Integer id)
            throws BadRequestException {
        log.info("Got delete item {} request", id);
        itemsService.delete(userId, id);
    }

    @GetMapping(value = "/search")
    public List<ItemResponseDto> searchItemByName(@RequestHeader(OWNER_ID) Integer userId,
                                                  @RequestParam(value = "text") String text)
            throws NoContentException {
        log.info("Got search Item request");
        return itemsService.searchItemByName(userId, text);
    }

}
