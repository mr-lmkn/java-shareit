package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.storege.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage users;
    private final ModelMapper modelMapper;

    @Override
    public List<UserResponseDto> getAllUsers() {
        log.info("Зарос всех пользователей");
        return users.getAllUsers().stream()
                .map(p -> modelMapper.map(p, UserResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto getUserById(Integer id) throws NoContentException {
        log.info("Зарос пользователя");
        return modelMapper.map(users.getUserById(id), UserResponseDto.class);
    }

    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) throws BadRequestException, ConflictException {
        log.info("Зарос создания пользователя");
        User user = modelMapper.map(userRequestDto, User.class);
        return modelMapper.map(users.createUser(user), UserResponseDto.class);
    }

    @Override
    public UserResponseDto updateUser(Integer id, UserRequestDto userRequestDto) throws BadRequestException, NoContentException {
        log.info("Зарос обновления пользователя");
        User user = modelMapper.map(userRequestDto, User.class);
        user.setId(id);
        return modelMapper.map(users.updateUser(user), UserResponseDto.class);
    }

    @Override
    public void delete(Integer id) throws BadRequestException {
        log.info("Зарос удаления пользователя");
        users.delete(id);
    }

}
