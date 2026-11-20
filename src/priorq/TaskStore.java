package priorq;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

final class TaskStore {
    private final Path file;
    private final Map<Long, Task> records = new LinkedHashMap<>();
    private final PriorityQueue<Task> queue = new PriorityQueue<>(Task.QUEUE_ORDER);

    TaskStore(Path file) throws IOException {
        this.file = file;
        if (Files.notExists(file)) return;
        Properties data = new Properties();
        try (InputStream in = Files.newInputStream(file)) { data.loadFromXML(in); }
        try {
            for (String key : data.stringPropertyNames()) {
                if (!key.endsWith(".title")) continue;
                String p = key.substring(0, key.length() - 5);
                long id = Long.parseLong(p.substring(0, p.length()-1));
                String study = data.getProperty(p+"study", "");
                Task t = new Task(id, data.getProperty(key), data.getProperty(p+"subject"),
                        Task.Kind.valueOf(data.getProperty(p+"kind")), LocalDate.parse(data.getProperty(p+"due")),
                        Task.Priority.valueOf(data.getProperty(p+"priority")), Task.Status.valueOf(data.getProperty(p+"status")),
                        study.isEmpty() ? null : LocalDateTime.parse(study));
                records.put(id,t);
                if (t.status()!=Task.Status.DONE) queue.offer(t);
            }
        } catch (RuntimeException ex) { throw new IOException("Invalid task data in "+file, ex); }
    }

    long nextId() { return records.keySet().stream().mapToLong(Long::longValue).max().orElse(0)+1; }
    Task next() { return queue.peek(); }
    List<Task> active() {
        PriorityQueue<Task> copy = new PriorityQueue<>(queue);
        List<Task> ordered = new ArrayList<>();
        while (!copy.isEmpty()) ordered.add(copy.poll());
        return ordered;
    }
    List<Task> completed() {
        return records.values().stream().filter(t -> t.status()==Task.Status.DONE)
                .sorted(Task.QUEUE_ORDER).toList();
    }
    List<Task> schedule() {
        return records.values().stream().filter(t -> t.study()!=null && t.status()!=Task.Status.DONE)
                .sorted(Comparator.comparing(Task::study).thenComparing(Task.QUEUE_ORDER)).toList();
    }

    void save(Task task) throws IOException { update(task.id(),task); }
    void delete(long id) throws IOException { update(id,null); }

    private void update(long id, Task replacement) throws IOException {
        Map<Long,Task> updated = new LinkedHashMap<>(records);
        if (replacement==null) updated.remove(id); else updated.put(id,replacement);
        Properties data = new Properties();
        for (Task t : updated.values()) {
            String p = t.id()+".";
            data.setProperty(p+"title",t.title());
            data.setProperty(p+"subject",t.subject());
            data.setProperty(p+"kind",t.kind().name());
            data.setProperty(p+"due",t.due().toString());
            data.setProperty(p+"priority",t.priority().name());
            data.setProperty(p+"status",t.status().name());
            if(t.study()!=null) data.setProperty(p+"study",t.study().toString());
        }
        Path temp = Files.createTempFile(file.getParent(),"priorq-", ".tmp");
        try {
            try(OutputStream out=Files.newOutputStream(temp)) { data.storeToXML(out,"PriorQ tasks","UTF-8"); }
            Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
        } finally { Files.deleteIfExists(temp); }
        Task old = records.remove(id);
        if(old!=null) queue.remove(old);
        if(replacement!=null) {
            records.put(id,replacement);
            if(replacement.status()!=Task.Status.DONE) queue.offer(replacement);
        }
    }
}
