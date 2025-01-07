package com.emobile.springtodo.controller;

import com.emobile.springtodo.controller.interfaces.TodoApi;
import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import com.emobile.springtodo.service.todo.TodoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/api/todos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TodoController implements TodoApi {
    private final TodoService todoService;

    @Override
    public ResponseEntity<Void> createTodo(@Valid @RequestBody TodoCreateRequest request, Principal principal) {
        todoService.saveTodo(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public TodoResponse updateTodo(@Valid @RequestBody TodoUpdateRequest request, Principal principal) {
        return todoService.updateTodo(request, principal);
    }

    @Override
    public List<TodoResponse> getAllTodosWithPagination( Principal principal,int page, int size) {
        return todoService.allTodosByPrincipalWithPagination(principal, page, size);
    }

    @Override
    public List<TodoResponse> getAllCompletedTodos(Principal principal) {
        return todoService.allTodosCompletedByPrincipal(principal);
    }

    @Override
    public TodoResponse getTodoById(Long todoId, Principal principal) {
        return todoService.findTodoById(todoId, principal);
    }

}
