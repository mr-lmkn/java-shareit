package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storege.UserRepository;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private EntityManager entityManager;

    private Item item1Value;
    private Item item2Value;
    private final User user = User.builder().id(1L).email("aaa.@rr.rr").build();

    @BeforeEach
    private void fillDb() {
        User xUser = userRepo.save(user);
        Item item = Item.builder().name("mmmm").available(true).owner(xUser).build();
        Item item2 = Item.builder().description("aaa").available(true).owner(xUser).build();
        item1Value = itemRepo.save(item);
        item2Value = itemRepo.save(item2);
    }

    @Test
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    void partialUpdate() {
        Item itemXValue = itemRepo.save(item1Value);
        long userId = itemXValue.getOwner().getId();
        long id = itemXValue.getId();
        int updated = itemRepo.partialUpdate(
                "name",
                "description",
                false,
                id,
                userId);
        assertEquals(1, updated);
        entityManager.clear();
        Item updatedItem = itemRepo.findById(id).get();
        System.out.println(updatedItem.getId() + updatedItem.getName());
        assertEquals("name", updatedItem.getName());
        assertEquals("description", updatedItem.getDescription());
        assertEquals(false, updatedItem.getAvailable());
    }

    @Test
    void findUserItemLike_ok() {
        List waitOut = List.of(item1Value, item2Value);
        assertEquals(waitOut.size(), itemRepo.findUserItemLike("m", "a").size());
    }

    @Test
    void findUserItemLike_err() {
        List waitOut = List.of(item1Value, item2Value);
        assertEquals(0, itemRepo.findUserItemLike("x", "b").size());
    }

    @Test
    void findUserItemLikePage_ok() {
        List waitOut = List.of(item1Value, item2Value);
        assertEquals(1, itemRepo.findUserItemLikePage("m", "a", 1, 1).size());
    }

    @Test
    void findUserItemLikePage_err() {
        List waitOut = List.of(item1Value, item2Value);
        assertEquals(0, itemRepo.findUserItemLikePage("x", "b", 1, 1).size());
    }
}