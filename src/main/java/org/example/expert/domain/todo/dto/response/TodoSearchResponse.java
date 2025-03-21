package org.example.expert.domain.todo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class TodoSearchResponse {
    private String title;
    private long assigneeCount;
    private long commentCount;

    public TodoSearchResponse(String title, long assigneeCount, long commentCount) {
        this.title = title;
        this.assigneeCount = assigneeCount;
        this.commentCount = commentCount;
    }
}
