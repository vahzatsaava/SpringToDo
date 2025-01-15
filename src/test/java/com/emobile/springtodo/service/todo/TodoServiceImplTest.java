package com.emobile.springtodo.service.todo;

import com.emobile.springtodo.entity.Todo;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import com.emobile.springtodo.mapper.TodoMapper;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.repository.hibernate.TodoRepository;
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
import static org.mockito.ArgumentMatchers.*;
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

    @Mock
    private TodoMapper todoMapper;


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
        Todo updatedTodo = getTodo();

        TodoUpdateRequest request = new TodoUpdateRequest(1L, "Updated Title", "Updated Desc", true);
        User user = getUser();
        when(principal.getName()).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(todoRepository.updateTodo(request, user.getId())).thenReturn(updatedTodo);
        when(todoMapper.mapToTodoResponse(any(Todo.class))).thenReturn(getTodoResponse());

        TodoResponse response = todoService.updateTodo(request, principal);

        assertEquals(updatedTodo.getId(), response.getId());
        assertTrue(response.isCompleted());

        verify(todoRepository).updateTodo(request, user.getId());
    }


    @Test
    void allTodosByPrincipal_ShouldReturnTodos() {
        Todo updatedTodo = new Todo();
        updatedTodo.setId(1L);
        updatedTodo.setUserId(2L);
        updatedTodo.setCompleted(true);
        updatedTodo.setTitle("Hello world");

        List<Todo> todos = List.of(updatedTodo);
        CustomUserDetails userDetails = new CustomUserDetails(1L, "testUser", "password", new ArrayList<>());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null);

        when(todoRepository.allTodosByUserIdWithPagination(eq(1L), anyInt(), anyInt())).thenReturn(todos);

        List<TodoResponse> result = todoService.allTodosByPrincipalWithPagination(authToken, 1, 10);

        assertEquals(1, result.size());
        verify(todoRepository).allTodosByUserIdWithPagination(1L, 1, 10);
    }




    @Test
    void allTodosCompletedByPrincipal_ShouldReturnCompletedTodos() {
        Todo updatedTodo = getTodo();

        List<Todo> todos = List.of(updatedTodo);

        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(getUser()));
        when(todoRepository.allTodosCompletedByUserId(1L)).thenReturn(todos);

        List<TodoResponse> result = todoService.allTodosCompletedByPrincipal(principal);

        assertEquals(1, result.size());
        verify(todoRepository).allTodosCompletedByUserId(1L);
    }

    @Test
    void findTodoById_ShouldReturnTodo() {
        Todo updatedTodo = getTodo();

        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(getUser()));
        when(todoRepository.findTodoById(1L, 1L)).thenReturn(Optional.of(updatedTodo));
        when(todoMapper.mapToTodoResponse(any(Todo.class))).thenReturn(getTodoResponse());


        TodoResponse result = todoService.findTodoById(1L, principal);

        assertEquals(updatedTodo.isCompleted(), result.isCompleted());
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

    private Todo getTodo() {
        Todo updatedTodo = new Todo();
        updatedTodo.setId(1L);
        updatedTodo.setUserId(2L);
        updatedTodo.setCompleted(true);
        updatedTodo.setTitle("Hello world");
        return updatedTodo;
    }


}