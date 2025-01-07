package com.emobile.springtodo.service.todo;

import com.emobile.springtodo.entity.User;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

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
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        todoRepository.saveTodo(request, user.getId());
    }

    @Override
    @CacheEvict(value = {"todos", "allTodos", "completedTodos"}, allEntries = true)
    public TodoResponse updateTodo(TodoUpdateRequest request, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        int rowsUpdated = todoRepository.updateTodo(request, user.getId());

        if (rowsUpdated == 0) {
            throw new TodoNotFoundException("Todo not found or permission denied.");
        }
        return TodoMapper.mapToTodoResponse(request);
    }


    @Override
    public List<TodoResponse> allTodosByPrincipalWithPagination(Principal principal, int page, int size) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();
        Long userId = userDetails.getUserId();

        return todoRepository.allTodosByUserIdWithPagination(userId, page, size);
    }


    @Override
    public List<TodoResponse> allTodosCompletedByPrincipal(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        return todoRepository.allTodosCompletedByUserId(user.getId());
    }

    @Override
    public TodoResponse findTodoById(Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        return todoRepository.findTodoById(id, user.getId())
                .orElseThrow(() -> new TodoNotFoundException("Todo not found with id: " + id));
    }
}
