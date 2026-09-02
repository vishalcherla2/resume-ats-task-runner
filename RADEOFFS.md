TRADEOFFS

1. Fixed thread pool

I used a fixed thread pool instead of creating a new thread for every task.

Creating a new thread for every task can use too many resources when many tasks come at the same time.

The fixed thread pool keeps the number of running tasks under control.

2. MySQL instead of only memory

I used MySQL to store task information.

An in-memory solution would be simpler, but task information would be lost when the application is restarted.

With MySQL, the application can recover tasks after a restart.

3. Increasing retry delay

I used increasing delay between retries instead of retrying immediately.

Immediate retries can keep putting load on a task which is already failing.

Increasing the delay gives the task some time before trying again.

4. FIFO waiting order

I used creation time to decide which waiting task should run first.

The oldest waiting task gets selected first.

A priority based system could be better for some use cases, but FIFO is simpler and easier to understand for this project.