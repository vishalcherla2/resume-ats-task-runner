package com.ailab.resumetaskrunner.service;

import java.nio.file.Files;
import java.nio.file.Path;

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