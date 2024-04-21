package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dtoValidateGroups.GroupCreate;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.comment.dto.ItemCommentRequestDto;
import ru.practicum.shareit.item.comment.dto.ItemCommentResponseDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemsService;
    private final ModelMapper modelMapper;
    private static final String OWNER_ID_HOLDER = "X-Sharer-User-Id";

    @GetMapping()
    public List<ItemResponseDto> getAllUserItems(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @RequestParam(value = "from", required = false)
            @Min(value = 0, message = "The value must be positive")
            Optional<Integer> from,
            @Positive
            @RequestParam(value = "size", required = false) Optional<Integer> size
    ) throws NoContentException {
        log.info("Got all Items request");
        return itemsService.getAllUserItems(userId, from, size).stream()
                .map(item -> modelMapper.map(item, ItemResponseDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/{id}")
    public ItemResponseDto getItem(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @PathVariable long id) throws NoContentException {
        log.info("Got Item request");
        return modelMapper.map(itemsService.getItemById(id, userId), ItemResponseDto.class);
    }

    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public ItemResponseDto create(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @Validated(GroupCreate.class) @RequestBody ItemRequestDto itemRequestDto
    ) throws BadRequestException, ConflictException, NoContentException {
        log.info("Got Item create request: {}", itemRequestDto);
        return modelMapper.map(itemsService.createItem(userId, itemRequestDto), ItemResponseDto.class);
    }

    @PatchMapping(path = "/{itemId}", consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public ItemResponseDto update(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @PathVariable long itemId,
            @Valid @RequestBody ItemRequestDto itemRequestDto
    ) throws BadRequestException, NoContentException {
        log.info("Got update Item id '{}' request: {}", itemId, itemRequestDto);
        return modelMapper.map(itemsService.updateItem(userId, itemId, itemRequestDto), ItemResponseDto.class);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json;")
    public void delete(@RequestHeader(OWNER_ID_HOLDER) long userId, @PathVariable long id)
            throws BadRequestException {
        log.info("Got delete item {} request", id);
        itemsService.delete(userId, id);
    }

    @GetMapping(value = "/search")
    public List<ItemResponseDto> searchItemByName(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @RequestParam(value = "text") String text,
            @RequestParam(value = "from", required = false)
            @Min(value = 0, message = "The value must be positive")
            Optional<Integer> from,
            @Positive
            @RequestParam(value = "size", required = false) Optional<Integer> size
    ) {
        log.info("Got search Item request");
        return itemsService.searchItemByName(userId, text, from, size).stream()
                .map(item -> modelMapper.map(item, ItemResponseDto.class))
                .collect(Collectors.toList());
    }

    @PostMapping("/{itemId}/comment")
    public ItemCommentResponseDto createComment(@RequestHeader(OWNER_ID_HOLDER) Long userId,
                                                @Validated @RequestBody ItemCommentRequestDto inComment,
                                                @PathVariable Long itemId)
            throws NoContentException, BadRequestException {
        log.info("Got add post request to item {} --> {} ", itemId, inComment.toString());
        return modelMapper.map(
                itemsService.addComment(userId, inComment, itemId),
                ItemCommentResponseDto.class);
    }
}
