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
    public ItemResponseDto create(@RequestHeader("X-Sharer-User-Id") int userId,
                                  @Validated(GroupCreate.class) @RequestBody ItemRequestDto itemRequestDtotem)
            throws BadRequestException, ConflictException, NoContentException {
        log.info("Got Item create request: {}", itemRequestDtotem);
        return itemsService.createItem(userId, itemRequestDtotem);
    }

    @PatchMapping(path = "/{itemId}", consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public ItemResponseDto update(@RequestHeader("X-Sharer-User-Id") int userId,
                                  @PathVariable Integer itemId,
                                  @Valid @RequestBody ItemRequestDto itemRequestDtotem)
            throws BadRequestException, NoContentException {
        log.info("Got update Item id '{}' request: {}", itemId, itemRequestDtotem);
        return itemsService.updateItem(userId, itemId, itemRequestDtotem);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json;")
    public void delete(@RequestHeader("X-Sharer-User-Id") int userId, @PathVariable Integer id)
            throws BadRequestException {
        log.info("Got delete item {} request", id);
        itemsService.delete(userId, id);
    }

    @GetMapping(value = "/search")
    public List<ItemResponseDto> searchItemByName(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                  @RequestParam(value = "text") String text) throws NoContentException {
        log.info("Got search Item request");
        return itemsService.searchItemByName(userId, text);
    }

}
