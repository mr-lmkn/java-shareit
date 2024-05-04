package ru.practicum.shareit.Item.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ItemCommentRequestDto {
    @NotEmpty
    private String text;
}
