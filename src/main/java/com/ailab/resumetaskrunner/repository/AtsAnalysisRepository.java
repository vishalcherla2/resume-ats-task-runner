package com.ailab.resumetaskrunner.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ailab.resumetaskrunner.entity.AtsAnalysis;

public interface AtsAnalysisRepository extends JpaRepository<AtsAnalysis, UUID> {
}