package ru.practicum.shareit.user.service;


import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storege.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<String> emailArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> nameArgumentCaptor;
    @Captor
    private ArgumentCaptor<Long> idArgumentCaptor;

    private User user;
    private UserRequestDto userRequestDto;
    private final long userId = 1L;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .email("xmail@mail.ru")
                .name("User-name")
                .build();
        userRequestDto = UserRequestDto.builder()
                .id(userId)
                .email("xmail@mail.ru")
                .name("User-name")
                .build();
    }

    @Test
    @SneakyThrows
    void getUserById_ok() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        User ret = userService.getUserById(userId);
        assertEquals(user, ret);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_noUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(NoContentException.class, () -> userService.getUserById(userId));
    }

    @Test
    void create_ok() throws Exception {
        ArrayList<User> emptyUserList = new ArrayList<User>();
        when(userRepository.findByEmailContainingIgnoreCase(anyString())).thenReturn(emptyUserList);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(userRequestDto, User.class)).thenReturn(user);
        User ret = userService.createUser(userRequestDto);
        assertEquals(user, ret);
        verify(userRepository).save(user);
    }

    @Test
    @SneakyThrows
    void create_err() {
        assertThrows(BadRequestException.class, () -> userService.createUser(null));
        verify(userRepository, never()).save(user);
    }

    @Test
    @SneakyThrows
    void update_err() {
        assertThrows(BadRequestException.class, () -> userService.updateUser(userId, null));
        verify(userRepository, never()).partialUpdate(user.getEmail(), user.getName(), userId);
    }

    @Test
    void isUserDataExist_err() {
        assertThrows(BadRequestException.class, () -> userService.isUserDataExist(null));
    }

    @Test
    @SneakyThrows
    void update_ok() {
        User user1 = User.builder()
                .id(userId)
                .email("x@mail.ru")
                .name("name")
                .build();
        UserRequestDto userRequestDto1 = UserRequestDto.builder()
                .id(userId)
                .email("x@mail.ru")
                .name("name")
                .build();
        when(userService.isUserDataExist(userRequestDto1)).thenReturn(user1);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        userService.updateUser(userId, userRequestDto1);

        verify(userRepository)
                .partialUpdate(emailArgumentCaptor.capture(), nameArgumentCaptor.capture(), idArgumentCaptor.capture());
        String email = emailArgumentCaptor.getValue();
        String name = nameArgumentCaptor.getValue();
        Long id = idArgumentCaptor.getValue();
        assertEquals(user1.getEmail(), email);
        assertEquals(user1.getName(), name);
        assertEquals(user1.getId(), id);
    }

    @Test
    @SneakyThrows
    void getAllUsers_ok() {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        when(userRepository.findAll(sortById)).thenReturn(List.of(new User()));
        assertEquals(1, userService.getAllUsers().size());
    }
}