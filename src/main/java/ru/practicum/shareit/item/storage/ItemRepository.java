package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;

@EnableJpaRepositories
public interface ItemRepository extends JpaRepository<Item, Long> {

    ArrayList<Item> findAllByOwnerOrderByIdAsc(User owner);

    @Modifying
    @Query(value = "UPDATE items SET "
            + "     item_name = CASE WHEN :itemName is not null THEN :itemName ELSE item_name END,"
            + "  description  = CASE WHEN :description is not null THEN :description ELSE description END, "
            + "    available  = CASE WHEN :available is not null THEN :available ELSE available END "
            + " WHERE item_id = :idItem "
            + "   AND owner_user_id = :idUser ",
            nativeQuery = true)
    int partialUpdate(
            @Param("itemName") String itemName,
            @Param("description") String description,
            @Param("available") Boolean available,
            @Param("idItem") long idItem,
            @Param("idUser") long idUser);

    @Query(value = "SELECT * "
            + "       FROM items \n"
            + "      WHERE available = true \n"
            + "        AND (    upper(item_name) like '%'||upper(:itemName)||'%' \n"
            + "              OR upper(description) like '%'||upper(:description)||'%') \n",
            nativeQuery = true)
    ArrayList<Item> findUserItemLike(
            @Param("itemName") String itemName,
            @Param("description") String description);

}
