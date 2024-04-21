package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

@EnableJpaRepositories
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Modifying
    @Query(value = " UPDATE bookings b\n"
            + "                SET status_id = :status_id\n"
            + "              WHERE booking_id = :booking_id\n"
            + "                AND EXISTS (\n"
            + "            select null \n"
            + "              from items i \n"
            + "             where i.owner_user_id = :user_id\n"
            + "                  and i.item_id = b.item_id)",
            nativeQuery = true)
    void updateStatus(
            @Param("status_id") Integer statusId,
            @Param("booking_id") Long bookingId,
            @Param("user_id") Long userId
    );

    @Query(value = "SELECT case when count(*) > 0 then false else true end "
            + " FROM bookings b"
            + " WHERE b.item_id = :item_id "
            + "   AND status_id in (0,1) " // С любым статусом ?
            + "   AND (   (    date_from BETWEEN :date_from AND :date_end "
            + "             OR date_end  BETWEEN :date_from AND :date_end )"
            + "         OR(    :date_from BETWEEN date_from AND date_end "
            + "             OR :date_end  BETWEEN date_from AND date_end )"
            + "        )",
            nativeQuery = true)
    boolean isBookingAvailable(
            @Param("item_id") long itemId,
            @Param("date_from") LocalDateTime dateFrom,
            @Param("date_end") LocalDateTime dateEnd);

    @Query(value = "SELECT b.* \n"
            + "       FROM bookings b\n"
            + "  LEFT JOIN items i \n"
            + "         ON i.item_id = b.item_id  \n"
            + "      WHERE (    (b.booker_user_id = :user_id and :owner_Only = false)\n"
            + "              OR (i.owner_user_id  = :user_id and :owner_Only = true)) \n"
            + "        AND (   :state = 'ALL' \n"
            + "              OR ( :state = 'CURRENT'  and now() BETWEEN b.date_from AND b.date_end ) \n"
            + "              OR ( :state = 'FUTURE'   and now() < b.date_from ) \n"
            + "              OR ( :state = 'PAST'     and now() > b.date_end ) \n"
            + "              OR ( :state = 'WAITING'  and b.status_id = 0 ) \n"
            + "              OR ( :state = 'REJECTED' and b.status_id = 2)  \n"
            + "            ) \n"
            + "      ORDER BY date_end DESC",
            nativeQuery = true)
    List<Booking> getFromUserByState(
            @Param("user_id") long userId,
            @Param("state") String state,
            @Param("owner_Only") Boolean ownerOnly
    );

    @Query(value = "SELECT b.* \n"
            + "       FROM bookings b\n"
            + "  LEFT JOIN items i \n"
            + "         ON i.item_id = b.item_id  \n"
            + "      WHERE (    (b.booker_user_id = :user_id and :owner_Only = false)\n"
            + "              OR (i.owner_user_id  = :user_id and :owner_Only = true)) \n"
            + "        AND (   :state = 'ALL' \n"
            + "              OR ( :state = 'CURRENT'  and now() BETWEEN b.date_from AND b.date_end ) \n"
            + "              OR ( :state = 'FUTURE'   and now() < b.date_from ) \n"
            + "              OR ( :state = 'PAST'     and now() > b.date_end ) \n"
            + "              OR ( :state = 'WAITING'  and b.status_id = 0 ) \n"
            + "              OR ( :state = 'REJECTED' and b.status_id = 2)  \n"
            + "            ) \n"
            + "      ORDER BY date_end DESC"
            + " LIMIT :size OFFSET :from",
            nativeQuery = true)
    List<Booking> getFromUserByStatePage(
            @Param("user_id") long userId,
            @Param("state") String state,
            @Param("owner_Only") Boolean ownerOnly,
            @Param("from") Integer from,
            @Param("size") Integer size
    );

    @Query(value = "SELECT b.* \n "
            + "      FROM bookings as b \n "
            + "      JOIN items as i "
            + "        ON i.item_id = b.item_id \n "
            + "     WHERE b.booker_user_id = :user_id \n "
            + "       AND i.item_id = :item_id \n "
            + "       AND b.status_id = 1 \n "
            + "       AND b.date_end < :date_x \n ",
            nativeQuery = true)
    List<Booking> findAllByUserBookings(
            @Param("user_id") long userId,
            @Param("item_id") long itemId,
            @Param("date_x") LocalDateTime dateX
    );

    List<Booking> findAllByItemInAndStatusOrderByStartAsc(List<Item> item, BookingStatus status);

    List<Booking> findAllByItemAndStatusOrderByEndAsc(Item item, BookingStatus status);

    @Query(value = "WITH m AS ("
            + " SELECT item_id, max(date_end) AS max_end "
            + "    FROM bookings "
            + "   WHERE item_id = :item_id "
            + "       AND status_id = 1 \n "
            + "       AND date_end < now() \n "
            + "   GROUP BY item_id "
            + ")"
            + "SELECT b.* \n "
            + "  FROM bookings as b JOIN m ON b.item_id = m.item_id AND b.date_end = m.max_end\n ",
            nativeQuery = true)
    Booking getLastPrevByItemId(
            @Param("item_id") long itemId
    );

    @Query(value = "WITH m AS ("
            + " SELECT item_id, min(date_end) AS min_start "
            + "    FROM bookings "
            + "   WHERE item_id = :item_id "
            + "       AND status_id = 1 \n "
            + "       AND date_from > now() \n "
            + "   GROUP BY item_id "
            + ")"
            + "SELECT b.* \n "
            + "  FROM bookings as b JOIN m ON b.item_id = m.item_id AND b.date_from = m.min_start \n ",
            nativeQuery = true)
    Booking getFirstNextByItemId(
            @Param("item_id") long itemId
    );
}
