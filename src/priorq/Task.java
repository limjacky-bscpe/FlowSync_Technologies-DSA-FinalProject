package priorq;

import java.time.*;
import java.util.Comparator;
import java.util.Objects;

record Task(long id, String title, String subject, Kind kind, LocalDate due,
            Priority priority, Status status, LocalDateTime study,
            String description, LocalTime dueTime, int minutes, LocalDate completedOn) {

    enum Kind { Assignment, Quiz, Project, Examination, Other }
    enum Priority { High, Medium, Low }
    enum Status {
        TODO("To do"), ACTIVE("In progress"), DONE("Completed");
        final String label;
        Status(String label) { this.label = label; }
        public String toString() { return label; }
    }

    static final Comparator<Task> QUEUE_ORDER = Comparator.comparing(Task::due)
            .thenComparing(Task::dueTime).thenComparing(Task::priority)
            .thenComparingLong(Task::id);

    Task {
        title = title.strip();
        subject = subject.strip();
        if (title.isEmpty() || subject.isEmpty())
            throw new IllegalArgumentException("Enter a task title and subject.");
        Objects.requireNonNull(due);
        Objects.requireNonNull(dueTime);
        Objects.requireNonNull(kind);
        Objects.requireNonNull(priority);
        Objects.requireNonNull(status);
        if (minutes < 1 || minutes > 1440)
            throw new IllegalArgumentException("Choose a duration from 1 to 1440 minutes.");
        description = description == null ? "" : description.strip();
        if (status != Status.DONE) completedOn = null;
    }

    Task(long id, String title, String subject, Kind kind, LocalDate due,
         Priority priority, Status status, LocalDateTime study) {
        this(id, title, subject, kind, due, priority, status, study,
                "", LocalTime.of(23, 59), 60, null);
    }

    Task withStatus(Status next) {
        LocalDate finished = next == Status.DONE
                ? (status == Status.DONE ? completedOn : LocalDate.now()) : null;
        return new Task(id, title, subject, kind, due, priority, next, study,
                description, dueTime, minutes, finished);
    }
}