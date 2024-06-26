package ru.practicum.shareit.user.service;

import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUsers();

    User getUserById(Long id) throws NoContentException;

    UserResponseDto getUserDtoById(Long id) throws NoContentException;

    UserResponseDto createUser(UserRequestDto user) throws BadRequestException, ConflictException, NoContentException;

    UserResponseDto updateUser(Long id, UserRequestDto user) throws BadRequestException, NoContentException;

    void delete(Long id) throws BadRequestException;

    User isUserDataExist(UserRequestDto userRequestDto) throws BadRequestException;
}
