package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import ru.practicum.shareit.dtoValidateGroups.GroupCreate;
import ru.practicum.shareit.dtoValidateGroups.GroupUpdate;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {
    //целочисленный идентификатор
    @Min(value = 1, message = "id должен быть > 1")
    @Nullable()
    private Integer id;

    //электронная почта
    @Email(groups = {GroupCreate.class, GroupUpdate.class},
            regexp = "^[\\w!#$%&amp;'*+/=?`{|}~^-]+"
            + "(?:\\.[\\w!#$%&amp;'*+/=?`{|}~^-]+)*@"
            + "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$",
            message = "Поле e-mail должно содержать валидный адрес")
    @NotBlank(groups = {GroupCreate.class}, message = "Поле 'E-mail' не заполнено")
    private String email;

    // имя для отображения
    private String name;
}
