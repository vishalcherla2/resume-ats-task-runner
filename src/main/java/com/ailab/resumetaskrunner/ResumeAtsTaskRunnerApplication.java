package com.ailab.resumetaskrunner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResumeAtsTaskRunnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeAtsTaskRunnerApplication.class, args);
    }
}