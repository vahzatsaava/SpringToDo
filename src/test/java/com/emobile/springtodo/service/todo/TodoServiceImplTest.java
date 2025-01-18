package com.emobile.springtodo.service.todo;

import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import com.emobile.springtodo.exception.TodoNotFoundException;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.repository.todo.TodoRepository;
import com.emobile.springtodo.security.CustomUserDetails;
import com.emobile.springtodo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

     @Mock
    private TodoRepository todoRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TodoServiceImpl todoService;

    private Principal principal;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new CustomUserDetails(1L, "testUser", "password", List.of());
        principal = new UsernamePasswordAuthenticationToken(userDetails, null);
    }

    @Test
    void saveTodo_ShouldCallRepository() {
        TodoCreateRequest request = new TodoCreateRequest("Test Task", "dsfdfsd");
        doNothing().when(todoRepository).saveTodo(request, 1L);

        todoService.saveTodo(request, principal);

        verify(todoRepository, times(1)).saveTodo(request, 1L);
    }

    @Test
    void updateTodo_ShouldUpdateAndReturnResponse() {
        TodoUpdateRequest request = new TodoUpdateRequest(1L, "Updated Task","sdfsd", true);
        when(todoRepository.updateTodo(request, 1L)).thenReturn(1);

        TodoResponse response = todoService.updateTodo(request, principal);

        assertNotNull(response);
        assertEquals(request.getId(), response.getId());
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.isCompleted(), response.isCompleted());
        verify(todoRepository, times(1)).updateTodo(request, 1L);
    }

    @Test
    void updateTodo_ShouldThrowException_WhenTodoNotFound() {
        TodoUpdateRequest request = new TodoUpdateRequest(1L, "Updated Task","sdfsd", true);
        when(todoRepository.updateTodo(request, 1L)).thenReturn(0);

        assertThrows(TodoNotFoundException.class, () -> todoService.updateTodo(request, principal));
    }

    @Test
    void allTodosByPrincipalWithPagination_ShouldReturnTodos() {
        when(todoRepository.allTodosByUserIdWithPagination(1L, 0, 10)).thenReturn(List.of(getTodoResponse()));

        List<TodoResponse> todos = todoService.allTodosByPrincipalWithPagination(principal, 0, 10);

        assertEquals(1, todos.size());
        verify(todoRepository, times(1)).allTodosByUserIdWithPagination(1L, 0, 10);
    }

    @Test
    void allTodosCompletedByPrincipal_ShouldReturnCompletedTodos() {
        when(todoRepository.allTodosCompletedByUserId(1L)).thenReturn(List.of(getTodoResponse()));

        List<TodoResponse> todos = todoService.allTodosCompletedByPrincipal(principal);

        assertEquals(1, todos.size());
        assertTrue(todos.get(0).isCompleted());
        verify(todoRepository, times(1)).allTodosCompletedByUserId(1L);
    }

    @Test
    void findTodoById_ShouldReturnTodo() {
        User user = getUser();
        when(todoRepository.findTodoById(1L, 1L)).thenReturn(Optional.of(getTodoResponse()));

        TodoResponse todo = todoService.findTodoById(1L, principal);

        assertNotNull(todo);
        assertEquals(1L, todo.getId());
        verify(todoRepository, times(1)).findTodoById(1L, 1L);
    }

    @Test
    void findTodoById_ShouldThrowException_WhenNotFound() {
        User user = getUser();
        when(todoRepository.findTodoById(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(TodoNotFoundException.class, () -> todoService.findTodoById(1L, principal));
    }

    private User getUser() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        return mockUser;
    }

    private TodoResponse getTodoResponse() {
        return new TodoResponse(1L, "Updated Todo", "description", true, LocalDateTime.now(), LocalDateTime.now());

    }

}