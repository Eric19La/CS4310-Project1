import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FCFSScheduler implements Scheduler {
    @Override
    public String getName() {
        return "First-Come-First-Serve (FCFS)";
    }

    @Override
    public SchedulingResult run(List<Job> jobs) {
        List<ExecutionSlice> timeline = new ArrayList<>();
        Map<String, Integer> completionTimes = new LinkedHashMap<>();
        Map<String, Integer> turnaroundTimes = new LinkedHashMap<>();

        int clock = 0;
        int totalTurnaround = 0;

        for (Job job : jobs) {
            int start = clock;
            int end = clock + job.burstTime;
            timeline.add(new ExecutionSlice(job.name, start, end));

            clock = end;
            completionTimes.put(job.name, clock);
            int turnaround = clock; // arrival time is 0 for all jobs
            turnaroundTimes.put(job.name, turnaround);
            totalTurnaround += turnaround;
        }

        double average = jobs.isEmpty() ? 0 : (double) totalTurnaround / jobs.size();
        return new SchedulingResult(getName(), timeline, completionTimes, turnaroundTimes, average);
    }
}
