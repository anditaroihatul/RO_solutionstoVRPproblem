import java.util.ArrayList;
import java.util.List;

class Activity {
    String name;
    double duration;
    List<Activity> predecessors; // Aktivitas yang harus selesai sebelum aktivitas ini dimulai
    double earlyStart, earlyFinish, lateStart, lateFinish, slack;

    public Activity(String name, double duration) {
        this.name = name;
        this.duration = duration;
        this.predecessors = new ArrayList<>();
    }

    public void addPredecessor(Activity predecessor) {
        this.predecessors.add(predecessor);
    }

    // Getter methods
    public String getName() { return name; }
    public double getDuration() { return duration; }
    public List<Activity> getPredecessors() { return predecessors; }
    public double getEarlyStart() { return earlyStart; }
    public double getEarlyFinish() { return earlyFinish; }
    public double getLateStart() { return lateStart; }
    public double getLateFinish() { return lateFinish; }
    public double getSlack() { return slack; }

    // Setter methods (digunakan oleh CPMCalculator)
    public void setEarlyStart(double earlyStart) { this.earlyStart = earlyStart; }
    public void setEarlyFinish(double earlyFinish) { this.earlyFinish = earlyFinish; }
    public void setLateStart(double lateStart) { this.lateStart = lateStart; }
    public void setLateFinish(double lateFinish) { this.lateFinish = lateFinish; }
    public void setSlack(double slack) { this.slack = slack; }

    @Override
    public String toString() {
        return String.format("%-40s | Dur: %7.2f | ES: %7.2f | EF: %7.2f | LS: %7.2f | LF: %7.2f | Slack: %7.2f",
                             name, duration, earlyStart, earlyFinish, lateStart, lateFinish, slack);
    }
}