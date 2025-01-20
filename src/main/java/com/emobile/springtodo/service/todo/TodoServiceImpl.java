package com.emobile.springtodo.service.todo;

import com.emobile.springtodo.entity.Todo;
import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import com.emobile.springtodo.exception.TodoNotFoundException;
import com.emobile.springtodo.mapper.TodoMapper;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.repository.hibernate.TodoRepository;
import com.emobile.springtodo.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
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

        Long userId = getUserIdFromPrincipal(principal);

        Todo todo = Todo.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .completed(false)
                .build();
        todoRepository.save(todo);
    }


    @Override
    @CacheEvict(value = {"todos", "allTodos", "completedTodos"}, allEntries = true)
    @Transactional
    public TodoResponse updateTodo(TodoUpdateRequest request, Principal principal) {

        Long userId = getUserIdFromPrincipal(principal);

        Todo todo = todoRepository.findByIdAndUserId(request.getId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Todo not found or user mismatch."));

        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setCompleted(request.isCompleted());
        todo.setUpdatedAt(LocalDateTime.now());

        return todoMapper.mapToTodoResponse(todo);
    }


    @Override
    public Page<TodoResponse> allTodosByPrincipalWithPagination(Principal principal, int page, int size) {

        Long userId = getUserIdFromPrincipal(principal);

        Pageable pageRequest = PageRequest.of(page, size);

        return todoRepository.findAllByUserId(userId, pageRequest)
                .map(todoMapper::mapToTodoResponse);
    }


    @Override
    public List<TodoResponse> allTodosCompletedByPrincipal(Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);

        return todoRepository.findCompletedTodosByUserId(userId)
                .stream()
                .map(todoMapper::mapToTodoResponse)
                .toList();
    }

    @Override
    public TodoResponse findTodoById(Long id, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);

        Todo todo = todoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TodoNotFoundException("Todo not found with id: " + id));
        return todoMapper.mapToTodoResponse(todo);
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();
        return userDetails.getUserId();
    }
}
