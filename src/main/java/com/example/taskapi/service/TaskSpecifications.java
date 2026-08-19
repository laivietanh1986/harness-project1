package com.example.taskapi.service;

import com.example.taskapi.entity.Task;
import org.springframework.data.jpa.domain.Specification;

final class TaskSpecifications {

    private TaskSpecifications() {
    }

    static Specification<Task> hasCategory(String category) {
        return (root, query, cb) ->
                (category == null || category.isBlank()) ? null : cb.equal(root.get("category"), category);
    }

    static Specification<Task> hasStatus(String status) {
        return (root, query, cb) ->
                (status == null || status.isBlank()) ? null : cb.equal(root.get("status"), status);
    }

    static Specification<Task> filter(String category, String status) {
        return hasCategory(category).and(hasStatus(status));
    }
}
