package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
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
    private final EntityManager entityManager;
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final CommentRepository commentsRepository;
    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public List<Item> getAllUserItems(Long ownerId) throws NoContentException {
        ArrayList<Item> itemsList = itemRepository.findAllByOwnerOrderByIdAsc(userService.getUserById(ownerId));

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
                .map(item -> setBookings(item, bookings.get(item.getId())))
                .collect(toList());

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
        Item ret = items.get();
        boolean isOwner = Objects.equals(ret.getOwner().getId(), userId);

        List<ItemCommentResponseDto> comments = getAllItemComments(id)
                .stream()
                .map(itemComment -> modelMapper.map(itemComment, ItemCommentResponseDto.class))
                .collect(Collectors.toList());
        ret.setComments(comments);

        if (isOwner) {
            List<Booking> bookings = bookingRepository
                    .findAllByItemAndStatusOrderByEndAsc(ret, APPROVED);
            ret = setBookings(ret, bookings);
            log.info("----> add bookings {} ", ret.toString());
        }

        return ret;
    }

    @Override
    @Transactional
    public Item setBookings(Item item, List<Booking> bookings) {
        log.info(" setBookings ---> ComeIn ");
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
    public Item createItem(Long userID, ItemRequestDto itemRequestDto)
            throws BadRequestException, NoContentException {
        if (Objects.nonNull(itemRequestDto)) {
            User user = userService.getUserById(userID);
            if (Objects.nonNull(user)) {
                Item item = modelMapper.map(itemRequestDto, Item.class);
                item.setOwner(user);
                return itemRepository.save(item);
            }
            String msg = String.format("Нет пользователя с 'id' %s.", userID);
            log.info(msg);
            throw new NoContentException(msg);
        }
        String msg = String.format("Нет тела запроса", userID);
        log.info(msg);
        throw new BadRequestException(msg);
    }

    @Override
    @Transactional
    public Item updateItem(Long userID, Long itemId, ItemRequestDto itemRequestDto)
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
                return itemRepository.findById(itemId).get();
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
    public List<Item> searchItemByName(Long userId, String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return itemRepository.findUserItemLike(text, text);
    }

    @Override
    @Transactional
    public ItemComment addComment(Long userId, ItemCommentRequestDto inComment, Long itemId)
            throws NoContentException, BadRequestException {
        User user = userService.getUserById(userId);
        Item item = getItemById(itemId, userId);

        ItemComment itemComment = modelMapper.map(inComment, ItemComment.class);
        itemComment.setItem(item);
        itemComment.setAuthor(user);

        List<Booking> userBookings = bookingRepository.findAllByUserBookings(userId, itemId, LocalDateTime.now());
        if (userBookings.isEmpty()) {
            String msg = String.format("У user_id = {%s} нечего комментировать", userId);
            log.info(msg);
            throw new BadRequestException(msg);
        }
        return commentsRepository.save(itemComment);
    }

    @Override
    @Transactional
    public List<ItemComment> getAllItemComments(Long itemId) {
        return commentsRepository.findAllByItemId(itemId);
    }

}