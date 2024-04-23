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

    private final LocalDateTime from = LocalDateTime.now().plusDays(1);
    private final LocalDateTime to = LocalDateTime.now().plusDays(10);
    private final User user = User.builder().email("xxx@mm.eee").build();

    private final Item item = Item.builder().owner(user).name("ff").available(true).build();
    private final Booking booking = Booking.builder()
            .start(from).end(to).item(item).booker(user).status(BookingStatus.CANCELED).build();

    @Test
    void updateStatus() {
        User userX = userRepo.save(user);
        Item itemX = itemRepo.save(item);
        Booking bookingX = bookingRepository.save(booking);
        bookingRepository.updateStatus(1, bookingX.getId(), userX.getId());
        entityManager.clear();
        BookingStatus state = bookingRepository.findById(bookingX.getId()).get().getStatus();
        assertEquals(1, state.ordinal());
    }

    @Test
    void isBookingAvailable() {
        User userX = userRepo.save(user);
        Item itemX = itemRepo.save(item);
        Booking bookingX = bookingRepository.save(booking);
        entityManager.clear();
        assertEquals(true,
                bookingRepository.isBookingAvailable(itemX.getId(), from.plusDays(10), to.plusDays(11)));
    }

    @Test
    void isBookingAvailable_ERR() {
        User owner = userRepo.save(User.builder().email("d").build());
        User userX = userRepo.save(user);
        Item itemX = itemRepo.save(Item.builder().owner(owner).build());
        Booking bookingX = bookingRepository.save(
                Booking.builder().booker(userX)
                        .item(itemX)
                        .start(from).end(to)
                        .status(BookingStatus.APPROVED).build());
        entityManager.clear();
        System.out.println(bookingX.toString());
        assertEquals(false,
                bookingRepository.isBookingAvailable(itemX.getId(), from.minusDays(1), to.minusDays(5)));
    }

    @Test
    void getFromUserByStatePage() {
        User owner = userRepo.save(User.builder().email("d").build());
        User userX = userRepo.save(user);
        Item itemX = itemRepo.save(Item.builder().owner(owner).build());
        Booking b = bookingRepository.save(Booking
                .builder().booker(userX).item(itemX).status(BookingStatus.APPROVED)
                .start(from.plusDays(-100)).end(to.plusDays(-90))
                .build());
        Booking b2 = bookingRepository.save(Booking
                .builder().booker(userX).item(itemX).status(BookingStatus.APPROVED)
                .start(from.plusDays(-50)).end(to.plusDays(-40))
                .build());
        entityManager.getEntityManager().clear();
        assertEquals(1,
                bookingRepository.getFromUserByStatePage(userX.getId(), "PAST", false, 1, 1).size()
        );
    }

    @Test
    void findAllByUserBookings_ok() {
        User owner = userRepo.save(User.builder().email("d").build());
        User userX = userRepo.save(user);
        Item itemX = itemRepo.save(Item.builder().owner(owner).build());
        Booking b = bookingRepository.save(Booking
                .builder().booker(userX).item(itemX).status(BookingStatus.APPROVED)
                .start(from.plusDays(-100)).end(to.plusDays(-90))
                .build());
        Booking b2 = bookingRepository.save(Booking
                .builder().booker(userX).item(itemX).status(BookingStatus.APPROVED)
                .start(from.plusDays(-50)).end(to.plusDays(-40))
                .build());
        entityManager.getEntityManager().clear();
        System.out.println(booking.toString());
        assertEquals(2,
                bookingRepository.findAllByUserBookings(userX.getId(), itemX.getId()).size()
        );
    }

}