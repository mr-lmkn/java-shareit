package ru.practicum.shareit.user.storege;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;

@EnableJpaRepositories
public interface UserRepository extends JpaRepository<User, Long> {
    @Modifying
    @Query("UPDATE User u SET \n"
            + " u.email = CASE WHEN ?1 is not null THEN ?1 ELSE email END, \n"
            + " u.name = CASE WHEN ?2 is not null THEN ?2 ELSE user_name END \n"
            + " WHERE u.id = ?3")
    int partialUpdate(String email, String name, long id);

    ArrayList<User> findByEmailContainingIgnoreCase(String emailSearch);
}
