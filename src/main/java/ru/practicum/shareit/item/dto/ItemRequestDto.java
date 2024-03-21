package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import ru.practicum.shareit.dtoValidateGroups.GroupCreate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    @Min(value = 1, message = "id должен быть > 1")
    @Nullable
    private Integer id;
    @Min(value = 1, message = "userId должен быть > 1")
    @Nullable
    private Integer owner;
    @NotBlank(groups = {GroupCreate.class}, message = "Поле 'name' не заполнено")
    private String name;
    @NotBlank(groups = {GroupCreate.class}, message = "Поле 'description' не заполнено")
    private String description;
    @NotNull(groups = {GroupCreate.class}, message = "Поле 'available' не заполнено")
    private Boolean available;
    @Min(value = 1, message = "id должен быть > 1")
    @Nullable
    private Integer request;
}
