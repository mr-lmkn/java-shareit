package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dtoValidateGroups.GroupCreate;
import ru.practicum.shareit.dtoValidateGroups.GroupUpdate;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
public class UserController {
    private final UserService users;
    private final ModelMapper modelMapper;

    @GetMapping()
    public List<UserResponseDto> getAll() {
        log.info("Got all users request");
        return users.getAllUsers();
    }

    @GetMapping(value = "/{id}")
    public UserResponseDto getUser(@PathVariable long id) throws NoContentException {
        log.info("Got user request");
        return users.getUserDtoById(id);
    }

    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public UserResponseDto create(@Validated(GroupCreate.class) @RequestBody UserRequestDto user)
            throws BadRequestException, ConflictException, NoContentException {
        log.info("Got user create request: {}", user);
        return users.createUser(user);
    }

    @PatchMapping(path = "/{id}", consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public UserResponseDto update(@PathVariable long id, @Validated(GroupUpdate.class) @RequestBody UserRequestDto user)
            throws BadRequestException, NoContentException {
        log.info("Got update user id '{}' request: {}", id, user);
        return users.updateUser(id, user);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json;")
    public void delete(@PathVariable long id)
            throws BadRequestException {
        log.info("Got delete user {} request", id);
        users.delete(id);
    }

}
