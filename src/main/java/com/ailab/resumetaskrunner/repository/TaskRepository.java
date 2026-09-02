package com.ailab.resumetaskrunner.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ailab.resumetaskrunner.entity.Task;
import com.ailab.resumetaskrunner.enums.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByStatus(TaskStatus status);
}