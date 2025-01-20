package com.emobile.springtodo.mapper;

import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;

import java.time.LocalDateTime;

public class TodoMapper {

    private TodoMapper() {}

    public static TodoResponse mapToTodoResponse(TodoUpdateRequest request) {
        return new TodoResponse(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.isCompleted(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
