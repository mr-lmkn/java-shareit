package ru.practicum.shareit.item.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCommentRequestDto {
    @NotEmpty
    private String text;
}
