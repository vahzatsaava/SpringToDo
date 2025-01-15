package com.emobile.springtodo.repository.hibernate;

import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import com.emobile.springtodo.entity.Todo;

import java.util.List;
import java.util.Optional;

public interface TodoRepository {

    String SELECT_ALL_TODOS = "FROM Todo WHERE userId = :userId";
    String SELECT_COMPLETED_TODOS = "FROM Todo WHERE userId = :userId AND completed = true";
    String SELECT_TODO_BY_ID = "FROM Todo WHERE id = :id AND userId = :userId";

    String USER_ID = "userId";

    void saveTodo(TodoCreateRequest request, Long userId);

    Todo updateTodo(TodoUpdateRequest request, Long userId);

    Optional<Todo> findTodoById(Long toDoId,Long userId);

    List<Todo> allTodosByUserIdWithPagination(Long userId, int page, int size);

    List<Todo> allTodosCompletedByUserId(Long userId);
}
