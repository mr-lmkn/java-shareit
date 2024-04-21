package ru.practicum.shareit.request.service;

import ru.practicum.shareit.error.exceptions.BadRequestException;
import ru.practicum.shareit.error.exceptions.NoContentException;
import ru.practicum.shareit.request.dto.RequestItemRequestDto;
import ru.practicum.shareit.request.model.RequestItem;

import java.util.List;
import java.util.Optional;

public interface RequestService {
    List<RequestItem> getAll(Long userId, Optional<Integer> from, Optional<Integer> size) throws NoContentException;

    List<RequestItem> getAllUserItemRequests(Long userId) throws NoContentException;

    RequestItem getById(Long id, Long userId) throws NoContentException;

    RequestItem create(Long userId, RequestItemRequestDto requestDto) throws NoContentException, BadRequestException;
}
