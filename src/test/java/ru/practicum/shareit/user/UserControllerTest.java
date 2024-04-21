package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storege.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@SpringJUnitWebConfig()
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Mock
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ModelMapper modelMapper;

    private final ObjectMapper mapper = new ObjectMapper();
    private final UserRequestDto requestUser1 = UserRequestDto.builder()
            .email("mail@mail.ru")
            .name("user1")
            .build();

    private final UserResponseDto responseUser1 = UserResponseDto.builder()
            .id(1L)
            .email("mail@mail.ru")
            .name("user1")
            .build();

    private final User user1 = User.builder()
            .email("mail@mail.ru")
            .name("user1")
            .build();


    @Test
    void create_ok() throws Exception {
        ArrayList<User> emptyUserList = new ArrayList<User>();
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(requestUser1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(responseUser1.getName())))
                .andExpect(jsonPath("$.email", is(responseUser1.getEmail())));
    }

    @Test
    void create_wrong_email_err() throws Exception {
        UserRequestDto requestUser2 = UserRequestDto.builder()
                .email("cccc")
                .name("ffff")
                .build();

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(requestUser2))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void create_double_email_err() throws Exception {
        UserRequestDto requestUser2 = UserRequestDto.builder()
                .email("cccc@mail.ru")
                .name("ffff")
                .build();

        mvc.perform(post("/users")
                .content(mapper.writeValueAsString(requestUser2))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        requestUser2.setName("ddd");

        Exception exception = assertThrows(org.springframework.web.util.NestedServletException.class, () -> {
                    mvc.perform(post("/users")
                                    .content(mapper.writeValueAsString(requestUser2))
                                    .characterEncoding(StandardCharsets.UTF_8)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isInternalServerError());
                }
        );
    }

    @Test
    void create_no_email_err() throws Exception {
        UserRequestDto requestUser2 = UserRequestDto.builder()
                .email("cccc")
                .build();

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(requestUser2))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void create_no_name_err() throws Exception {
        UserRequestDto requestUser2 = UserRequestDto.builder()
                .email("cccc@mail.ru")
                .build();
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(requestUser2))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void delete_ok() throws Exception {
        UserRequestDto requestUser2 = UserRequestDto.builder()
                .email("delete@mail.ru")
                .name("to_delete")
                .build();
        User u = userService.createUser(requestUser2);

        mvc.perform(delete("/users/" + u.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

}