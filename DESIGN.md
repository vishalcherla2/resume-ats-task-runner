DESIGN

Scenario

I used resume processing as the example for this task runner.

When a resume is submitted, the application creates two tasks.

1. Resume Skill Extraction
2. ATS Skill Matching

The second task depends on the first task. So ATS Skill Matching will run only after skill extraction is successful.

How the runner works

The scheduler checks waiting tasks regularly.

Before starting a task, the runner checks if the task is ready and if a concurrency slot is available.

If everything is okay, the task changes from WAITING to RUNNING.

After execution, the task becomes SUCCEEDED or it is retried if it fails.

Concurrency

The maximum number of tasks is configured in application.yml.

The current value is 2.

The runner uses a fixed thread pool and a semaphore. A task needs an available slot before it starts running.

Because of this, if the limit is 2, a third task will stay in WAITING until one of the running tasks finishes.

If this limit was not handled properly, many tasks could run at the same time and use more system resources than expected.

Dependencies

A task checks all its dependencies before it starts.

If all dependencies are SUCCEEDED, the task can run.

If any dependency has permanently FAILED, BLOCKED or CANCELLED, the dependent task becomes BLOCKED.

This makes sure that a task does not wait forever.

Retries

When a task fails, it is retried until the maximum number of attempts is reached.

The delay increases after every failed attempt.

The current retry delay is 2 seconds after the first failure and 4 seconds after the second failure.

After the maximum attempts are completed, the task becomes FAILED.

Circular dependencies

Before adding a dependency, the application checks if the dependency will create a cycle.

For example, if Task A depends on Task B and Task B depends on Task A, the dependency is rejected.

The error message is "Circular dependency detected".

Restart

Task information is stored in MySQL.

If the application is stopped while a task is RUNNING, that task is changed back to WAITING when the application starts again.

The task can then run again.

I chose this because it is better to run the task again than to lose the task completely.

There is a small possibility that some work can be done twice after a restart.

Waiting task order

When multiple tasks are waiting, older tasks are selected first using their creation time.

This is simple and predictable.

One downside is that a long task can be selected before a short task.

Correctness rule

The most important rule is that a task must not run before all its dependencies have succeeded.

This rule is checked in TaskRunner before the task is changed to RUNNING.

Cancellation

A task can be cancelled before it completes.

The task is changed to CANCELLED.

If another task depends on a cancelled task, the dependent task will become BLOCKED.

Extra improvement

I added a task statistics API.

It shows the number of tasks in WAITING, RUNNING, SUCCEEDED, FAILED, BLOCKED and CANCELLED states.

This gives a quick view of what is happening in the task runner.

Future improvements

For a bigger production system, I would add authentication, distributed locking for multiple application instances and better task submission for large task graphs.