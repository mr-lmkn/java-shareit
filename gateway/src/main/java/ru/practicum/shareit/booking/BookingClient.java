package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingRequestStatus;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.error.exceptions.BadRequestException;

import java.util.Map;
import java.util.Optional;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getBookings(
            long userId,
            BookingRequestStatus state,
            Optional<Integer> from,
            Optional<Integer> size,
            Boolean fromOwner
    ) throws BadRequestException {
        String pathPrefix = "";
        Map<String, Object> parameters;

        if (fromOwner) {
            pathPrefix = "/owner";
        }

        if (from.orElse(1) <= 0 || size.orElse(1) <= 0) {
            String msg = "Нет такой страницы";
            throw new BadRequestException(msg);
        }

        if (from.isPresent() && size.isPresent()) {
            parameters = Map.of(
                    "state", state.name(),
                    "from", from.get(),
                    "size", size.get()
            );
        } else {
            parameters = Map.of(
                    "state", state.name(),
                    "from", "",
                    "size", ""
            );
        }
        return get(pathPrefix + "?state={state}&from={from}&size={size}", userId, parameters);

    }


    public ResponseEntity<Object> add(long userId, BookItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getFromBookerOrOwner(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> setState(long userId, Long bookingId, String approved) {
        return patch("/" + bookingId + "?approved=" + approved, userId, approved);
    }

    public ResponseEntity<Object> getFromUser(Long userId,
                                              BookingRequestStatus state,
                                              Optional<Integer> from,
                                              Optional<Integer> size) {
        if (from.isPresent() && size.isPresent()) {
            Map<String, Object> parameters = Map.of(
                    "state", state.name(),
                    "from", from.get(),
                    "size", size.get()
            );
            return get("", userId, parameters);
        } else return get("", userId);
    }

    public ResponseEntity<Object> getFromOwner(Long userId,
                                               BookingRequestStatus state,
                                               Optional<Integer> from,
                                               Optional<Integer> size) {
        if (from.isPresent() && size.isPresent()) {
            Map<String, Object> parameters = Map.of(
                    "state", state.name(),
                    "from", from.get(),
                    "size", size.get()
            );
            return get("/owner", userId, parameters);
        } else return get("/owner", userId);
    }
}
