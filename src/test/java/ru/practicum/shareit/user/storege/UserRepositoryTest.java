package ru.practicum.shareit.user.storege;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void partialUpdate() {
        User userX = userRepo.save(User.builder().email("fff@rrr.rr").build());
        userRepo.partialUpdate("String email", "String name", userX.getId());
        entityManager.getEntityManager().clear();
        assertEquals(1,
                userRepo.findByEmailContainingIgnoreCase("String email").size()
        );
    }

}