import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Non-preemptive Shortest-Job-First. Since every job arrives at time 0,
 * this reduces to sorting jobs by burst time, breaking ties by the order
 * jobs appear in the input file.
 */
public class SJFScheduler implements Scheduler {
    @Override
    public String getName() {
        return "Shortest-Job-First (SJF)";
    }

    @Override
    public SchedulingResult run(List<Job> jobs) {
        List<Job> order = new ArrayList<>(jobs);
        order.sort((a, b) -> {
            if (a.burstTime != b.burstTime) {
                return Integer.compare(a.burstTime, b.burstTime);
            }
            return Integer.compare(jobs.indexOf(a), jobs.indexOf(b));
        });

        List<ExecutionSlice> timeline = new ArrayList<>();
        Map<String, Integer> completionTimes = new LinkedHashMap<>();
        Map<String, Integer> turnaroundTimes = new LinkedHashMap<>();

        int clock = 0;
        int totalTurnaround = 0;

        for (Job job : order) {
            int start = clock;
            int end = clock + job.burstTime;
            timeline.add(new ExecutionSlice(job.name, start, end));

            clock = end;
            completionTimes.put(job.name, clock);
            int turnaround = clock;
            turnaroundTimes.put(job.name, turnaround);
            totalTurnaround += turnaround;
        }

        double average = order.isEmpty() ? 0 : (double) totalTurnaround / order.size();
        return new SchedulingResult(getName(), timeline, completionTimes, turnaroundTimes, average);
    }
}
