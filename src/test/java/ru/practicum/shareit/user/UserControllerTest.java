package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private UserController userController;

    private User user;
    private UserRequestDto userReq;
    private UserResponseDto userResp;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("xmail@mail.ru")
                .name("User-name")
                .build();
        userReq = UserRequestDto.builder().id(1L).email("xmail@mail.ru").name("User-name").build();
        userResp = UserResponseDto.builder().id(1L).email("xmail@mail.ru").name("User-name").build();
    }

    @Test
    void getAll() {
        List<UserResponseDto> sample = List.of(userResp);
        when(userService.getAllUsers()).thenReturn(sample);

        List<UserResponseDto> reply = userController.getAll();
        assertEquals(sample.size(), reply.size());
    }

    @Test
    void create_ok() throws ConflictException, BadRequestException, NoContentException {
        when(userService.createUser(userReq)).thenReturn(userResp);
        UserResponseDto reply = userController.create(userReq);
        assertEquals(userResp, reply);
    }

    @Test
    void update_ok() throws ConflictException, BadRequestException, NoContentException {
        when(userService.createUser(userReq)).thenReturn(userResp);
        userController.create(userReq);

        User updatedUser = User.builder()
                .id(1L)
                .email("some@mail.com")
                .name("some")
                .build();
        UserRequestDto requset = modelMapper.map(updatedUser, UserRequestDto.class);
        UserResponseDto response = modelMapper.map(updatedUser, UserResponseDto.class);
        UserResponseDto reply = userController.update(1, requset);

        assertEquals(response, reply);
    }

}