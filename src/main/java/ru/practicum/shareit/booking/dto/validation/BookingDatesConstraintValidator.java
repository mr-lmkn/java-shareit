package ru.practicum.shareit.booking.dto.validation;

import org.springframework.beans.factory.annotation.Qualifier;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.util.Objects;

@Qualifier("BookingDatesValidator")
public class BookingDatesConstraintValidator implements ConstraintValidator<BookingDatesValidator, BookingRequestDto> {

    @Override
    public boolean isValid(BookingRequestDto bookingRequestDto, ConstraintValidatorContext context) {
        LocalDateTime dateStart = bookingRequestDto.getStart();
        LocalDateTime dateEnd = bookingRequestDto.getEnd();
        if (Objects.isNull(dateStart)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Дата начала бронирования должна быть заполнены")
                    .addPropertyNode("start")
                    .addConstraintViolation();
            return false;
        }
        if (Objects.isNull(dateEnd)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Дата конца бронирования должна быть заполнены")
                    .addPropertyNode("end")
                    .addConstraintViolation();
            return false;
        }

        if (dateEnd.isBefore(dateStart) || dateEnd.isEqual(dateStart)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Дата начала должна быть раньше дата конца периода")
                    .addPropertyNode("end")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}