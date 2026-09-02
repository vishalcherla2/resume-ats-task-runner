package com.ailab.resumetaskrunner.controller;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ailab.resumetaskrunner.entity.AtsAnalysis;
import com.ailab.resumetaskrunner.entity.Task;
import com.ailab.resumetaskrunner.service.TaskService;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/ats/analyze")
    public ResponseEntity<Task> analyze(
            @RequestParam("resume") MultipartFile resume,
            @RequestParam("jobDescription") String jobDescription)
            throws IOException {

        return ResponseEntity.ok(
                taskService.createAnalysis(
                        resume,
                        jobDescription
                )
        );
    }

    @GetMapping("/ats/analysis/{analysisId}")
    public ResponseEntity<AtsAnalysis> getAnalysis(
            @PathVariable UUID analysisId) {

        return ResponseEntity.ok(
                taskService.getAnalysis(analysisId)
        );
    }

    @GetMapping("/ats/task/{taskId}")
    public ResponseEntity<Task> getTask(
            @PathVariable UUID taskId) {

        return ResponseEntity.ok(
                taskService.getTask(taskId)
        );
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Task> cancelTask(
            @PathVariable UUID taskId) {

        return ResponseEntity.ok(
                taskService.cancelTask(taskId)
        );
    }

    @GetMapping("/tasks/stats")
    public ResponseEntity<Map<String, Long>> getStats() {

        return ResponseEntity.ok(
                taskService.getStats()
        );
    }
}