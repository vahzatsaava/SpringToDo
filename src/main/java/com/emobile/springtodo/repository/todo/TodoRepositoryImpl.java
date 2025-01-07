package com.emobile.springtodo.repository.todo;

import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveTodo(TodoCreateRequest request, Long userId) {

        jdbcTemplate.update(
                INSERT_TODO, userId,
                request.getTitle(),
                request.getDescription(),
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Override
    public int updateTodo(TodoUpdateRequest request, Long userId) {
        return jdbcTemplate.update(
                UPDATE_TODO,
                request.getTitle(),
                request.getDescription(),
                request.isCompleted(),
                LocalDateTime.now(),
                request.getId(),
                userId
        );
    }


    @Override
    @Cacheable(value = "pagedTodos", key = "#userId + '-' + #page + '-' + #size")
    public List<TodoResponse> allTodosByUserIdWithPagination(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return jdbcTemplate.query(
                SELECT_ALL_TODOS,
                (rs, rowNum) -> mapTodoResponse(rs),
                userId,size,offset
        );
    }

    @Override
    @Cacheable(value = "completedTodos", key = "#userId")
    public List<TodoResponse> allTodosCompletedByUserId(Long userId) {
        return jdbcTemplate.query(
                SELECT_COMPLETED_TODOS,
                (rs, rowNum) -> mapTodoResponse(rs),
                userId
        );
    }

    @Override
    @Cacheable(value = "todos", key = "#toDoId")
    public Optional<TodoResponse> findTodoById(Long toDoId, Long userId) {
        List<TodoResponse> todos = jdbcTemplate.query(
                SELECT_TODO_BY_ID,
                (rs, rowNum) -> mapTodoResponse(rs),
                toDoId, userId
        );
        return todos.stream().findFirst();
    }




    private TodoResponse mapTodoResponse(ResultSet rs) throws SQLException {
        return new TodoResponse(rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getBoolean("completed"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
