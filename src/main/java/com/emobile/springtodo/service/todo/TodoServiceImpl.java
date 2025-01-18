package com.emobile.springtodo.service.todo;

import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import com.emobile.springtodo.exception.TodoNotFoundException;
import com.emobile.springtodo.mapper.TodoMapper;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.repository.todo.TodoRepository;
import com.emobile.springtodo.security.CustomUserDetails;
import com.emobile.springtodo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoServiceImpl implements TodoService {
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final JwtUtil jwtUtil;

    @Override
    @CacheEvict(value = {"todos", "allTodos", "completedTodos"}, allEntries = true)
    public void saveTodo(TodoCreateRequest request, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        todoRepository.saveTodo(request, userId);
    }

    @Override
    @CacheEvict(value = {"todos", "allTodos", "completedTodos"}, allEntries = true)
    public TodoResponse updateTodo(TodoUpdateRequest request, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);

        int rowsUpdated = todoRepository.updateTodo(request, userId);

        if (rowsUpdated == 0) {
            throw new TodoNotFoundException("Todo not found or permission denied.");
        }
        return TodoMapper.mapToTodoResponse(request);
    }


    @Override
    public List<TodoResponse> allTodosByPrincipalWithPagination(Principal principal, int page, int size) {
        Long userId = getUserIdFromPrincipal(principal);
        return todoRepository.allTodosByUserIdWithPagination(userId, page, size);
    }

    @Override
    public List<TodoResponse> allTodosCompletedByPrincipal(Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        return todoRepository.allTodosCompletedByUserId(userId);
    }

    @Override
    public TodoResponse findTodoById(Long id, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        return todoRepository.findTodoById(id, userId)
                .orElseThrow(() -> new TodoNotFoundException("Todo not found with id: " + id));
    }

    private Long getUserIdFromPrincipal(Principal principal){
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();
        Long userId = userDetails.getUserId();

        if (userId == null){
            throw new NoSuchElementException("User not found with ID: " + userId);

        }
        return userId;
    }
}
