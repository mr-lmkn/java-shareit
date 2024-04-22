package ru.practicum.shareit.booking.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storege.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private ItemRepository itemRepo;
    @Autowired
    private TestEntityManager entityManager;

    private final long id = 1L;
    private final long id2 = 2L;
    private final LocalDateTime from = LocalDateTime.now().plusDays(1);
    private final LocalDateTime to = LocalDateTime.now().plusDays(2);
    private final User user = User.builder().id(id).email("xxx@mm.eee").build();

    private final Item item = Item.builder().id(id).owner(user).name("ff").available(true).build();
    private final Booking booking = Booking.builder().id(id)
            .start(from).end(to).item(item).booker(user).status(BookingStatus.CANCELED).build();

    @Test
    void updateStatus() {
        userRepo.save(user);
        itemRepo.save(item);
        bookingRepository.save(booking);
        bookingRepository.updateStatus(1, id, id);
        entityManager.clear();
        BookingStatus state = bookingRepository.findById(id).get().getStatus();
        assertEquals(1, state.ordinal());
    }

    @Test
    void isBookingAvailable() {
        userRepo.save(user);
        Item itemX = itemRepo.save(item);
        bookingRepository.save(booking);
        entityManager.clear();
        assertEquals(true,
                bookingRepository.isBookingAvailable(itemX.getId(), from.plusDays(10), to.plusDays(11)));
    }

    @Test
    void isBookingAvailable_ERR() {
        userRepo.save(user);
        Item itemX = itemRepo.save(item);
        bookingRepository.save(booking);
        bookingRepository.updateStatus(1, itemX.getId(), id);
        entityManager.getEntityManager().clear();
        assertEquals(false,
                bookingRepository.isBookingAvailable(itemX.getId(), from, to));
    }


    @Test
    void getFromUserByStatePage() {
        User owner = userRepo.save(User.builder().email("d").build());
        User userX = userRepo.save(user);
        Item itemX = itemRepo.save(Item.builder().owner(owner).build());
        Booking b = bookingRepository.save(Booking
                .builder().booker(userX).item(itemX).status(BookingStatus.APPROVED)
                .start(from.plusDays(-10)).end(to.plusDays(-9))
                .build());
        Booking b2 = bookingRepository.save(Booking
                .builder().booker(userX).item(itemX).status(BookingStatus.APPROVED)
                .start(from.plusDays(-5)).end(to.plusDays(-4))
                .build());
        entityManager.getEntityManager().clear();
        System.out.println(booking.toString());
        assertEquals(1,
                bookingRepository.getFromUserByStatePage(userX.getId(),"PAST", false, 1,1).size()
        );
    }

    @Test
    void findAllByUserBookings_ok() {
        User owner = userRepo.save(User.builder().email("d").build());
        User userX = userRepo.save(user);
        Item itemX = itemRepo.save(Item.builder().owner(owner).build());
        Booking b = bookingRepository.save(Booking
                .builder().booker(userX).item(itemX).status(BookingStatus.APPROVED)
                .start(from.plusDays(-10)).end(to.plusDays(-9))
                .build());
        Booking b2 = bookingRepository.save(Booking
                .builder().booker(userX).item(itemX).status(BookingStatus.APPROVED)
                .start(from.plusDays(-5)).end(to.plusDays(-4))
                .build());
        entityManager.getEntityManager().clear();
        System.out.println(booking.toString());
        assertEquals(2,
         bookingRepository.findAllByUserBookings(userX.getId(),itemX.getId()).size()
        );
    }

}