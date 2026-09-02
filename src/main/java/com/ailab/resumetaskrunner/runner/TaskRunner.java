package com.ailab.resumetaskrunner.runner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import com.ailab.resumetaskrunner.entity.Task;
import com.ailab.resumetaskrunner.enums.TaskStatus;
import com.ailab.resumetaskrunner.repository.TaskRepository;

@Service
public class TaskRunner {

    private final TaskRepository taskRepository;

    private final ExecutorService executorService =
            Executors.newFixedThreadPool(2);

    public TaskRunner(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void runTasks() {

        List<Task> tasks =
                taskRepository.findByStatus(TaskStatus.WAITING);

        for (Task task : tasks) {

            task.setStatus(TaskStatus.RUNNING);
            task.setStartedAt(LocalDateTime.now());

            taskRepository.save(task);

            executorService.submit(() -> executeTask(task));
        }
    }

    private void executeTask(Task task) {

        try {

            Thread.sleep(2000);

            task.setStatus(TaskStatus.SUCCEEDED);
            task.setCompletedAt(LocalDateTime.now());

            taskRepository.save(task);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage("Task interrupted");

            taskRepository.save(task);
        }
    }
}