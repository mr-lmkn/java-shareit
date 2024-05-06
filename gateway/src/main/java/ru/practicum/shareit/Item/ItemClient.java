package ru.practicum.shareit.Item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.Item.comment.dto.ItemCommentRequestDto;
import ru.practicum.shareit.Item.dto.ItemRequestDto;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.error.exceptions.BadRequestException;

import java.util.Map;
import java.util.Optional;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getAllUserItems(
            long userId,
            Optional<Integer> from,
            Optional<Integer> size
    ) throws BadRequestException {
        Map<String, Object> parameters;
        if (from.orElse(1) < 0 || size.orElse(1) < 0) {
            String msg = "Нет такой страницы";
            throw new BadRequestException(msg);
        }
        if (from.isPresent() && size.isPresent()) {
            parameters = Map.of(
                    "from", from.get(),
                    "size", size.get()
            );
        } else {
            parameters = Map.of(
                    "from", "",
                    "size", ""
            );
        }
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getItemById(Long id, Long userId) {
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> createItem(Long userId, ItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> updateItem(Long userId, Long itemId, ItemRequestDto requestDto) {
        Map<String, Object> parameters = Map.of(
                "itemId", itemId
        );
        return patch("/{itemId}", userId, parameters, requestDto);
    }

    public ResponseEntity<Object> delete(Long userId, Long itemId) {
        return post("/" + itemId, userId);
    }

    public ResponseEntity<Object> searchItemByName(Long userId, String text) {
        return get("/search?text=" + text, userId);
    }

    public ResponseEntity<Object> addComment(Long userId, ItemCommentRequestDto inComment, Long itemId) {
        return post("/" + itemId + "/comment", userId, inComment);
    }

}
