package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;


@Data
@Builder
@Entity
@Table(name = "bookings")
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
public class Booking /*implements Comparable<Booking>*/ {
    @Id
    @Column(name = "booking_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "date_from")
    private LocalDateTime start;
    @Column(name = "date_end")
    private LocalDateTime end;
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
    @ManyToOne
    @JoinColumn(name = "booker_user_id")
    private User booker;
    @Column(name = "status_id")
    private BookingStatus status;

    public Long getItemId() {
        return item.getId();
    }
/*
    @Override
    public int compareTo(Booking o) {
        if (Objects.isNull(this.getStart()) || Objects.isNull(this.getEnd())) return -1;
        if (Objects.isNull(o.getStart()) || Objects.isNull(o.getEnd())) return -1;
        if (this.getEnd().isBefore(o.getEnd())) return -1;
        if (this.getStart().equals(o.getStart())) return 0;
        return 1;
    }
 */
}
