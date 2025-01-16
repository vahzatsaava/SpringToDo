package com.emobile.springtodo.repository.hibernate;
import com.emobile.springtodo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {


    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    Page<Todo> findAllByUserId(Long userId, Pageable pageable);

    @Query("SELECT t FROM Todo t WHERE t.userId = :userId AND t.completed = true")
    List<Todo> findCompletedTodosByUserId(@Param("userId") Long userId);

}
