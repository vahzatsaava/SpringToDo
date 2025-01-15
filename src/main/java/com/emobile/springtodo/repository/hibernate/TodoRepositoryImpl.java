package com.emobile.springtodo.repository.hibernate;

import com.emobile.springtodo.dto.TodoCreateRequest;
import com.emobile.springtodo.dto.TodoUpdateRequest;
import com.emobile.springtodo.entity.Todo;
import com.emobile.springtodo.utills.HibernateUtil;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepository {

    @Override
    public void saveTodo(TodoCreateRequest request, Long userId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Todo todo = new Todo();
            todo.setUserId(userId);
            todo.setTitle(request.getTitle());
            todo.setDescription(request.getDescription());
            todo.setCompleted(false);
            todo.setCreatedAt(LocalDateTime.now());
            todo.setUpdatedAt(LocalDateTime.now());

            session.persist(todo);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public Todo updateTodo(TodoUpdateRequest request, Long userId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Todo todo = findTodoById(request.getId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Todo not found or user mismatch."));

            todo.setTitle(request.getTitle());
            todo.setDescription(request.getDescription());
            todo.setCompleted(request.isCompleted());
            todo.setUpdatedAt(LocalDateTime.now());

            session.merge(todo);

            transaction.commit();
            return todo;
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            throw e;
        }
    }


    @Override
    public Optional<Todo> findTodoById(Long toDoId, Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Todo todo = session.createQuery(SELECT_TODO_BY_ID, Todo.class)
                    .setParameter("id", toDoId)
                    .setParameter(USER_ID, userId)
                    .uniqueResult();

            return todo != null ? Optional.of(todo) : Optional.empty();
        }
    }

    @Override
    public List<Todo> allTodosByUserIdWithPagination(Long userId, int page, int size) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(SELECT_ALL_TODOS, Todo.class)
                    .setParameter(USER_ID, userId)
                    .setFirstResult((page - 1) * size)
                    .setMaxResults(size)
                    .list();
        }
    }

    @Override
    public List<Todo> allTodosCompletedByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(SELECT_COMPLETED_TODOS, Todo.class)
                    .setParameter(USER_ID, userId)
                    .list();
        }
    }


}
