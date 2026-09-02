Resume ATS Task Runner

This is a small Spring Boot application for running tasks with dependencies, retries and limited concurrency.

I used resume processing as the real example for this project.

For one resume, two tasks are created:

1. Resume Skill Extraction
2. ATS Skill Matching

ATS Skill Matching will wait until Resume Skill Extraction is completed successfully.

TECHNOLOGY USED

- Java 17
- Spring Boot
- Spring Data JPA
- MySQL
- JUnit
- PDFBox
- Maven

MAIN FEATURES

- Task dependencies
- Configurable concurrency
- Task retries
- Increasing retry delay
- Failed dependency handling
- Blocked tasks
- Circular dependency validation
- Task cancellation
- Task status checking
- Task statistics
- Restart recovery

HOW TO RUN

1. Create the MySQL database.

Database name:

resume_ats_task_runner

2. Configure the database username and password using:

DB_USERNAME
DB_PASSWORD

For local testing I used:

DB_USERNAME = root
DB_PASSWORD = root

The password is not stored in the project source code.

3. Start the Spring Boot application from Eclipse/STS.

The application runs on:

http://localhost:8080

CONCURRENCY

The maximum number of tasks running at the same time is configured in application.yml.

Current value is 2.

If the value is 2, only two tasks can run at the same time. Other tasks stay in WAITING state.

MAIN APIs

Submit Resume Analysis

POST /api/ats/analyze

This API accepts a PDF resume and a job description.

Check Task Status

GET /api/ats/task/{taskId}

This shows the current task status, attempts, retry time and other task details.

Check ATS Analysis

GET /api/ats/analysis/{analysisId}

This shows extracted skills, matched skills, missing skills and ATS score.

Cancel Task

DELETE /api/tasks/{taskId}

This is used to cancel a task which has not completed.

Task Statistics

GET /api/tasks/stats

This shows how many tasks are waiting, running, succeeded, failed, blocked and cancelled.

Simulated Task

POST /api/tasks/simulated

This API is mainly used to test the task runner without uploading a resume.

Example:

/api/tasks/simulated?name=TestTask&failureChance=0&durationSeconds=2

A failureChance of 1 makes the task always fail. This can be used to test retry handling.

Task Dependency

POST /api/tasks/{taskId}/dependencies/{dependsOnTaskId}

This adds a dependency between two tasks.

A task will run only after its dependency has succeeded.

RETRY HANDLING

When a task fails, it is retried until the maximum number of attempts is reached.

The retry delay increases after every failure.

For the current implementation the delays are 2 seconds, 4 seconds and then the task is marked as FAILED after the final attempt.

FAILED DEPENDENCY

If a dependency fails permanently, the task waiting for it will not run.

It will be changed to BLOCKED state.

This prevents tasks from waiting forever.

RESTART HANDLING

Task information is stored in MySQL.

If the application stops while a task is running, the task is changed back to WAITING when the application starts again.

The task can then run again.

TESTING

The tests can be run using Maven.

On Windows use:

mvnw.cmd test

The project should show BUILD SUCCESS when the tests pass.

POSTMAN

A Postman collection is included in the project under the postman folder.

It can be imported into Postman to test the APIs.

PROJECT STRUCTURE

src/main/java
- controller
- entity
- enums
- repository
- runner
- service

src/test/java
- TaskRunnerBehaviourTest.java

postman
- Resume-ATS-Task-Runner.postman_collection.json

Other important files:

README.md
DESIGN.md
TRADEOFFS.md
pom.xml

FUTURE IMPROVEMENTS

For a bigger production system, I would add authentication, distributed locking for multiple application instances and better task management for large task graphs.