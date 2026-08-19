package com.example.taskapi.repository;

import com.example.taskapi.entity.Task;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> hasCategory(String category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Task> hasStatus(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
