package com.ailab.resumetaskrunner.runner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import com.ailab.resumetaskrunner.entity.Task;
import com.ailab.resumetaskrunner.enums.TaskStatus;
import com.ailab.resumetaskrunner.repository.TaskRepository;
import com.ailab.resumetaskrunner.service.DependencyService;

@Service
public class TaskRunner {

    private final TaskRepository taskRepository;
    private final DependencyService dependencyService;

    private final ExecutorService executorService =
            Executors.newFixedThreadPool(2);

    public TaskRunner(TaskRepository taskRepository,
                      DependencyService dependencyService) {

        this.taskRepository = taskRepository;
        this.dependencyService = dependencyService;
    }

    public void runTasks() {

        List<Task> tasks =
                taskRepository.findByStatus(TaskStatus.WAITING);

        for (Task task : tasks) {

            if (task.getRetryAfter() != null &&
                task.getRetryAfter().isAfter(LocalDateTime.now())) {
                continue;
            }

            if (!dependencyService.areDependenciesCompleted(task.getId())) {

                if (dependencyService.hasFailedDependency(task.getId())) {

                    task.setStatus(TaskStatus.BLOCKED);
                    task.setErrorMessage("Dependency task failed");

                    taskRepository.save(task);
                }

                continue;
            }

            task.setStatus(TaskStatus.RUNNING);
            task.setStartedAt(LocalDateTime.now());
            task.setAttempts(task.getAttempts() + 1);

            taskRepository.save(task);

            executorService.submit(() -> executeTask(task));
        }
    }

    private void executeTask(Task task) {

        try {

            Thread.sleep(2000);

            task.setStatus(TaskStatus.SUCCEEDED);
            task.setCompletedAt(LocalDateTime.now());
            task.setRetryAfter(null);

            taskRepository.save(task);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            handleFailure(task, "Task interrupted");
        }
    }

    private void handleFailure(Task task, String errorMessage) {

        task.setErrorMessage(errorMessage);

        if (task.getAttempts() < task.getMaxAttempts()) {

            task.setStatus(TaskStatus.WAITING);

            int retryNumber = task.getAttempts();

            long delaySeconds = (long) Math.pow(2, retryNumber);

            task.setRetryAfter(
                    LocalDateTime.now().plusSeconds(delaySeconds)
            );

        } else {

            task.setStatus(TaskStatus.FAILED);
            task.setCompletedAt(LocalDateTime.now());
        }

        taskRepository.save(task);
    }
}