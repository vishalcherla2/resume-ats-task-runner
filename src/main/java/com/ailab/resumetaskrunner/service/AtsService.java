package com.ailab.resumetaskrunner.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ailab.resumetaskrunner.entity.AtsAnalysis;

@Service
public class AtsService {

    private final SkillCatalog skillCatalog;

    public AtsService(SkillCatalog skillCatalog) {
        this.skillCatalog = skillCatalog;
    }

    public void extractResumeSkills(AtsAnalysis analysis,
                                    String resumeText) {

        List<String> extractedSkills =
                findSkills(resumeText);

        analysis.setExtractedSkills(
                String.join(", ", extractedSkills)
        );
    }

    public void calculateAtsScore(AtsAnalysis analysis) {

        List<String> resumeSkills =
                splitSkills(analysis.getExtractedSkills());

        List<String> requiredSkills =
                findSkills(analysis.getJobDescription());

        List<String> matchedSkills = requiredSkills.stream()
                .filter(resumeSkills::contains)
                .collect(Collectors.toList());

        List<String> missingSkills = requiredSkills.stream()
                .filter(skill -> !resumeSkills.contains(skill))
                .collect(Collectors.toList());

        double score = requiredSkills.isEmpty()
                ? 0.0
                : matchedSkills.size() * 100.0 / requiredSkills.size();

        analysis.setMatchedSkills(
                String.join(", ", matchedSkills)
        );

        analysis.setMissingSkills(
                String.join(", ", missingSkills)
        );

        analysis.setAtsScore(
                Math.round(score * 100.0) / 100.0
        );
    }

    private List<String> findSkills(String text) {

        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        String normalizedText =
                text.toLowerCase(Locale.ROOT);

        return skillCatalog.getSkills().stream()
                .filter(skill ->
                        normalizedText.contains(
                                skill.toLowerCase(Locale.ROOT)))
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> splitSkills(String skills) {

        if (skills == null || skills.isBlank()) {
            return new ArrayList<>();
        }

        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(skill -> !skill.isBlank())
                .map(skill -> skill.toLowerCase(Locale.ROOT))
                .distinct()
                .collect(Collectors.toList());
    }
}