import java.util.List;

public interface Scheduler {
    String getName();

    SchedulingResult run(List<Job> jobs);
}
