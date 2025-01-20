package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import com.emobile.springtodo.exception.TodoNotFoundException;
import com.emobile.springtodo.security.JwtUtil;
import com.emobile.springtodo.service.todo.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TodoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Principal principal;

    @Test
    void createTodo_ShouldReturnCreatedStatus() throws Exception {
        TodoCreateRequest request = new TodoCreateRequest("New Todo", "Description");

        Mockito.doNothing().when(todoService).saveTodo(any(TodoCreateRequest.class), any(Principal.class));

        mockMvc.perform(post("/v1/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(principal))
                .andExpect(status().isCreated());
    }

    @Test
    void updateTodo_ShouldReturnUpdatedTodo() throws Exception {
        TodoUpdateRequest request = new TodoUpdateRequest(1L, "Updated Todo", "Updated Description", true);
        TodoResponse response = new TodoResponse(1L, "Updated Todo", "Updated Description", true, LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(todoService.updateTodo(any(TodoUpdateRequest.class), any(Principal.class))).thenReturn(response);

        mockMvc.perform(put("/v1/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Todo")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    void getAllTodosWithPagination_ShouldReturnPagedTodoList() throws Exception {

        TodoResponse response1 = getTodoResponse();
        TodoResponse response2 = getTodoResponse();

        List<TodoResponse> responses = List.of(response1, response2);
        Page<TodoResponse> responsePage = new PageImpl<>(responses, PageRequest.of(0, 10), responses.size());

        Mockito.when(todoService.allTodosByPrincipalWithPagination(any(Principal.class), eq(0), eq(10)))
                .thenReturn(responsePage);

        mockMvc.perform(get("/v1/api/todos")
                        .param("page", "0")
                        .param("size", "10")
                        .principal(principal))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title", is("Todo 1")))
                .andExpect(jsonPath("$.content[0].description", is("Description 1")))
                .andExpect(jsonPath("$.content[1].title", is("Todo 1")))
                .andExpect(jsonPath("$.content[1].description", is("Description 1")));
    }


    @Test
    void getAllCompletedTodos_ShouldReturnEmptyList() throws Exception {
        Mockito.when(todoService.allTodosCompletedByPrincipal(any(Principal.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/api/todos/completed")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getTodoById_ShouldReturnTodo() throws Exception {
        TodoResponse response = getTodoResponse();

        Mockito.when(todoService.findTodoById(eq(1L), any(Principal.class))).thenReturn(response);

        mockMvc.perform(get("/v1/api/todos/1")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Todo 1")))
                .andExpect(jsonPath("$.description", is("Description 1")));
    }

    @Test
    void getTodoById_ShouldReturnNotFound_WhenTodoDoesNotExist() throws Exception {
        Mockito.when(todoService.findTodoById(eq(999L), any(Principal.class)))
                .thenThrow(new TodoNotFoundException("Todo not found"));

        mockMvc.perform(get("/v1/api/todos/999")
                        .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Todo not found")));
    }
    @Test
    void createTodo_ShouldReturnBadRequest_WhenInvalidInput() throws Exception {
        TodoCreateRequest request = new TodoCreateRequest("", ""); // Invalid input

        mockMvc.perform(post("/v1/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(principal))
                .andExpect(status().isBadRequest());
    }
    @Test
    void updateTodo_ShouldReturnNotFound_WhenTodoDoesNotExist() throws Exception {
        TodoUpdateRequest request = new TodoUpdateRequest(999L, "Nonexistent Todo", "Nonexistent Description", true);

        Mockito.when(todoService.updateTodo(any(TodoUpdateRequest.class), any(Principal.class)))
                .thenThrow(new TodoNotFoundException("Todo not found"));

        mockMvc.perform(put("/v1/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Todo not found")));
    }


    private TodoResponse getTodoResponse() {
        return new TodoResponse(1L, "Todo 1", "Description 1", false, LocalDateTime.now(), LocalDateTime.now());
    }


}
