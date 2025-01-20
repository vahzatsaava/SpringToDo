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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    @InjectMocks
    private TodoServiceImpl todoService;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private Principal principal;

    @Mock
    private TodoMapper todoMapper;


    @Test
    void saveTodo_ShouldSaveTodo() {

        TodoCreateRequest request = new TodoCreateRequest("New Todo", "Description");
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUserId()).thenReturn(user.getId());

        Principal principal = new UsernamePasswordAuthenticationToken(userDetails, null);

        todoService.saveTodo(request, principal);

        verify(todoRepository, times(1)).save(argThat(todo ->
                todo.getUserId().equals(1L) &&
                        todo.getTitle().equals("New Todo") &&
                        todo.getDescription().equals("Description") &&
                        !todo.isCompleted()
        ));
    }

    @Test
    void updateTodo_ShouldUpdateTodo() {
        Long todoId = 1L;
        TodoUpdateRequest request = new TodoUpdateRequest(todoId, "Updated Todo", "description", true);
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        Todo existingTodo = getTodo();

        TodoResponse expectedResponse = getTodoResponse();

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUserId()).thenReturn(user.getId());

        Principal principal = new UsernamePasswordAuthenticationToken(userDetails, null);

        when(todoRepository.findByIdAndUserId(todoId, user.getId())).thenReturn(Optional.of(existingTodo));
        when(todoMapper.mapToTodoResponse(existingTodo)).thenReturn(expectedResponse);


        TodoResponse response = todoService.updateTodo(request, principal);


        assertNotNull(response);
        assertEquals(todoId, response.getId());
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.getDescription(), response.getDescription());
        assertTrue(response.isCompleted());


        verify(todoRepository, times(1)).findByIdAndUserId(todoId, user.getId());
        verify(todoMapper, times(1)).mapToTodoResponse(existingTodo);
        verify(todoRepository, never()).save(any());

    }

    @Test
    void allTodosByPrincipalWithPagination_ShouldReturnPagedTodoResponses() {

        int page = 0;
        int size = 5;
        Long userId = 1L;
        Pageable pageable = PageRequest.of(page, size);

        Todo todo1 = getTodo();
        Todo todo2 = getTodo();

        List<Todo> todos = List.of(todo1, todo2);
        Page<Todo> todoPage = new PageImpl<>(todos, pageable, todos.size());

        TodoResponse response1 = getTodoResponse();
        TodoResponse response2 = getTodoResponse();

        UsernamePasswordAuthenticationToken authToken = mock(UsernamePasswordAuthenticationToken.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(authToken.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserId()).thenReturn(userId);
        when(todoRepository.findAllByUserId(userId, pageable)).thenReturn(todoPage);
        when(todoMapper.mapToTodoResponse(todo1)).thenReturn(response1);
        when(todoMapper.mapToTodoResponse(todo2)).thenReturn(response2);

        Page<TodoResponse> result = todoService.allTodosByPrincipalWithPagination(authToken, page, size);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Updated Todo", result.getContent().get(0).getTitle());
        assertEquals("Updated Todo", result.getContent().get(1).getTitle());

        // Verify interactions
        verify(todoRepository, times(1)).findAllByUserId(userId, pageable);
        verify(todoMapper, times(1)).mapToTodoResponse(todo1);
        verify(todoMapper, times(1)).mapToTodoResponse(todo2);
    }



    @Test
    void allTodosCompletedByPrincipal_ShouldReturnCompletedTodos() {
        Todo updatedTodo = getTodo();

        List<Todo> todos = List.of(updatedTodo);
        Long userId = 1L;

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUserId()).thenReturn(userId);

        Principal principal = new UsernamePasswordAuthenticationToken(userDetails, null);
        when(todoRepository.findCompletedTodosByUserId(1L)).thenReturn(todos);

        List<TodoResponse> result = todoService.allTodosCompletedByPrincipal(principal);

        assertEquals(1, result.size());
        verify(todoRepository).findCompletedTodosByUserId(1L);
    }


    @Test
    void findTodoById_ShouldReturnTodo() {
        Todo updatedTodo = getTodo();
        Long userId = 1L;

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUserId()).thenReturn(userId);

        Principal principal = new UsernamePasswordAuthenticationToken(userDetails, null);

        when(todoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(updatedTodo));
        when(todoMapper.mapToTodoResponse(any(Todo.class))).thenReturn(getTodoResponse());


        TodoResponse result = todoService.findTodoById(1L, principal);

        assertEquals(updatedTodo.isCompleted(), result.isCompleted());
        verify(todoRepository).findByIdAndUserId(1L, 1L);
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