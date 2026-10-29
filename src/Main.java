import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        String filePath = args.length > 0 ? args[0] : "job.txt";
        List<Job> jobs = readJobs(filePath);

        if (jobs.isEmpty()) {
            System.out.println("No jobs found in " + filePath);
            return;
        }

        System.out.println("Loaded " + jobs.size() + " job(s) from " + filePath);
        System.out.println();

        List<Scheduler> schedulers = new ArrayList<>();
        schedulers.add(new FCFSScheduler());
        schedulers.add(new SJFScheduler());
        schedulers.add(new RRScheduler2());
        schedulers.add(new RRScheduler5());

        List<SchedulingResult> results = new ArrayList<>();
        for (Scheduler scheduler : schedulers) {
            SchedulingResult result = scheduler.run(jobs);
            results.add(result);
            printResult(result, jobs);
        }

        printSummary(results);
    }

    private static List<Job> readJobs(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filePath));
        List<Job> jobs = new ArrayList<>();

        String pendingName = null;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (pendingName == null) {
                pendingName = line;
            } else {
                int burst = Integer.parseInt(line);
                jobs.add(new Job(pendingName, burst));
                pendingName = null;
            }
        }

        if (jobs.size() > 30) {
            throw new IllegalArgumentException("Input file contains more than 30 jobs: " + jobs.size());
        }

        return jobs;
    }

    private static void printResult(SchedulingResult result, List<Job> jobs) {
        System.out.println("=".repeat(60));
        System.out.println(result.algorithmName);
        System.out.println("=".repeat(60));

        printGanttChart(result.timeline);
        System.out.println();

        System.out.printf("%-8s %-10s %-10s %-10s %-10s%n", "Job", "Burst", "Start", "Finish", "Turnaround");
        for (Job job : jobs) {
            int start = firstStart(result.timeline, job.name);
            int finish = result.completionTimes.get(job.name);
            int turnaround = result.turnaroundTimes.get(job.name);
            System.out.printf("%-8s %-10d %-10d %-10d %-10d%n", job.name, job.burstTime, start, finish, turnaround);
        }

        System.out.println();
        System.out.printf("Average Turnaround Time: %.2f%n", result.averageTurnaroundTime);
        System.out.println();
    }

    private static int firstStart(List<ExecutionSlice> timeline, String jobName) {
        for (ExecutionSlice slice : timeline) {
            if (slice.jobName.equals(jobName)) {
                return slice.start;
            }
        }
        return -1;
    }

    private static void printGanttChart(List<ExecutionSlice> timeline) {
        StringBuilder top = new StringBuilder();
        StringBuilder bottom = new StringBuilder();

        for (ExecutionSlice slice : timeline) {
            int width = Math.max(slice.jobName.length() + 2, String.valueOf(slice.end).length() + 1);
            top.append('|').append(center(slice.jobName, width));

            String startLabel = String.valueOf(slice.start);
            bottom.append(startLabel);
            bottom.append(" ".repeat(Math.max(0, width - startLabel.length())));
        }
        top.append('|');
        bottom.append(timeline.isEmpty() ? "" : timeline.get(timeline.size() - 1).end);

        System.out.println(top);
        System.out.println(bottom);
    }

    private static String center(String text, int width) {
        int totalPadding = width - text.length();
        int left = totalPadding / 2;
        int right = totalPadding - left;
        return " ".repeat(Math.max(0, left)) + text + " ".repeat(Math.max(0, right));
    }

    private static void printSummary(List<SchedulingResult> results) {
        System.out.println("=".repeat(60));
        System.out.println("Performance Summary (Average Turnaround Time)");
        System.out.println("=".repeat(60));
        for (SchedulingResult result : results) {
            System.out.printf("%-30s %.2f%n", result.algorithmName, result.averageTurnaroundTime);
        }
    }
}

/** A single job read from the input file. */
class Job {
    public final String name;
    public final int burstTime;
    public int remainingTime;

    public Job(String name, int burstTime) {
        this.name = name;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }
}

/** One contiguous block of CPU time given to a job, used to draw the Gantt chart. */
class ExecutionSlice {
    public final String jobName;
    public final int start;
    public final int end;

    public ExecutionSlice(String jobName, int start, int end) {
        this.jobName = jobName;
        this.start = start;
        this.end = end;
    }
}

/** The full outcome of running one scheduling algorithm over the job list. */
class SchedulingResult {
    public final String algorithmName;
    public final List<ExecutionSlice> timeline;
    public final Map<String, Integer> completionTimes;
    public final Map<String, Integer> turnaroundTimes;
    public final double averageTurnaroundTime;

    public SchedulingResult(String algorithmName, List<ExecutionSlice> timeline,
                             Map<String, Integer> completionTimes,
                             Map<String, Integer> turnaroundTimes,
                             double averageTurnaroundTime) {
        this.algorithmName = algorithmName;
        this.timeline = timeline;
        this.completionTimes = completionTimes;
        this.turnaroundTimes = turnaroundTimes;
        this.averageTurnaroundTime = averageTurnaroundTime;
    }
}

interface Scheduler {
    String getName();

    SchedulingResult run(List<Job> jobs);
}
