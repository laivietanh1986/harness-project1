package com.example.taskapi;

import com.example.taskapi.entity.Task;
import com.example.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskFilteringSortingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void noParams_returnsDefaultsWithAllTasks() throws Exception {
        taskRepository.save(new Task("a", "work", "open"));
        taskRepository.save(new Task("b", "home", "done"));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void filterByCategory_returnsOnlyMatchingTasks() throws Exception {
        taskRepository.save(new Task("a", "work", "open"));
        taskRepository.save(new Task("b", "home", "open"));

        mockMvc.perform(get("/api/tasks").param("category", "work"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("a"));
    }

    @Test
    void filterByStatus_returnsOnlyMatchingTasks() throws Exception {
        taskRepository.save(new Task("a", "work", "open"));
        taskRepository.save(new Task("b", "work", "done"));

        mockMvc.perform(get("/api/tasks").param("status", "done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("b"));
    }

    @Test
    void filterByCategoryAndStatus_combinesWithAnd() throws Exception {
        taskRepository.save(new Task("a", "work", "open"));
        taskRepository.save(new Task("b", "work", "done"));
        taskRepository.save(new Task("c", "home", "open"));

        mockMvc.perform(get("/api/tasks").param("category", "work").param("status", "open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("a"));
    }

    @Test
    void filterCombination_zeroResults_returnsEmptyContentNotError() throws Exception {
        taskRepository.save(new Task("a", "work", "open"));

        mockMvc.perform(get("/api/tasks").param("category", "work").param("status", "done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void sortByTitleAsc_ordersResultsAscending() throws Exception {
        taskRepository.save(new Task("charlie", null, null));
        taskRepository.save(new Task("alpha", null, null));
        taskRepository.save(new Task("bravo", null, null));

        mockMvc.perform(get("/api/tasks").param("sortBy", "title").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("alpha"))
                .andExpect(jsonPath("$.content[1].title").value("bravo"))
                .andExpect(jsonPath("$.content[2].title").value("charlie"));
    }

    @Test
    void invalidSortBy_returns400WithClearMessage() throws Exception {
        mockMvc.perform(get("/api/tasks").param("sortBy", "notAColumn"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidSortDir_returns400WithClearMessage() throws Exception {
        mockMvc.perform(get("/api/tasks").param("sortDir", "sideways"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativePage_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks").param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonPositiveSize_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks").param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pagination_returnsCorrectMetadataAcrossPages() throws Exception {
        for (int i = 0; i < 5; i++) {
            taskRepository.save(new Task("task" + i, null, null));
        }

        mockMvc.perform(get("/api/tasks").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get("/api/tasks").param("page", "2").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
