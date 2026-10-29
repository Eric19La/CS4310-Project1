import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Round-Robin with time quantum = 2. */
public class RRScheduler2 extends RoundRobinScheduler {
    public RRScheduler2() {
        super(2);
    }
}

/**
 * Shared Round-Robin logic parameterized by time quantum. RRScheduler2 and
 * RRScheduler5 both extend this with their own fixed quantum.
 */
abstract class RoundRobinScheduler implements Scheduler {
    private final int timeQuantum;

    protected RoundRobinScheduler(int timeQuantum) {
        this.timeQuantum = timeQuantum;
    }

    @Override
    public String getName() {
        return "Round-Robin (RR-" + timeQuantum + ")";
    }

    @Override
    public SchedulingResult run(List<Job> jobs) {
        List<Job> copies = new ArrayList<>();
        for (Job job : jobs) {
            copies.add(new Job(job.name, job.burstTime));
        }

        Deque<Job> queue = new ArrayDeque<>(copies);
        List<ExecutionSlice> timeline = new ArrayList<>();
        Map<String, Integer> completionTimes = new LinkedHashMap<>();
        Map<String, Integer> turnaroundTimes = new LinkedHashMap<>();

        int clock = 0;
        int totalTurnaround = 0;

        while (!queue.isEmpty()) {
            Job job = queue.poll();
            int runTime = Math.min(timeQuantum, job.remainingTime);
            int start = clock;
            int end = clock + runTime;

            timeline.add(new ExecutionSlice(job.name, start, end));

            clock = end;
            job.remainingTime -= runTime;

            if (job.remainingTime > 0) {
                queue.offer(job);
            } else {
                completionTimes.put(job.name, clock);
                int turnaround = clock;
                turnaroundTimes.put(job.name, turnaround);
                totalTurnaround += turnaround;
            }
        }

        List<ExecutionSlice> merged = mergeAdjacentSlices(timeline);

        double average = copies.isEmpty() ? 0 : (double) totalTurnaround / copies.size();
        return new SchedulingResult(getName(), merged, completionTimes, turnaroundTimes, average);
    }

    private List<ExecutionSlice> mergeAdjacentSlices(List<ExecutionSlice> timeline) {
        List<ExecutionSlice> merged = new ArrayList<>();
        for (ExecutionSlice slice : timeline) {
            if (!merged.isEmpty()) {
                ExecutionSlice last = merged.get(merged.size() - 1);
                if (last.jobName.equals(slice.jobName) && last.end == slice.start) {
                    merged.set(merged.size() - 1, new ExecutionSlice(last.jobName, last.start, slice.end));
                    continue;
                }
            }
            merged.add(slice);
        }
        return merged;
    }
}
