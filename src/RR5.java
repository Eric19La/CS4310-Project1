import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Round-Robin CPU scheduling simulator with time quantum = 5 ms (RR-5).
 *
 * Reads jobs (name + burst time in ms) from an input file (default job.txt,
 * or a path passed as the first command-line argument) and processes them in
 * a FIFO queue, giving each job at most TIME_QUANTUM ms per turn. A job that
 * still has remaining burst time is sent to the back of the queue; a job
 * that finishes is recorded with its completion/turnaround time. All jobs
 * are assumed to arrive at time 0.
 *
 * Usage:
 *   javac RR5.java
 *   java RR5 [path-to-job-file]
 */
public class RR5 {

    private static final int TIME_QUANTUM = 5;

    /** A single job read from the input file; tracks remaining burst time as it runs. */
    static class Job {
        final String name;
        final int burstTime;
        int remainingTime;

        Job(String name, int burstTime) {
            this.name = name;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
        }
    }

    /** One contiguous block of CPU time given to a job, used to draw the Gantt chart. */
    static class ExecutionSlice {
        final String jobName;
        final int start;
        final int end;

        ExecutionSlice(String jobName, int start, int end) {
            this.jobName = jobName;
            this.start = start;
            this.end = end;
        }
    }

    public static void main(String[] args) throws IOException {
        String filePath = args.length > 0 ? args[0] : "job.txt";
        List<Job> jobs = readJobs(filePath);

        if (jobs.isEmpty()) {
            System.out.println("No jobs found in " + filePath);
            return;
        }

        System.out.println("Loaded " + jobs.size() + " job(s) from " + filePath);
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("Round-Robin (RR-" + TIME_QUANTUM + ")");
        System.out.println("=".repeat(60));

        Deque<Job> queue = new ArrayDeque<>(jobs);
        List<ExecutionSlice> timeline = new ArrayList<>();
        Map<String, Integer> finishTimes = new LinkedHashMap<>();
        Map<String, Integer> turnaroundTimes = new LinkedHashMap<>();

        int clock = 0;
        int totalTurnaround = 0;
        while (!queue.isEmpty()) {
            Job job = queue.poll();
            int runTime = Math.min(TIME_QUANTUM, job.remainingTime);
            int start = clock;
            int end = clock + runTime;

            timeline.add(new ExecutionSlice(job.name, start, end));

            clock = end;
            job.remainingTime -= runTime;

            if (job.remainingTime > 0) {
                queue.offer(job);
            } else {
                finishTimes.put(job.name, clock);
                turnaroundTimes.put(job.name, clock); // arrival time is 0 for all jobs
                totalTurnaround += clock;
            }
        }

        List<ExecutionSlice> merged = mergeAdjacentSlices(timeline);
        printGanttChart(merged);
        System.out.println();
        printTable(jobs, timeline, finishTimes, turnaroundTimes);

        double average = (double) totalTurnaround / jobs.size();
        System.out.println();
        System.out.printf("Average Turnaround Time: %.2f%n", average);
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

    private static void printTable(List<Job> jobs, List<ExecutionSlice> timeline,
                                    Map<String, Integer> finishTimes, Map<String, Integer> turnaroundTimes) {
        System.out.printf("%-8s %-10s %-10s %-10s %-10s%n", "Job", "Burst", "Start", "Finish", "Turnaround");
        for (Job job : jobs) {
            int start = firstStart(timeline, job.name);
            int finish = finishTimes.get(job.name);
            int turnaround = turnaroundTimes.get(job.name);
            System.out.printf("%-8s %-10d %-10d %-10d %-10d%n", job.name, job.burstTime, start, finish, turnaround);
        }
    }

    private static int firstStart(List<ExecutionSlice> timeline, String jobName) {
        for (ExecutionSlice slice : timeline) {
            if (slice.jobName.equals(jobName)) {
                return slice.start;
            }
        }
        return -1;
    }

    private static List<ExecutionSlice> mergeAdjacentSlices(List<ExecutionSlice> timeline) {
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
}
