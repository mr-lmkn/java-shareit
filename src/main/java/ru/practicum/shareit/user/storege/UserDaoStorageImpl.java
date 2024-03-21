package ru.practicum.shareit.user.storege;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.user.User;

import java.util.*;


@Slf4j
@Repository
@AllArgsConstructor
public class UserDaoStorageImpl implements UserStorage {
    private final JdbcTemplate dataSource;

    @Override
    public List<User> getAllUsers() {
        ArrayList<User> users = new ArrayList<User>();
        SqlRowSet userRows = dataSource.queryForRowSet("SELECT * FROM users");
        while (userRows.next()) {
            User user = mapUserRow(userRows);
            users.add(user);
            log.info("Найден пользователь: {} {}", user.getId(), user.getName());
        }
        log.info("Конец списка пользователей");
        return users;
    }

    @Override
    public User getUserById(Integer id) throws NoContentException {
        SqlRowSet userRows = dataSource.queryForRowSet("SELECT * FROM users WHERE user_id = ?", id);
        if (userRows.next()) {
            User user = mapUserRow(userRows);
            log.info("Найден пользователь: {} {}", user.getId(), user.getName());
            return user;
        } else {
            String msg = String.format("Нет пользователя с 'id'=%s.", id);
            log.info(msg);
            throw new NoContentException(msg);
        }
    }

    @Override
    public User getUserByEmail(String email) throws NoContentException {
        SqlRowSet userRows = dataSource.queryForRowSet("SELECT * FROM users WHERE email = ?", email);
        if (userRows.next()) {
            User user = mapUserRow(userRows);
            log.info("Найден пользователь: {} {}", user.getId(), user.getName());
            return user;
        } else {
            String msg = String.format("Нет пользователя с 'email'=%s.", email);
            log.info(msg);
            throw new NoContentException(msg);
        }
    }

    @Override
    public boolean isEmailExists(String email) {
        try {
            getUserByEmail(email);
            return false;
        } catch (NoContentException e) {
            return true;
        }
    }

    @Override
    public User createUser(User user) throws BadRequestException, ConflictException {
        if (isEmailExists(user.getEmail())) {
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                    .withTableName("users")
                    .usingGeneratedKeyColumns("user_id");
            Map<String, Object> parameters = mapUserQueryParameters(user);
            Integer id = simpleJdbcInsert.executeAndReturnKey(parameters).intValue();
            log.info("Generated id - " + id);
            user.setId(id);
            return user;
        } else {
            throw new ConflictException("Пользователь с таким e-mail уже существует.");
        }
    }

    @Override
    public User updateUser(User user) throws BadRequestException, NoContentException {
        Integer id = user.getId();
        String doDo = "обновление пользователя";
        log.info("Инициировано {} {}", doDo, id);

        if (Objects.nonNull(id) && id > 0) {
            Integer updaterRows = dataSource.update(
                    "UPDATE USERS SET "
                            + "     email = CASE WHEN ? is not null THEN ? ELSE email END,"
                            + " user_name = CASE WHEN ? is not null THEN ? ELSE user_name END "
                            + " WHERE USER_ID = ?",
                    user.getEmail(),
                    user.getEmail(),
                    user.getName(),
                    user.getName(),
                    id);

            if (updaterRows > 0) {
                log.info("Операция {} выполнена уcпешно", doDo);
                return getUserById(id);
            } else {
                String msg = String.format("Нет пользователя с 'id' %s. Обновление не возможно.", id);
                log.info(msg);
                throw new NoContentException(msg);
            }
        }

        String msg = String.format("Не указан 'id' %s. Обновление не возможно.", id);
        log.info(msg);
        throw new BadRequestException(msg);

    }

    @Override
    public void delete(Integer userId) throws BadRequestException {
        String doDo = " удаление пользователя";
        log.info("Инициировано {} {}", doDo, userId);
        String msg;

        if (Objects.nonNull(userId)) {
            Integer deleteUserRows = dataSource.update(
                    "DELETE FROM users WHERE user_id = ?",
                    userId);

            if (deleteUserRows > 0) {
                log.info("Пользователь {} удален", userId);
                return;
            } else {
                msg = String.format("Нет пользователя с ID %s", userId);
                log.info(msg);
                throw new BadRequestException(msg);
            }
        }

        msg = String.format("Не указан 'id' %s. Удаление не возможно.", userId);
        log.info(msg);
        throw new BadRequestException(msg);
    }

    private User mapUserRow(SqlRowSet userRows) {
        Integer userId = userRows.getInt("user_id");

        return new User(
                userId,
                userRows.getString("email"),
                userRows.getString("user_name")
        );
    }

    private Map<String, Object> mapUserQueryParameters(User user) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("user_id", user.getId());
        parameters.put("email", user.getEmail());
        parameters.put("user_name", user.getName());
        return parameters;
    }

}
