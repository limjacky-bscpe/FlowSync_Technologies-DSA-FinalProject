package priorq;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

record Task(long id, String title, String subject, Kind kind, LocalDate due,
            Priority priority, Status status, LocalDateTime study) {
    enum Kind { Assignment, Quiz, Project, Examination, Other }
    enum Priority { High, Medium, Low }
    enum Status {
        TODO("To do"), ACTIVE("In progress"), DONE("Completed");
        private final String label;
        Status(String label) { this.label = label; }
        public String toString() { return label; }
    }

    static final Comparator<Task> QUEUE_ORDER = Comparator.comparing(Task::due)
            .thenComparing(Task::priority).thenComparingLong(Task::id);

    Task {
        title = title.strip();
        subject = subject.strip();
        if (title.isEmpty() || subject.isEmpty())
            throw new IllegalArgumentException("Enter a task name and subject.");
        Objects.requireNonNull(kind);
        Objects.requireNonNull(due);
        Objects.requireNonNull(priority);
        Objects.requireNonNull(status);
    }

    Task withStatus(Status next) {
        return new Task(id, title, subject, kind, due, priority, next, study);
    }
}
