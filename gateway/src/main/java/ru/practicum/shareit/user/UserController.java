package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dtoValidateGroups.GroupCreate;
import ru.practicum.shareit.dtoValidateGroups.GroupUpdate;
import ru.practicum.shareit.user.dto.UserRequestDto;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
public class UserController {
    private final UserClient userClient;

    @GetMapping()
    public ResponseEntity<Object> getAll() {
        log.info("Got all users request");
        return userClient.getAll();
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getUser(@PathVariable long id) {
        log.info("Got user request");
        return userClient.getUser(id);
    }

    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public ResponseEntity<Object> create(@Validated(GroupCreate.class) @RequestBody UserRequestDto user) {
        log.info("Got user create request: {}", user);
        return userClient.create(user);
    }

    @PatchMapping(path = "/{id}", consumes = "application/json;charset=UTF-8", produces = "application/json;")
    public ResponseEntity<Object> update(@PathVariable long id, @Validated(GroupUpdate.class) @RequestBody UserRequestDto user) {
        log.info("Got update user id '{}' request: {}", id, user);
        return userClient.updateUser(id, user);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json;")
    public ResponseEntity<Object> delete(@PathVariable long id) {
        log.info("Got delete user {} request", id);
        return userClient.delete(id);
    }

}
