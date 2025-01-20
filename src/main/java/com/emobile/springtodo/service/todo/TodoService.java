package com.emobile.springtodo.service.todo;

import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;

import java.security.Principal;
import java.util.List;

public interface TodoService {

    void saveTodo(TodoCreateRequest request, Principal principal);
    TodoResponse updateTodo(TodoUpdateRequest request, Principal principal);
    List<TodoResponse> allTodosByPrincipalWithPagination(Principal principal,int page, int size);
    List<TodoResponse> allTodosCompletedByPrincipal(Principal principal);
    TodoResponse findTodoById(Long id, Principal principal);
}
