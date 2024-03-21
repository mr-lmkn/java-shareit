package ru.practicum.shareit.user.storege;

import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserStorage {
    List<User> getAllUsers();

    User getUserById(Integer id) throws NoContentException;

    User getUserByEmail(String email) throws NoContentException;

    boolean isEmailExists(String email);

    User createUser(User user) throws BadRequestException, ConflictException;

    User updateUser(User user) throws BadRequestException, NoContentException;

    void delete(Integer id) throws BadRequestException;
}
