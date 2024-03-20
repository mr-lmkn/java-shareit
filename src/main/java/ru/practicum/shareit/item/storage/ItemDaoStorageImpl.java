package ru.practicum.shareit.item.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.ConflictException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@AllArgsConstructor
public class ItemDaoStorageImpl implements ItemStorage {
    private final JdbcTemplate dataSource;

    @Override
    public List<Item> getAllItems() {
        ArrayList<Item> items = new ArrayList<>();
        SqlRowSet qryRows = dataSource.queryForRowSet("SELECT * FROM items");
        while (qryRows.next()) {
            Item item = mapItemRow(qryRows);
            items.add(item);
            log.info("Найдена вещь: {} {}", item.getId(), item.getName());
        }
        log.info("Конец списка вещей");
        return items;
    }

    @Override
    public List<Item> getAllUserItems(int userId) {
        ArrayList<Item> items = new ArrayList<>();
        SqlRowSet userRows = dataSource.queryForRowSet("SELECT * FROM items WHERE owner_user_id = ?", userId);
        while (userRows.next()) {
            Item item = mapItemRow(userRows);
            items.add(item);
            log.info("Найдена вещь: {} {}", item.getId(), item.getName());
        }
        log.info("Конец списка вещей пользователя");
        return items;
    }

    @Override
    public Item getItemById(Integer id) throws NoContentException {
        SqlRowSet itemsRows = dataSource.queryForRowSet("SELECT * FROM items WHERE item_id = ?", id);
        if (itemsRows.next()) {
            Item item = mapItemRow(itemsRows);
            log.info("Найдена вещь: {} {}", item.getId(), item.getName());
            return item;
        } else {
            String msg = String.format("Нет вещи с 'id'=%s.", id);
            log.info(msg);
            throw new NoContentException(msg);
        }
    }

    @Override
    public Item createItem(Item item) throws BadRequestException, ConflictException {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("items")
                .usingGeneratedKeyColumns("item_id");
        Map<String, Object> parameters = mapUserQueryParameters(item);
        Integer id = simpleJdbcInsert.executeAndReturnKey(parameters).intValue();
        log.info("Generated id - " + id);
        item.setId(id);
        return item;
    }

    @Override
    public Item updateItem(Integer itemId, Item item) throws BadRequestException, NoContentException {
        //Integer id = item.getId();
        String doDo = "обновление вещи";
        log.info("Инициировано {} {}", doDo, itemId);

        if (itemId != null && itemId > 0) {
            Integer updaterRows = dataSource.update(
                    "UPDATE items SET "
                            + "     item_name = CASE WHEN ? is not null THEN ? ELSE item_name END,"
                            + "  description  = CASE WHEN ? is not null THEN ? ELSE description END, "
                            + "    available  = CASE WHEN ? is not null THEN ? ELSE available END "
                            + " WHERE item_id = ? "
                            + "   AND owner_user_id = ? ",
                    item.getName(),
                    item.getName(),
                    item.getDescription(),
                    item.getDescription(),
                    item.getAvailable(),
                    item.getAvailable(),
                    itemId,
                    item.getOwner());

            if (updaterRows > 0) {
                log.info("Операция {} выполнена уcпешно", doDo);
                return getItemById(itemId);
            } else {
                String msg = String.format("Нет вещи с 'id' %s. Обновление не возможно.", itemId);
                log.info(msg);
                throw new NoContentException(msg);
            }
        }

        String msg = String.format("Не указан 'id' %s. Обновление не возможно.", itemId);
        log.info(msg);
        throw new BadRequestException(msg);

    }

    @Override
    public void delete(Integer userid, Integer id) throws BadRequestException {
        String doDo = " удаление вещи ";
        log.info("Инициировано {} {} пользователя {}", doDo, id, userid);
        String msg;

        if (id != null && id > 0) {
            Integer deleteUserRows = dataSource.update(
                    "DELETE FROM items WHERE item_id = ? and owner_user_id = ? ",
                    id,
                    userid);

            if (deleteUserRows > 0) {
                log.info("Вещь {} удален", id);
                return;
            } else {
                msg = String.format("Нет вещи с ID %s", id);
                log.info(msg);
                throw new BadRequestException(msg);
            }
        }

        msg = String.format("Не указан 'id' %s. Удаление не возможно.", id);
        log.info(msg);
        throw new BadRequestException(msg);
    }

    @Override
    public List<Item> searchItemByName(Integer userId, String text) {
        List outList = new ArrayList<>();
        SqlRowSet qryRows = dataSource.queryForRowSet(
                "SELECT * FROM items "
                        + " WHERE " //owner_user_id = ? "
                        + "       available = true "
                        + "   AND (    lower(item_name) like '%'||?||'%' "
                        + "         OR lower(description) like '%'||?||'%') ",
                // userId,
                text.toLowerCase(),
                text.toLowerCase());

        while (qryRows.next()) {
            Item item = mapItemRow(qryRows);
            outList.add(item);
            log.info("Найдена вещь: {} {}", item.getId(), item.getName());
        }
        log.info("Конец списка вещей");
        return outList;
    }

    private Item mapItemRow(SqlRowSet itemRows) {
        return Item.builder()
                .id(itemRows.getInt("ITEM_ID"))
                .owner(itemRows.getInt("OWNER_USER_ID"))
                .name(itemRows.getString("ITEM_NAME"))
                .description(itemRows.getString("DESCRIPTION"))
                .available(itemRows.getBoolean("AVAILABLE"))
                .request(itemRows.getInt("REQUEST_ID"))
                .build();
    }

    private Map<String, Object> mapUserQueryParameters(Item item) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ITEM_ID", item.getId());
        parameters.put("OWNER_USER_ID", item.getOwner());
        parameters.put("ITEM_NAME", item.getName());
        parameters.put("DESCRIPTION", item.getDescription());
        parameters.put("AVAILABLE", item.getAvailable());
        parameters.put("REQUEST_ID", item.getOwner());
        return parameters;
    }

}
