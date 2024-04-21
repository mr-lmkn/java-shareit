package ru.practicum.shareit.request.storage;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.request.model.RequestItem;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@EnableJpaRepositories
public interface RequestRepository extends JpaRepository<RequestItem, Long> {

    List<RequestItem> findAllByRequesterOrderByCreated(User user);

    List<RequestItem> findAllByRequesterNotInOrderByCreated(List<User> user);

    List<RequestItem> findAllByRequesterNotIn(List<User> user, PageRequest page);

}
