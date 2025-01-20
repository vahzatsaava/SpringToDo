package com.emobile.springtodo.repository.todo;

import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface TodoRepository {
    String INSERT_TODO = "INSERT INTO todo (user_id, title, description, completed, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
    String UPDATE_TODO = "UPDATE todo SET title = ?, description = ?, completed = ?, updated_at = ? WHERE id = ? AND user_id = ?";
    String SELECT_ALL_TODOS = "SELECT * FROM todo WHERE user_id = ? LIMIT ? OFFSET ?";
    String SELECT_COMPLETED_TODOS = "SELECT * FROM todo WHERE user_id = ? AND completed = true";
    String SELECT_TODO_BY_ID = "SELECT * FROM todo WHERE id = ? AND user_id = ?";

    void saveTodo(TodoCreateRequest request, Long userId);

    int updateTodo(TodoUpdateRequest request, Long userId);

    Optional<TodoResponse> findTodoById(Long toDoId,Long userId);

    List<TodoResponse> allTodosByUserIdWithPagination(Long userId, int page, int size);

    List<TodoResponse> allTodosCompletedByUserId(Long userId);
}
