package com.example.taskapi.dto;

import java.util.List;

public class PagedTaskResponse {

    private final List<TaskResponse> content;
    private final long totalElements;
    private final int totalPages;
    private final int page;
    private final int size;

    public PagedTaskResponse(List<TaskResponse> content, long totalElements, int totalPages, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
    }

    public List<TaskResponse> getContent() {
        return content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}
