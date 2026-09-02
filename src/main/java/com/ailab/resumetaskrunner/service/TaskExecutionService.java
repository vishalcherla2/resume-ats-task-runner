package com.ailab.resumetaskrunner.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.ailab.resumetaskrunner.entity.AtsAnalysis;
import com.ailab.resumetaskrunner.entity.Task;
import com.ailab.resumetaskrunner.enums.TaskType;
import com.ailab.resumetaskrunner.repository.AtsAnalysisRepository;

@Service
public class TaskExecutionService {

    private final AtsAnalysisRepository atsAnalysisRepository;
    private final ResumeTextExtractor resumeTextExtractor;
    private final AtsService atsService;

    public TaskExecutionService(
            AtsAnalysisRepository atsAnalysisRepository,
            ResumeTextExtractor resumeTextExtractor,
            AtsService atsService) {

        this.atsAnalysisRepository = atsAnalysisRepository;
        this.resumeTextExtractor = resumeTextExtractor;
        this.atsService = atsService;
    }

    public void execute(Task task) throws Exception {

        if (task.getType() == TaskType.SIMULATED) {

            int seconds = task.getDurationSeconds() == null
                    ? 2
                    : task.getDurationSeconds();

            Thread.sleep(seconds * 1000L);

            double failureChance =
                    task.getFailureChance() == null
                            ? 0.0
                            : task.getFailureChance();

            if (ThreadLocalRandom.current().nextDouble() < failureChance) {
                throw new RuntimeException("Simulated task failure");
            }

            return;
        }

        AtsAnalysis analysis = task.getAnalysis();

        if (task.getType() == TaskType.RESUME_SKILL_EXTRACTION) {

            Path resumePath =
                    Path.of(analysis.getResumeFilePath());

            byte[] fileBytes =
                    Files.readAllBytes(resumePath);

            String resumeText =
                    resumeTextExtractor.extractText(fileBytes);

            atsService.extractResumeSkills(
                    analysis,
                    resumeText
            );

            atsAnalysisRepository.save(analysis);

        } else if (task.getType() == TaskType.ATS_SKILL_MATCHING) {

            atsService.calculateAtsScore(analysis);

            atsAnalysisRepository.save(analysis);
        }
    }
}