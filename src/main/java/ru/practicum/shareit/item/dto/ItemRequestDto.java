package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import ru.practicum.shareit.dtoValidateGroups.GroupCreate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ItemRequestDto {
    @Min(value = 1, message = "id должен быть > 1")
    @Nullable
    private Long id;
    @Min(value = 1, message = "userId должен быть > 1")
    @Nullable
    private Long owner;
    @NotBlank(groups = {GroupCreate.class}, message = "Поле 'name' не заполнено")
    private String name;
    @NotBlank(groups = {GroupCreate.class}, message = "Поле 'description' не заполнено")
    private String description;
    @NotNull(groups = {GroupCreate.class}, message = "Поле 'available' не заполнено")
    private Boolean available;
    @Min(value = 1, message = "id должен быть > 1")
    @Nullable
    private Long request;
}
