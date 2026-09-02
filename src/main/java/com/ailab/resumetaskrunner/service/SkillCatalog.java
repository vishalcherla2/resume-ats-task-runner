package com.ailab.resumetaskrunner.service;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SkillCatalog {

    private final List<String> skills = List.of(

            // Programming Languages
            "java",
            "python",
            "c",
            "c++",
            "c#",
            "javascript",
            "typescript",

            // Backend
            "spring boot",
            "spring framework",
            "spring",
            "spring security",
            "spring data jpa",
            "hibernate",
            "jpa",

            // Frontend
            "react",
            "react js",
            "angular",
            "vue",
            "html",
            "html5",
            "css",
            "css3",

            // Databases
            "mysql",
            "postgresql",
            "oracle",
            "sql",
            "mongodb",
            "redis",

            // APIs / Architecture
            "rest api",
            "restful api",
            "microservices",
            "web services",

            // Version Control / DevOps
            "git",
            "github",
            "gitlab",
            "docker",
            "kubernetes",
            "jenkins",
            "ci/cd",

            // Cloud
            "aws",
            "azure",
            "gcp",

            // Messaging
            "kafka",
            "rabbitmq",

            // Testing
            "junit",
            "mockito",

            // Build
            "maven",
            "gradle",

            // Data / Analytics
            "machine learning",
            "data analytics",
            "data visualization",
            "power bi",
            "excel"
    );

    public List<String> getSkills() {
        return skills;
    }
}