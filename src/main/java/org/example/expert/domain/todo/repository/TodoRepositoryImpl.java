package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;

        Todo result = jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(String title, String managerNickname, Pageable pageable) {
        QTodo todo = QTodo.todo;
        QManager manager = QManager.manager;
        QComment comment = QComment.comment;

        // 담당자 수 서브쿼리
        Expression<Long> managerCount =
                JPAExpressions.select(manager.count())
                        .from(manager)
                        .where(manager.todo.eq(todo));

        // 댓글 수 서브쿼리
        Expression<Long> commentCount =
                JPAExpressions.select(comment.count())
                        .from(comment)
                        .where(comment.todo.eq(todo));

        List<TodoSearchResponse> results = jpaQueryFactory
                .select(Projections.constructor(TodoSearchResponse.class,
                        todo.title,
                        Expressions.asNumber(managerCount),
                        Expressions.asNumber(commentCount)
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(todo.comments, comment)
                .where(
                        titleContains(title),
                        managerNicknameContains(managerNickname)
                )
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                jpaQueryFactory
                        .select(todo.count())
                        .from(todo)
                        .where(
                                titleContains(title),
                                managerNicknameContains(managerNickname)
                        )
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(results, pageable, total);
    }

    private BooleanExpression titleContains(String title) {
        return title != null ? QTodo.todo.title.contains(title) : null;
    }

    private BooleanExpression managerNicknameContains(String managerNickname) {
        return managerNickname != null ? QManager.manager.user.nickname.contains(managerNickname) : null;
    }
}
