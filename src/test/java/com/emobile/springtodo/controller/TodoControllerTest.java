package com.emobile.springtodo.controller;

import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import com.emobile.springtodo.exception.TodoNotFoundException;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.repository.todo.TodoRepository;
import com.emobile.springtodo.security.CustomUserDetails;
import com.emobile.springtodo.service.todo.TodoServiceImpl;
import com.emobile.springtodo.utils.AbstractRestControllerBaseTest;
import com.emobile.springtodo.utils.RedisTestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {RedisTestContainerConfig.class})
@Testcontainers
@ActiveProfiles("test")
class TodoControllerTest extends AbstractRestControllerBaseTest{

    @Autowired
    private TodoServiceImpl todoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoRepository todoRepository;

    private Principal principal;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE todo RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushDb();

        createTestUser();
        CustomUserDetails userDetails = new CustomUserDetails(1L, "testUser", "password", List.of());
        principal = new UsernamePasswordAuthenticationToken(userDetails, null);
    }

    @Test
    void saveTodo_ShouldSaveTodo() {
        TodoCreateRequest request = new TodoCreateRequest("New Todo", "Description");
        todoService.saveTodo(request, principal);

        List<TodoResponse> todos = todoRepository.allTodosByUserIdWithPagination(1L, 1, 10);
        assertEquals(1, todos.size());
        assertEquals("New Todo", todos.get(0).getTitle());
    }

    @Test
    void findTodoById_ShouldReturnTodoFromDatabase() {
        TodoCreateRequest todoCreateRequest = new TodoCreateRequest("New Todo", "Description");
        todoService.saveTodo(todoCreateRequest, principal);

        TodoResponse response = todoService.findTodoById(1L, principal);

        assertEquals("New Todo", response.getTitle());
        assertEquals("Description", response.getDescription());
    }

    @Test
    void updateTodo_ShouldUpdateTodoInDatabase() {
        TodoCreateRequest todoCreateRequest = new TodoCreateRequest("New Todo", "Description");
        todoService.saveTodo(todoCreateRequest, principal);

        TodoUpdateRequest request = new TodoUpdateRequest(1L, "Updated Todo", "Updated Description", true);
        TodoResponse response = todoService.updateTodo(request, principal);

        assertEquals("Updated Todo", response.getTitle());
        assertEquals("Updated Description", response.getDescription());
        assertTrue(response.isCompleted());
    }

    @Test
    void getAll_CompletedTodos_ShouldBeEmptySize() {
        List<TodoResponse> todoResponses = todoService.allTodosCompletedByPrincipal(principal);
        assertEquals(0, todoResponses.size());
    }

    @Test
    void getAll_CompletedTodos_ShouldHaveOneValue() {
        TodoCreateRequest todoCreateRequest = new TodoCreateRequest("New Todo", "Description");
        todoService.saveTodo(todoCreateRequest, principal);

        TodoUpdateRequest request = new TodoUpdateRequest(1L, "Updated Todo", "Updated Description", true);
        todoService.updateTodo(request, principal);

        List<TodoResponse> todoResponses = todoService.allTodosCompletedByPrincipal(principal);
        assertEquals(1, todoResponses.size());
    }
    

    private void createTestUser() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("password");
        user.setRole("USER");
        userRepository.save(user);
    }

}