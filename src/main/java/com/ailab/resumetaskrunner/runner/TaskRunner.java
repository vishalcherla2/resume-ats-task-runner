package com.ailab.resumetaskrunner.runner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ailab.resumetaskrunner.entity.Task;
import com.ailab.resumetaskrunner.enums.TaskStatus;
import com.ailab.resumetaskrunner.repository.TaskRepository;
import com.ailab.resumetaskrunner.service.DependencyService;
import com.ailab.resumetaskrunner.service.TaskExecutionService;

import jakarta.annotation.PostConstruct;

@Service
public class TaskRunner {

    private final TaskRepository taskRepository;
    private final DependencyService dependencyService;
    private final TaskExecutionService taskExecutionService;

    private final ExecutorService executorService;
    private final Semaphore semaphore;

    public TaskRunner(
            TaskRepository taskRepository,
            DependencyService dependencyService,
            TaskExecutionService taskExecutionService,
            @Value("${task-runner.max-concurrency:2}") int maxConcurrency) {

        this.taskRepository = taskRepository;
        this.dependencyService = dependencyService;
        this.taskExecutionService = taskExecutionService;

        this.executorService = Executors.newFixedThreadPool(maxConcurrency);
        this.semaphore = new Semaphore(maxConcurrency);
    }

    @PostConstruct
    public void recoverTasks() {

        List<Task> runningTasks =
                taskRepository.findByStatus(TaskStatus.RUNNING);

        for (Task task : runningTasks) {
            task.setStatus(TaskStatus.WAITING);
            task.setRetryAfter(null);
            task.setErrorMessage("Recovered after service restart");
            taskRepository.save(task);
        }
    }

    public void runTasks() {

        List<Task> tasks =
                taskRepository.findByStatus(TaskStatus.WAITING);

        for (Task task : tasks) {

            if (!semaphore.tryAcquire()) {
                break;
            }

            try {

                if (task.getRetryAfter() != null &&
                        task.getRetryAfter().isAfter(LocalDateTime.now())) {

                    semaphore.release();
                    continue;
                }

                if (task.getStatus() == TaskStatus.CANCELLED) {
                    semaphore.release();
                    continue;
                }

                if (!dependencyService.areDependenciesCompleted(task.getId())) {

                    if (dependencyService.hasFailedDependency(task.getId())) {

                        task.setStatus(TaskStatus.BLOCKED);
                        task.setErrorMessage("Dependency task failed");
                        task.setCompletedAt(LocalDateTime.now());

                        taskRepository.save(task);
                    }

                    semaphore.release();
                    continue;
                }

                task.setStatus(TaskStatus.RUNNING);
                task.setStartedAt(LocalDateTime.now());
                task.setAttempts(task.getAttempts() + 1);

                taskRepository.save(task);

                executorService.submit(() -> executeTask(task));

            } catch (Exception e) {

                semaphore.release();

                task.setStatus(TaskStatus.FAILED);
                task.setErrorMessage(e.getMessage());
                task.setCompletedAt(LocalDateTime.now());

                taskRepository.save(task);
            }
        }
    }

    private void executeTask(Task task) {

        try {

            taskExecutionService.execute(task);

            Task latestTask =
                    taskRepository.findById(task.getId()).orElse(task);

            if (latestTask.getStatus() == TaskStatus.CANCELLED) {
                return;
            }

            latestTask.setStatus(TaskStatus.SUCCEEDED);
            latestTask.setCompletedAt(LocalDateTime.now());
            latestTask.setRetryAfter(null);

            taskRepository.save(latestTask);

        } catch (Exception e) {

            handleFailure(
                    task,
                    e.getMessage() == null
                            ? "Task execution failed"
                            : e.getMessage()
            );

        } finally {

            semaphore.release();
        }
    }

    private void handleFailure(Task task, String errorMessage) {

        Task latestTask =
                taskRepository.findById(task.getId()).orElse(task);

        if (latestTask.getStatus() == TaskStatus.CANCELLED) {
            return;
        }

        latestTask.setErrorMessage(errorMessage);

        if (latestTask.getAttempts() < latestTask.getMaxAttempts()) {

            latestTask.setStatus(TaskStatus.WAITING);

            long delaySeconds =
                    (long) Math.pow(2, latestTask.getAttempts());

            latestTask.setRetryAfter(
                    LocalDateTime.now().plusSeconds(delaySeconds)
            );

        } else {

            latestTask.setStatus(TaskStatus.FAILED);
            latestTask.setCompletedAt(LocalDateTime.now());
        }

        taskRepository.save(latestTask);
    }
}