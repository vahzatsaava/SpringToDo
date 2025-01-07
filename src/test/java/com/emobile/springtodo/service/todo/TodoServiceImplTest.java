package com.emobile.springtodo.service.todo;

import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.repository.todo.TodoRepository;
import com.emobile.springtodo.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    @InjectMocks
    private TodoServiceImpl todoService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private Principal principal;


    @Test
    void saveTodo_ShouldSaveTodo() {
        TodoCreateRequest request = new TodoCreateRequest("New Todo", "Description");

        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(getUser()));

        todoService.saveTodo(request, principal);

        verify(todoRepository).saveTodo(request, 1L);
    }

    @Test
    void updateTodo_ShouldUpdateTodo() {

        TodoUpdateRequest request = new TodoUpdateRequest(1L, "Updated Title", "Updated Desc", true);
        User user = getUser();
        when(principal.getName()).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(todoRepository.updateTodo(request, user.getId())).thenReturn(1);

        TodoResponse response = todoService.updateTodo(request, principal);

        assertEquals(request.getId(), response.getId());
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.getDescription(), response.getDescription());
        assertTrue(response.isCompleted());

        verify(todoRepository).updateTodo(request, user.getId());
    }


    @Test
    void allTodosByPrincipal_ShouldReturnTodos() {
        List<TodoResponse> todos = List.of(getTodoResponse());
        CustomUserDetails userDetails = new CustomUserDetails(1L, "testUser", "password", new ArrayList<>());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null);

        when(todoRepository.allTodosByUserIdWithPagination(eq(1L), anyInt(), anyInt())).thenReturn(todos);

        List<TodoResponse> result = todoService.allTodosByPrincipalWithPagination(authToken, 1, 10);

        assertEquals(1, result.size());
        verify(todoRepository).allTodosByUserIdWithPagination(1L, 1, 10);
    }




    @Test
    void allTodosCompletedByPrincipal_ShouldReturnCompletedTodos() {
        List<TodoResponse> todos = List.of(getTodoResponse());
        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(getUser()));
        when(todoRepository.allTodosCompletedByUserId(1L)).thenReturn(todos);

        List<TodoResponse> result = todoService.allTodosCompletedByPrincipal(principal);

        assertEquals(1, result.size());
        verify(todoRepository).allTodosCompletedByUserId(1L);
    }

    @Test
    void findTodoById_ShouldReturnTodo() {
        TodoResponse todo = getTodoResponse();
        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(getUser()));
        when(todoRepository.findTodoById(1L, 1L)).thenReturn(Optional.of(todo));

        TodoResponse result = todoService.findTodoById(1L, principal);

        assertEquals(todo, result);
        verify(todoRepository).findTodoById(1L, 1L);
    }

    @Test
    void findTodoById_ShouldThrowException_WhenUserNotFound() {
        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> todoService.findTodoById(1L, principal));
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