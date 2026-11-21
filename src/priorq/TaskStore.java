package priorq;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

final class TaskStore {
    private final Path file;
    private final Map<Long, Task> tasks = new LinkedHashMap<>();
    private final PriorityQueue<Task> queue = new PriorityQueue<>(Task.QUEUE_ORDER);

    TaskStore(Path file) throws IOException {
        this.file = file.toAbsolutePath();
        if (Files.notExists(file)) return;

        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) { p.loadFromXML(in); }
        try {
            for (String key : p.stringPropertyNames()) {
                if (!key.endsWith(".title")) continue;
                String prefix = key.substring(0, key.length() - 5);
                long id = Long.parseLong(prefix.substring(0, prefix.length() - 1));
                String study = p.getProperty(prefix + "study", "");
                String finished = p.getProperty(prefix + "completedOn", "");
                Task t = new Task(id, p.getProperty(key), p.getProperty(prefix + "subject"),
                        Task.Kind.valueOf(p.getProperty(prefix + "kind")),
                        LocalDate.parse(p.getProperty(prefix + "due")),
                        Task.Priority.valueOf(p.getProperty(prefix + "priority")),
                        Task.Status.valueOf(p.getProperty(prefix + "status")),
                        study.isBlank() ? null : LocalDateTime.parse(study),
                        p.getProperty(prefix + "description", ""),
                        LocalTime.parse(p.getProperty(prefix + "dueTime", "23:59")),
                        Integer.parseInt(p.getProperty(prefix + "minutes", "60")),
                        finished.isBlank() ? null : LocalDate.parse(finished));
                tasks.put(id, t);
                if (t.status() != Task.Status.DONE) queue.offer(t);
            }
        } catch (RuntimeException ex) {
            throw new IOException("Could not read saved tasks in " + file, ex);
        }
    }

    long nextId() { return tasks.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1; }
    Task next() { return queue.peek(); }

    List<Task> active() {
        PriorityQueue<Task> copy = new PriorityQueue<>(queue);
        List<Task> result = new ArrayList<>();
        while (!copy.isEmpty()) result.add(copy.poll());
        return result;
    }

    List<Task> completed() {
        return tasks.values().stream().filter(t -> t.status() == Task.Status.DONE)
                .sorted(Comparator.comparing(Task::completedOn,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparingLong(Task::id)).toList();
    }

    List<Task> schedule() {
        return active().stream().filter(t -> t.study() != null)
                .sorted(Comparator.comparing(Task::study)).toList();
    }

    void save(Task t) throws IOException { update(t.id(), t); }
    void delete(long id) throws IOException { update(id, null); }

    private void update(long id, Task replacement) throws IOException {
        Map<Long, Task> updated = new LinkedHashMap<>(tasks);
        if (replacement == null) updated.remove(id);
        else updated.put(id, replacement);

        Properties data = new Properties();
        for (Task t : updated.values()) {
            String k = t.id() + ".";
            data.setProperty(k + "title", t.title());
            data.setProperty(k + "subject", t.subject());
            data.setProperty(k + "kind", t.kind().name());
            data.setProperty(k + "priority", t.priority().name());
            data.setProperty(k + "status", t.status().name());
            data.setProperty(k + "due", t.due().toString());
            data.setProperty(k + "dueTime", t.dueTime().toString());
            data.setProperty(k + "description", t.description());
            data.setProperty(k + "minutes", String.valueOf(t.minutes()));
            if (t.study() != null) data.setProperty(k + "study", t.study().toString());
            if (t.completedOn() != null) data.setProperty(k + "completedOn", t.completedOn().toString());
        }
        writeXml(file, data);
        Task old = tasks.remove(id);
        if (old != null) queue.remove(old);
        if (replacement != null) {
            tasks.put(id, replacement);
            if (replacement.status() != Task.Status.DONE) queue.offer(replacement);
        }
    }

    static void writeXml(Path file, Properties data) throws IOException {
        Files.createDirectories(file.getParent());
        Path temp = Files.createTempFile(file.getParent(), "priorq-", ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(temp)) {
                data.storeToXML(out, "PriorQ", "UTF-8");
            }
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}