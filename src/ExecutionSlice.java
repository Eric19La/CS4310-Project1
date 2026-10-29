public class ExecutionSlice {
    public final String jobName;
    public final int start;
    public final int end;

    public ExecutionSlice(String jobName, int start, int end) {
        this.jobName = jobName;
        this.start = start;
        this.end = end;
    }
}
