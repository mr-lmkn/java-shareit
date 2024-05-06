package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestItemRequestDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@AllArgsConstructor
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    private static final String OWNER_ID_HOLDER = "X-Sharer-User-Id";

    @GetMapping()
    public ResponseEntity<Object> getAllUserRequests(@RequestHeader(OWNER_ID_HOLDER) long userId) {
        log.info("Got all user itemRequests request");
        return itemRequestClient.getAllUserItemRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        log.info("Got all itemRequests request");
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getById(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @PathVariable long id) {
        log.info("Got itemRequest request by id");
        return itemRequestClient.getById(id, userId);
    }

    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public ResponseEntity<Object> create(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @Validated() @RequestBody RequestItemRequestDto requestDto
    ) {
        log.info("Got itemRequest create request: {}", requestDto);
        return itemRequestClient.create(userId, requestDto);
    }

}
