package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.comment.dto.ItemCommentRequestDto;
import ru.practicum.shareit.item.comment.dto.ItemCommentResponseDto;
import ru.practicum.shareit.item.comment.model.ItemComment;
import ru.practicum.shareit.item.comment.storage.CommentRepository;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.RequestItem;
import ru.practicum.shareit.request.storage.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static ru.practicum.shareit.booking.enums.BookingStatus.APPROVED;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final CommentRepository commentsRepository;
    private final BookingRepository bookingRepository;
    private final RequestRepository requestRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public List<ItemResponseDto> getAllUserItems(Long ownerId, Optional<Integer> from, Optional<Integer> size)
            throws NoContentException {
        ArrayList<Item> itemsList;
        if (from.isPresent() && size.isPresent()) {
            PageRequest page = PageRequest.of(from.get(), size.get(), Sort.by("created").descending());
            itemsList = itemRepository.findAllByOwner(userService.getUserById(ownerId), page);
        } else {
            itemsList = itemRepository.findAllByOwnerOrderByIdAsc(userService.getUserById(ownerId));
        }

        List<Long> idList = itemsList.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        Map<Long, List<ItemCommentResponseDto>> comments = commentsRepository
                .findAllByItemIdIn(idList)
                .stream()
                .filter(Objects::nonNull)
                .map(c -> modelMapper.map(c, ItemCommentResponseDto.class))
                .collect(groupingBy(ItemCommentResponseDto::getItemId));

        Map<Long, List<Booking>> bookings = bookingRepository
                .findAllByItemInAndStatusOrderByStartAsc(itemsList, APPROVED)
                .stream()
                .collect(groupingBy(Booking::getItemId, toList()));

        bookings.entrySet().stream()
                .forEach(e -> log.debug(" All bookings list ---> {}", Arrays.toString(e.getValue().toArray())));

        return itemsList
                .stream()
                .map(item -> item.setCommentsReturn(comments.get(item.getId())))
                .map(item -> setBookingsDto(item, bookings.get(item.getId())))
                .map(item -> modelMapper.map(item, ItemResponseDto.class))
                .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public Item getItemById(Long id, Long userId) throws NoContentException {
        Optional<Item> items = itemRepository.findById(id);
        if (items.isEmpty()) {
            String msg = String.format("Нет вещи с 'id' %s.", id);
            log.info(msg);
            throw new NoContentException(msg);
        }
        return items.get();
    }

    @Override
    public ItemResponseDto getItemDtoById(Long id, Long userId) throws NoContentException {
        Item ret = getItemById(id, userId);
        boolean isOwner = Objects.equals(ret.getOwner().getId(), userId);
        List<ItemCommentResponseDto> comments = getAllItemComments(id);
        ret.setComments(comments);
        if (isOwner) {
            List<Booking> bookings = bookingRepository
                    .findAllByItemAndStatusOrderByEndAsc(ret, APPROVED);
            ret = setBookingsDto(ret, bookings);
            log.info("----> add bookings {} ", ret.toString());
        }
        return modelMapper.map(ret, ItemResponseDto.class);
    }

    @Override
    public ArrayList<Item> getAllByRequestId(Long requestId) {
        return itemRepository.findAllByRequestId(requestId);
    }

    @Override
    public ArrayList<Item> getAllByRequestIdNotNull() {
        return itemRepository.findAllByRequestIdNotNull();
    }

    @Override
    @Transactional
    public Item setBookingsDto(Item item, List<Booking> bookings) {
        log.info(" setBookingsDto ---> ComeIn ");
        if (Objects.isNull(bookings)) return item;
        LocalDateTime now = LocalDateTime.now();

        List<Booking> filteredBefore = bookings.stream().filter(c -> c.getStart().isBefore(now))
                .collect(toCollection(ArrayList::new));
        List<Booking> filterdAfter = bookings.stream().filter(c -> c.getStart().isAfter(now))
                .collect(toCollection(ArrayList::new));

        bookings.stream().forEach(e -> log.info(" DatesList ---> {} {} {}",
                e.getStart(), now, e.getStart().isAfter(now)));

        Booking lastBooking = filteredBefore.stream()
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);
        Booking nextBooking = filterdAfter.stream()
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        if (Objects.nonNull(lastBooking))
            item.setLastBooking(modelMapper.map(lastBooking, BookingResponseDto.class));
        if (Objects.nonNull(nextBooking))
            item.setNextBooking(modelMapper.map(nextBooking, BookingResponseDto.class));
        return item;
    }

    @Override
    @Transactional
    public ItemResponseDto createItem(Long userId, ItemRequestDto itemRequestDto)
            throws BadRequestException, NoContentException {
        if (Objects.nonNull(itemRequestDto)) {
            User user = userService.getUserById(userId);
            if (Objects.nonNull(user)) {
                log.info(" Запрос на создание вещи: {}", itemRequestDto.toString());
                Item item = modelMapper.map(itemRequestDto, Item.class);
                item.setOwner(user);
                if (Objects.nonNull(itemRequestDto.getRequestId())) {
                    long requestId = itemRequestDto.getRequestId();
                    RequestItem requestItem = requestRepository.getById(requestId);
                    String msg = String.format(" Создается вещь по запросу %s", requestId);
                    log.info(msg);
                    item.setRequestId(requestId);
                }
                return modelMapper.map(itemRepository.save(item), ItemResponseDto.class);
            }
            String msg = String.format("Нет пользователя с 'id' %s.", userId);
            log.info(msg);
            throw new NoContentException(msg);
        }
        String msg = String.format("Нет тела запроса от %s", userId);
        log.info(msg);
        throw new BadRequestException(msg);
    }

    @Override
    @Transactional
    public ItemResponseDto updateItem(Long userID, Long itemId, ItemRequestDto itemRequestDto)
            throws BadRequestException, NoContentException {
        if (Objects.nonNull(itemId) && itemId > 0) {
            Item item = modelMapper.map(itemRequestDto, Item.class);
            int updaterRows = itemRepository.partialUpdate(
                    item.getName(),
                    item.getDescription(),
                    item.getAvailable(),
                    itemId,
                    userID);
            if (updaterRows > 0) {
                log.info("Операция выполнена уcпешно");
                return modelMapper.map(itemRepository.findById(itemId).get(), ItemResponseDto.class);
            } else {
                String msg = String
                        .format("Нет вещи с 'id' %s или вещь не принадлежит пользователю. Обновление не возможно.",
                                itemId);
                log.info(msg);
                throw new NoContentException(msg);
            }
        }
        String msg = String.format("Не указан 'id' %s. Обновление не возможно.", itemId);
        log.info(msg);
        throw new BadRequestException(msg);
    }

    @Override
    @Transactional
    public void delete(Long userid, Long id) throws BadRequestException {
        itemRepository.deleteAllById(Collections.singleton(id));
    }

    @Override
    @Transactional
    public List<ItemResponseDto> searchItemByName(
            Long userId, String text,
            Optional<Integer> from,
            Optional<Integer> size
    ) {
        List<Item> itemList = new ArrayList<>();
        if (!text.isEmpty() && (from.isEmpty() || size.isEmpty())) {
            itemList = itemRepository.findUserItemLike(text, text);
        } else if (!text.isEmpty() && from.isPresent() && size.isPresent()) {
            itemList = itemRepository.findUserItemLikePage(text, text, from.get(), size.get());
        }

        return itemList.stream()
                .map(item -> modelMapper.map(item, ItemResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemCommentResponseDto addComment(Long userId, ItemCommentRequestDto inComment, Long itemId)
            throws NoContentException, BadRequestException {
        User user = userService.getUserById(userId);
        Item item = getItemById(itemId, userId);

        ItemComment itemComment = modelMapper.map(inComment, ItemComment.class);
        itemComment.setItem(item);
        itemComment.setAuthor(user);

        List<Booking> userBookings = bookingRepository.findAllByUserBookings(userId, itemId/*, LocalDateTime.now()*/);
        if (userBookings.isEmpty()) {
            String msg = String.format("У user_id = {%s} нечего комментировать", userId);
            log.info(msg);
            throw new BadRequestException(msg);
        }
        return modelMapper.map(commentsRepository.save(itemComment), ItemCommentResponseDto.class);
    }

    @Override
    @Transactional
    public List<ItemCommentResponseDto> getAllItemComments(Long itemId) {
        return commentsRepository.findAllByItemId(itemId)
                .stream()
                .map(itemComment -> modelMapper.map(itemComment, ItemCommentResponseDto.class))
                .collect(Collectors.toList());
    }

}