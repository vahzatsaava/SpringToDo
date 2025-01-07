package com.emobile.springtodo.controller.interfaces;


import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoResponse;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/v1/api/todos")
public interface TodoApi {

    @Operation(summary = "Создать новую задачу", description = "Добавляет новую задачу в список пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Задача успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content)
    })
    @PostMapping
    ResponseEntity<Void> createTodo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Данные для создания задачи", required = true,
                    content = @Content(schema = @Schema(implementation = TodoCreateRequest.class)))
            @Valid @RequestBody TodoCreateRequest request,
            Principal principal
    );

    @Operation(summary = "Обновить существующую задачу", description = "Обновляет существующую задачу по ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена", content = @Content)
    })
    @PutMapping
    TodoResponse updateTodo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Данные для обновления задачи", required = true,
                    content = @Content(schema = @Schema(implementation = TodoUpdateRequest.class)))
            @Valid @RequestBody TodoUpdateRequest request,
            Principal principal
    );

    @Operation(summary = "Получить все задачи с пагинацией", description = "Возвращает список задач пользователя с пагинацией.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач успешно получен")
    })
    @GetMapping
    List<TodoResponse> getAllTodosWithPagination(Principal principal,
            @Parameter(description = "Номер страницы (по умолчанию 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Размер страницы (по умолчанию 10)") @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "Получить все завершённые задачи", description = "Возвращает список всех завершённых задач.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список завершённых задач успешно получен")
    })
    @GetMapping("/completed")
    List<TodoResponse> getAllCompletedTodos(Principal principal);

    @Operation(summary = "Получить задачу по ID", description = "Возвращает задачу по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача найдена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена", content = @Content)
    })
    @GetMapping("/{todoId}")
    TodoResponse getTodoById(
            @Parameter(description = "Идентификатор задачи", required = true) @PathVariable Long todoId,
            Principal principal
    );
}
