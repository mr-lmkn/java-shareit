package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.request.dto.RequestItemRequestDto;
import ru.practicum.shareit.request.dto.RequestItemResponseDto;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@AllArgsConstructor
public class ItemRequestController {
    private final RequestService requestService;
    private final ModelMapper modelMapper;
    private static final String OWNER_ID_HOLDER = "X-Sharer-User-Id";

    @GetMapping()
    public List<RequestItemResponseDto> getAllUserRequests(@RequestHeader(OWNER_ID_HOLDER) long userId)
            throws NoContentException {
        log.info("Got all user itemRequests request");
        return requestService.getAllUserItemRequests(userId).stream()
                .map(requestItem -> modelMapper.map(requestItem, RequestItemResponseDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping("/all")
    public List<RequestItemResponseDto> getAllRequests(@RequestHeader(OWNER_ID_HOLDER) long userId,
                                                       @RequestParam(value = "from", required = false)
                                                       @Min(value = 0, message = "The value must be positive")
                                                       Optional<Integer> from,
                                                       @Positive
                                                       @RequestParam(value = "size", required = false) Optional<Integer> size
    ) throws NoContentException {
        log.info("Got all itemRequests request");
        return requestService.getAll(userId, from, size).stream()
                .map(requestItem -> modelMapper.map(requestItem, RequestItemResponseDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/{id}")
    public RequestItemResponseDto getRequest(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @PathVariable long id) throws NoContentException {
        log.info("Got itemRequest request by id");
        return modelMapper.map(requestService.getById(id, userId), RequestItemResponseDto.class);
    }

    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public RequestItemResponseDto create(
            @RequestHeader(OWNER_ID_HOLDER) long userId,
            @Validated() @RequestBody RequestItemRequestDto requestDto
    ) throws BadRequestException, ConflictException, NoContentException {
        log.info("Got itemRequest create request: {}", requestDto);
        return modelMapper.map(requestService.create(userId, requestDto), RequestItemResponseDto.class);
    }

}
