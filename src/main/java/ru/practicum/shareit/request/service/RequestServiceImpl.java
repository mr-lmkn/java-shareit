package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.RequestItemRequestDto;
import ru.practicum.shareit.request.model.RequestItem;
import ru.practicum.shareit.request.storage.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.*;

import static java.util.stream.Collectors.*;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final ModelMapper modelMapper;

    @Override
    public List<RequestItem> getAll(Long userId,
                                    Optional<Integer> from,
                                    Optional<Integer> size
    ) throws NoContentException {
        List<User> user = Collections.singletonList(userService.getUserById(userId));
        List<RequestItem> returnItems;
        if (from.isPresent() && size.isPresent()) {
            PageRequest page = PageRequest.of(from.get(), size.get(), Sort.by("created").descending());
            returnItems = requestRepository.findAllByRequesterNotIn(user, page);
        } else {
            returnItems = requestRepository.findAllByRequesterNotInOrderByCreated(user);
        }
        returnItems = setLinkedItems(returnItems);
        return returnItems;
    }

    @Override
    public List<RequestItem> getAllUserItemRequests(Long userId) throws NoContentException {
        List<RequestItem> rList = requestRepository.findAllByRequesterOrderByCreated(userService.getUserById(userId));
        setLinkedItems(rList);
        return rList;
    }

    @Override
    public RequestItem getById(Long id, Long userId) throws NoContentException {
        User user = userService.getUserById(userId);
        Optional<RequestItem> outModel = requestRepository.findById(id);
        if (outModel.isPresent()) {
            ArrayList<Item> itemList = itemService.getAllByRequestId(id);
            outModel.get().setItems(itemList);
            return outModel.get();
        }
        throw new NoContentException("Нет вещи");
    }

    @Override
    public RequestItem create(Long userId, RequestItemRequestDto requestDto) throws NoContentException, BadRequestException {
        if (Objects.nonNull(requestDto)) {
            User user = userService.getUserById(userId);
            if (Objects.nonNull(user)) {
                RequestItem itemRequest = modelMapper.map(requestDto, RequestItem.class);
                itemRequest.setRequester(user);
                return requestRepository.save(itemRequest);
            }
            String msg = String.format("Нет пользователя с 'id' %s.", userId);
            log.info(msg);
            throw new NoContentException(msg);
        }
        String msg = String.format("Нет тела запроса", userId);
        log.info(msg);
        throw new BadRequestException(msg);
    }

    private List<RequestItem> setLinkedItems(List<RequestItem> requestItems) {
        Map<Long, List<Item>> itemsMap = itemService.getAllByRequestIdNotNull()
                .stream()
                .collect(groupingBy(Item::getRequestId, toList()));
        List<RequestItem> linkedRequests = requestItems
                .stream()
                .map(r -> r.setItemsReturn(itemsMap.get(r.getId())))
                .collect(toCollection(ArrayList::new));
        return linkedRequests;
    }
}
