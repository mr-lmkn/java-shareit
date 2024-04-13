package ru.practicum.shareit.item.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ItemCommentResponseDto {
    private Long id;
    private String text;
    private String authorName;
    private LocalDateTime created;
    private Long itemId;
}
