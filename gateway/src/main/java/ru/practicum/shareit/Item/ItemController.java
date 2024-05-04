package ru.practicum.shareit.Item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Item.comment.dto.ItemCommentRequestDto;
import ru.practicum.shareit.Item.dto.ItemRequestDto;
import ru.practicum.shareit.dtoValidateGroups.GroupCreate;
import ru.practicum.shareit.error.exceptions.BadRequestException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemClient itemClient;
    private static final String OWNER_ID_HOLDER = "X-Sharer-User-Id";

    @GetMapping()
    public ResponseEntity<Object> getAllUserItems(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @RequestParam(value = "from", required = false)
            @Min(value = 0, message = "The value must be positive")
            Optional<Integer> from,
            @Positive
            @RequestParam(value = "size", required = false) Optional<Integer> size
    ) throws BadRequestException {
        log.info("Got all Items request");
        try {
            return itemClient.getAllUserItems(userId, from, size);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getItem(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @PathVariable long id) {
        log.info("Got Item request");
        return itemClient.getItemById(id, userId);
    }

    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public ResponseEntity<Object> create(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @Validated(GroupCreate.class) @RequestBody ItemRequestDto itemRequestDto
    ) {
        log.info("Got Item create request: {}", itemRequestDto);
        return itemClient.createItem(userId, itemRequestDto);
    }

    @PatchMapping(path = "/{itemId}", consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public ResponseEntity<Object> update(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @PathVariable long itemId,
            @Valid @RequestBody ItemRequestDto itemRequestDto
    ) {
        log.info("Got update Item id '{}' request: {}", itemId, itemRequestDto);
        return itemClient.updateItem(userId, itemId, itemRequestDto);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json;")
    public ResponseEntity<Object> delete(@RequestHeader(OWNER_ID_HOLDER) long userId, @PathVariable long id) {
        log.info("Got delete item {} request", id);
        return itemClient.delete(userId, id);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<Object> searchItemByName(@RequestHeader(OWNER_ID_HOLDER) long userId,
                                                   @RequestParam(value = "text") String text) {
        log.info("Got search Item request");
        return itemClient.searchItemByName(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(OWNER_ID_HOLDER) Long userId,
                                                @Validated @RequestBody ItemCommentRequestDto inComment,
                                                @PathVariable Long itemId) {
        log.info("Got add post request to item {} --> {} ", itemId, inComment.toString());
        return itemClient.addComment(userId, inComment, itemId);
    }
}
