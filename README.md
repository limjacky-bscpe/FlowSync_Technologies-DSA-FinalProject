# PriorQ
Queue-Based Smart Task Scheduling and Deadline Prioritization System

PriorQ is a Java 21 desktop application for individual students. Use the task queue to decide what to work on next and the study schedule to reserve a start time for each task. GitHub is used to collaborate on the source code. Each student has a separate local task list.

## Run in Visual Studio Code
1. Install a Java Development Kit (JDK) version 21 or later, Visual Studio Code, and the Extension Pack for Java. Install Git to collaborate.
2. Extract the ZIP. In VS Code, use File > Open Folder and choose the PriorQ folder containing src and .vscode.
3. Open src/priorq/Main.java and select Run above main, or press F5 and select Run PriorQ.
4. To run the packaged build from the extracted folder, enter `java -jar PriorQ.jar`. Run the source in VS Code after making changes. The packaged JAR is a snapshot.

## Try a two-minute demonstration
1. Add a low-priority assignment due tomorrow and a high-priority project due the following day. The assignment appears first.
2. Change the project deadline to tomorrow. The high-priority project moves to the front. Add another high-priority task due tomorrow to demonstrate first-in, first-out tie breaking.
3. Give one task a study time. Open Study schedule to see it. A deadline is the submission date. A study session is when you plan to begin working.
4. Click Start task, then Mark complete. The next task moves to the front. Open Completed, select the finished task, and choose Edit task to reopen it.
5. Close and reopen PriorQ to see the saved tasks.

## Team workflow with GitHub
**Team lead:** Create one empty repository in your team's GitHub account and grant teammates access. From the extracted project folder, run the following commands, replacing OWNER and REPOSITORY with your actual values.
```sh
git init -b main
git add .
git commit -m "Add PriorQ Java desktop application"
git remote add origin https://github.com/OWNER/REPOSITORY.git
git push -u origin main
```
If your team already has a repository with commits, clone it first, copy the project files into the clone, then commit on a feature branch. Preserve existing work.

**Each teammate:** Clone the shared repository once and open its folder in VS Code.
```sh
git clone https://github.com/OWNER/REPOSITORY.git
cd REPOSITORY
code .
```
For each change, pull the latest main branch, work on a named branch, and push it for review.
```sh
git switch main
git pull --ff-only
git switch -c feature/task-editor
# Edit and run the application in VS Code before committing.
git add src
git commit -m "Improve the task editor"
git push -u origin feature/task-editor
```
Open a pull request on GitHub targeting main. A teammate reviews and merges it. Use a different branch name for each new feature. The .gitignore file excludes compiled outputs. Teammates can run directly from source.

## Course workflow documentation

The `docs/workflow/` folder contains a GitHub setup walkthrough, team and repository details sheet, commit log, progress log, AI tool usage log, and an evidence folder for the team's actual screenshots or exports. Complete these with your group's real information. The templates are not evidence of completed commits or team participation.

Follow the instructor's repository naming pattern `[TeamName]-DSA-FinalProject`. The repository administrator should invite each teammate in GitHub Settings and confirm everyone accepts the invitation. See [the workflow guide](docs/workflow/README.md) for GitHub initialization, VS Code Source Control steps, commit evidence, and progress board guidance.

## DSA explanation for the project defense

PriorQ stores active tasks in Java's `PriorityQueue`, a binary heap. Its comparator orders tasks by earliest deadline, then High before Medium before Low priority, then smaller task ID for first-entered-first-served ties. This ordering directly supports identifying the next task. Task types remain academic labels and do not change queue order. A study session records when the student plans to begin work, separate from the submission deadline.

## Implementation
- Main.java starts Swing and acquires a single-instance file lock.
- Task.java defines task fields, status values, and the ordering rule.
- TaskStore.java uses a Java PriorityQueue backed by a binary heap. Earlier deadlines come first, then higher priority, then smaller original entry ID. Editing preserves the ID. peek reads the front in O(1). Inserting and polling cost O(log n). Removing an arbitrary entry by value costs O(n). Displaying the ordered queue polls a copy because iterating a heap does not guarantee sorted order.
- Planner.java provides the task editor, queue, chronological study schedule, and completed-task view.
- The app saves each successful change to .priorq/tasks.xml under the operating system user's home directory. On Windows this is usually %USERPROFILE%\.priorq\tasks.xml. Task data stays outside the Git project. One study session can be scheduled per task.

## Build and test from the project root
These commands work in PowerShell and common Unix shells with a JDK on PATH. Create the bin folder first if needed.
```sh
javac -d bin src/priorq/Task.java src/priorq/TaskStore.java src/priorq/Planner.java src/priorq/Main.java test/priorq/TaskStoreTest.java
java -cp bin priorq.TaskStoreTest
java -cp bin priorq.Main
```
To rebuild the runnable JAR with application classes only, use a fresh output directory.
```sh
mkdir out
javac -d out src/priorq/Task.java src/priorq/TaskStore.java src/priorq/Planner.java src/priorq/Main.java
jar --create --file PriorQ.jar --main-class priorq.Main -C out .
```
