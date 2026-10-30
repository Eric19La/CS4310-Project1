import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * First-Come-First-Serve (FCFS) CPU scheduling simulator.
 *
 * Reads jobs (name + burst time in ms) from an input file (default job.txt,
 * or a path passed as the first command-line argument) and runs them in the
 * exact order they appear in the file. All jobs are assumed to arrive at
 * time 0, per the project assumptions, so each job's turnaround time equals
 * its completion time.
 *
 * Usage:
 *   javac FCFS.java
 *   java FCFS [path-to-job-file]
 */
public class FCFS {

    /** A single job read from the input file. */
    static class Job {
        final String name;
        final int burstTime;

        Job(String name, int burstTime) {
            this.name = name;
            this.burstTime = burstTime;
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
        System.out.println("First-Come-First-Serve (FCFS)");
        System.out.println("=".repeat(60));

        List<ExecutionSlice> timeline = new ArrayList<>();
        int[] startTimes = new int[jobs.size()];
        int[] finishTimes = new int[jobs.size()];
        int[] turnaroundTimes = new int[jobs.size()];

        int clock = 0;
        int totalTurnaround = 0;
        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            int start = clock;
            int end = clock + job.burstTime;
            timeline.add(new ExecutionSlice(job.name, start, end));

            clock = end;
            startTimes[i] = start;
            finishTimes[i] = end;
            turnaroundTimes[i] = end; // arrival time is 0 for all jobs
            totalTurnaround += end;
        }

        printGanttChart(timeline);
        System.out.println();
        printTable(jobs, startTimes, finishTimes, turnaroundTimes);

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

    private static void printTable(List<Job> jobs, int[] startTimes, int[] finishTimes, int[] turnaroundTimes) {
        System.out.printf("%-8s %-10s %-10s %-10s %-10s%n", "Job", "Burst", "Start", "Finish", "Turnaround");
        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            System.out.printf("%-8s %-10d %-10d %-10d %-10d%n",
                    job.name, job.burstTime, startTimes[i], finishTimes[i], turnaroundTimes[i]);
        }
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
