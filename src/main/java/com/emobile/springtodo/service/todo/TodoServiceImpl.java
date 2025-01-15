package com.emobile.springtodo.service.todo;

import com.emobile.springtodo.entity.Todo;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import com.emobile.springtodo.exception.TodoNotFoundException;
import com.emobile.springtodo.mapper.TodoMapper;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.repository.hibernate.TodoRepository;
import com.emobile.springtodo.security.CustomUserDetails;
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
    private final TodoMapper todoMapper;

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

        Todo updatedTodo = todoRepository.updateTodo(request, user.getId());

        if (updatedTodo == null) {
            throw new TodoNotFoundException("Todo not found or permission denied.");
        }
        return todoMapper.mapToTodoResponse(updatedTodo);
    }


    @Override
    public List<TodoResponse> allTodosByPrincipalWithPagination(Principal principal, int page, int size) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();
        Long userId = userDetails.getUserId();

        return todoRepository.allTodosByUserIdWithPagination(userId, page, size)
                .stream()
                .map(todoMapper::mapToTodoResponse)
                .toList();
    }


    @Override
    public List<TodoResponse> allTodosCompletedByPrincipal(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        return todoRepository.allTodosCompletedByUserId(user.getId())
                .stream()
                .map(todoMapper::mapToTodoResponse)
                .toList();
    }

    @Override
    public TodoResponse findTodoById(Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Todo todo = todoRepository.findTodoById(id, user.getId())
                .orElseThrow(() -> new TodoNotFoundException("Todo not found with id: " + id));
        return todoMapper.mapToTodoResponse(todo);
    }
}
