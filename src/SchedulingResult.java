import java.util.List;
import java.util.Map;

public class SchedulingResult {
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
