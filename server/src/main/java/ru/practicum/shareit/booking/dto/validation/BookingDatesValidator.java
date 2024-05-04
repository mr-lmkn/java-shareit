package ru.practicum.shareit.booking.dto.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = BookingDatesConstraintValidator.class)
public @interface BookingDatesValidator {
    String message() default "Дата начала не может быть меньше даты конца периода бронирования";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
