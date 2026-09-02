package com.ailab.resumetaskrunner.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ailab.resumetaskrunner.runner.TaskRunner;

@Component
public class TaskScheduler {

    private final TaskRunner taskRunner;

    public TaskScheduler(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    @Scheduled(fixedDelayString = "${task-runner.scheduler-delay}")
    public void checkTasks() {
        taskRunner.runTasks();
    }
}