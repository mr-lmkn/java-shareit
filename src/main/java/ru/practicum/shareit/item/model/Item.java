package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.comment.dto.ItemCommentResponseDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@Entity
@Table(name = "items")
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @Column(name = "item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "owner_user_id")
    private User owner;
    @Column(name = "item_name")
    private String name;
    private String description;
    private Boolean available;
    @Column(name = "request_id")
    private Long request;
    @Transient
    private BookingResponseDto lastBooking;
    @Transient
    private BookingResponseDto nextBooking;
    @Transient
    private List<ItemCommentResponseDto> comments;

    public Item setCommentsReturn(List<ItemCommentResponseDto> newComments) {
        this.comments = newComments;
        return this;
    }

}