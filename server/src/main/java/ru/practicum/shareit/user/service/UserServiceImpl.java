package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storege.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final ModelMapper modelMapper;

    @Override
    public List<UserResponseDto> getAllUsers() {
        log.info("Зарос всех пользователей");
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        return repository.findAll(sortById).stream()
                .map(p -> modelMapper.map(p, UserResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public User getUserById(Long id) throws NoContentException {
        log.info("Зарос пользователя");
        Optional<User> user = repository.findById(id);
        if (user.isPresent()) {
            return user.get();
        }
        String msg = String.format("Нет пользователя с 'id' %s.", id);
        log.info(msg);
        throw new NoContentException(msg);
    }

    @Override
    @Transactional
    public UserResponseDto getUserDtoById(Long id) throws NoContentException {
        return modelMapper.map(getUserById(id), UserResponseDto.class);
    }

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) throws BadRequestException {
        log.info("Зарос создания пользователя");
        User user = isUserDataExist(userRequestDto);
        ArrayList<User> usersSameEmail = repository.findByEmailContainingIgnoreCase(user.getEmail());
        return modelMapper.map(repository.save(user), UserResponseDto.class);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto)
            throws BadRequestException, NoContentException {
        log.info("Зарос обновления пользователя");
        User user = isUserDataExist(userRequestDto);
        user.setId(id);
        repository.partialUpdate(user.getEmail(), user.getName(), user.getId());
        return modelMapper.map(getUserById(id), UserResponseDto.class);
    }

    @Override
    @Transactional
    public void delete(Long id) throws BadRequestException {
        log.info("Зарос удаления пользователя");
        repository.deleteAllById(Collections.singleton(id));
    }

    @Override
    @Transactional
    public User isUserDataExist(UserRequestDto userRequestDto) throws BadRequestException {
        if (Objects.nonNull(userRequestDto)) {
            return modelMapper.map(userRequestDto, User.class);
        }
        String msg = String.format("Тело запроса не содержит данных");
        log.info(msg);
        throw new BadRequestException(msg);
    }

}
