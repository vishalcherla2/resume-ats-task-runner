package com.ailab.resumetaskrunner.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ailab.resumetaskrunner.entity.TaskDependency;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, UUID> {

    List<TaskDependency> findByTaskId(UUID taskId);

    List<TaskDependency> findByDependsOnTaskId(UUID taskId);
}