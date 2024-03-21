package ru.practicum.shareit.user.service;

import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Integer id) throws NoContentException;

    UserResponseDto createUser(UserRequestDto user) throws BadRequestException, ConflictException;

    UserResponseDto updateUser(Integer id, UserRequestDto user) throws BadRequestException, NoContentException;

    void delete(Integer id) throws BadRequestException;
}
