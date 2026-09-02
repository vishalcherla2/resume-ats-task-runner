package com.ailab.resumetaskrunner.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ailab.resumetaskrunner.entity.Task;
import com.ailab.resumetaskrunner.entity.TaskDependency;
import com.ailab.resumetaskrunner.enums.TaskStatus;
import com.ailab.resumetaskrunner.repository.TaskDependencyRepository;

@Service
public class DependencyService {

    private final TaskDependencyRepository dependencyRepository;

    public DependencyService(
            TaskDependencyRepository dependencyRepository) {

        this.dependencyRepository = dependencyRepository;
    }

    public boolean areDependenciesCompleted(UUID taskId) {

        List<TaskDependency> dependencies =
                dependencyRepository.findByTaskId(taskId);

        for (TaskDependency dependency : dependencies) {

            Task dependentTask =
                    dependency.getDependsOnTask();

            if (dependentTask.getStatus() != TaskStatus.SUCCEEDED) {
                return false;
            }
        }

        return true;
    }

    public boolean hasFailedDependency(UUID taskId) {

        List<TaskDependency> dependencies =
                dependencyRepository.findByTaskId(taskId);

        for (TaskDependency dependency : dependencies) {

            Task dependentTask =
                    dependency.getDependsOnTask();

            TaskStatus status =
                    dependentTask.getStatus();

            if (status == TaskStatus.FAILED ||
                status == TaskStatus.BLOCKED ||
                status == TaskStatus.CANCELLED) {

                return true;
            }
        }

        return false;
    }
}