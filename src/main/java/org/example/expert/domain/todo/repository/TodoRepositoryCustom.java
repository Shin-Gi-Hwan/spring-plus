package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TodoRepositoryCustom {
    Optional<Todo> findByIdWithUser(Long Id);
    Page<TodoSearchResponse> searchTodos(String title, String managerNickname, Pageable pageable);
}
