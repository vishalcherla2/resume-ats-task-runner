package com.ailab.resumetaskrunner.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ailab.resumetaskrunner.entity.AtsAnalysis;
import com.ailab.resumetaskrunner.entity.Task;
import com.ailab.resumetaskrunner.entity.TaskDependency;
import com.ailab.resumetaskrunner.enums.TaskStatus;
import com.ailab.resumetaskrunner.enums.TaskType;
import com.ailab.resumetaskrunner.repository.AtsAnalysisRepository;
import com.ailab.resumetaskrunner.repository.TaskDependencyRepository;
import com.ailab.resumetaskrunner.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AtsAnalysisRepository atsAnalysisRepository;
    private final TaskDependencyRepository dependencyRepository;

    public TaskService(
            TaskRepository taskRepository,
            AtsAnalysisRepository atsAnalysisRepository,
            TaskDependencyRepository dependencyRepository) {

        this.taskRepository = taskRepository;
        this.atsAnalysisRepository = atsAnalysisRepository;
        this.dependencyRepository = dependencyRepository;
    }

    public Task createAnalysis(
            MultipartFile resume,
            String jobDescription) throws IOException {

        if (resume == null || resume.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }

        if (jobDescription == null || jobDescription.isBlank()) {
            throw new IllegalArgumentException("Job description is required");
        }

        String originalFileName = resume.getOriginalFilename();

        if (originalFileName == null ||
                !originalFileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF resumes are supported");
        }

        Path uploadDirectory = Paths.get("uploads");
        Files.createDirectories(uploadDirectory);

        String fileName = UUID.randomUUID() + "_" + originalFileName;
        Path filePath = uploadDirectory.resolve(fileName);

        Files.write(filePath, resume.getBytes());

        AtsAnalysis analysis = new AtsAnalysis();
        analysis.setResumeFileName(originalFileName);
        analysis.setResumeFilePath(filePath.toString());
        analysis.setJobDescription(jobDescription);

        analysis = atsAnalysisRepository.save(analysis);

        Task extractionTask = new Task();
        extractionTask.setName("Resume Skill Extraction");
        extractionTask.setType(TaskType.RESUME_SKILL_EXTRACTION);
        extractionTask.setStatus(TaskStatus.WAITING);
        extractionTask.setAnalysis(analysis);

        extractionTask = taskRepository.save(extractionTask);

        Task matchingTask = new Task();
        matchingTask.setName("ATS Skill Matching");
        matchingTask.setType(TaskType.ATS_SKILL_MATCHING);
        matchingTask.setStatus(TaskStatus.WAITING);
        matchingTask.setAnalysis(analysis);

        matchingTask = taskRepository.save(matchingTask);

        TaskDependency dependency = new TaskDependency();
        dependency.setTask(matchingTask);
        dependency.setDependsOnTask(extractionTask);

        dependencyRepository.save(dependency);

        return extractionTask;
    }

    public Task getTask(UUID taskId) {

        return taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Task not found: " + taskId));
    }

    public AtsAnalysis getAnalysis(UUID analysisId) {

        return atsAnalysisRepository.findById(analysisId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Analysis not found: " + analysisId));
    }

    public Task cancelTask(UUID taskId) {

        Task task = getTask(taskId);

        if (task.getStatus() == TaskStatus.SUCCEEDED ||
                task.getStatus() == TaskStatus.FAILED ||
                task.getStatus() == TaskStatus.BLOCKED) {

            throw new IllegalStateException(
                    "Task cannot be cancelled in status "
                            + task.getStatus());
        }

        task.setStatus(TaskStatus.CANCELLED);
        task.setErrorMessage("Task cancelled by user");
        task.setCompletedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    public Map<String, Long> getStats() {

        Map<String, Long> stats = new LinkedHashMap<>();

        for (TaskStatus status : TaskStatus.values()) {
            stats.put(
                    status.name(),
                    taskRepository.countByStatus(status)
            );
        }

        stats.put("TOTAL", taskRepository.count());

        return stats;
    }
    public void validateNoCircularDependency(
            UUID taskId,
            UUID dependsOnTaskId) {

        if (taskId.equals(dependsOnTaskId)) {
            throw new CircularDependencyException(
                    "Circular dependency detected"
            );
        }

        if (createsCycle(taskId, dependsOnTaskId, new java.util.HashSet<>())) {
            throw new CircularDependencyException(
                    "Circular dependency detected"
            );
        }
    }

    private boolean createsCycle(
            UUID originalTaskId,
            UUID currentDependencyId,
            java.util.Set<UUID> visited) {

        if (!visited.add(currentDependencyId)) {
            return false;
        }

        if (originalTaskId.equals(currentDependencyId)) {
            return true;
        }

        for (TaskDependency dependency :
                dependencyRepository.findByTaskId(currentDependencyId)) {

            if (createsCycle(
                    originalTaskId,
                    dependency.getDependsOnTask().getId(),
                    visited)) {

                return true;
            }
        }

        return false;
    }
}