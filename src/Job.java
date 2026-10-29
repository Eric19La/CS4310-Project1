public class Job {
    public final String name;
    public final int burstTime;
    public int remainingTime;
    public int completionTime;
    public int turnaroundTime;

    public Job(String name, int burstTime) {
        this.name = name;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }
}
