package priorq;

import java.nio.file.*;
import java.time.*;
import java.util.*;

public class TaskStoreTest {
    private static void check(boolean result, String message) {
        if (!result) throw new AssertionError(message);
    }
    private static Task task(long id, String due, Task.Priority priority, String study) {
        return new Task(id,"Quiz "+id,"DSA",Task.Kind.Quiz,LocalDate.parse(due),priority,
                Task.Status.TODO,study==null?null:LocalDateTime.parse(study));
    }
    public static void main(String[] args) throws Exception {
        Path directory=Files.createTempDirectory("priorq-test");
        Path file=directory.resolve("tasks.xml");
        try {
            TaskStore store=new TaskStore(file);
            check(store.next()==null,"Empty queue");
            Task a=task(1,"2026-11-20",Task.Priority.Low,"2026-11-19T13:00");
            Task b=task(2,"2026-11-20",Task.Priority.High,"2026-11-19T09:00");
            Task c=task(3,"2026-11-20",Task.Priority.High,null);
            Task d=task(4,"2026-11-21",Task.Priority.High,null);
            for(Task t:List.of(d,c,b,a))store.save(t);
            check(store.active().equals(List.of(b,c,a,d)),"Deadline, priority, FIFO ties");
            check(store.schedule().equals(List.of(b,a)),"Scheduled time order");
            store.save(b.withStatus(Task.Status.ACTIVE));
            check(store.next().status()==Task.Status.ACTIVE,"Start task");
            store.save(b.withStatus(Task.Status.DONE));
            check(store.next().equals(c) && store.schedule().equals(List.of(a)),"Completion advances queue");
            store=new TaskStore(file);
            check(store.completed().equals(List.of(b.withStatus(Task.Status.DONE))),"Restart persistence");
            store.save(b);
            check(store.next().equals(b),"Reopen");
            store.save(task(4,"2026-11-18",Task.Priority.Low,null));
            check(store.next().id()==4,"Deadline change reorders queue");
            store.delete(4);
            check(new TaskStore(file).active().equals(List.of(b,c,a)),"Deletion persists");
            System.out.println("PASS: ordering, scheduling, updates, completion, reopening, deletion and persistence.");
        } finally {Files.deleteIfExists(file); Files.deleteIfExists(directory);}
    }
}
