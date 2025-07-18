import java.util.*;

class CPMCalculator {
    private final List<Activity> activities;
    private double projectDuration;
    private final List<Activity> criticalPath;

    public CPMCalculator(List<Activity> activities) {
        // Create a copy and sort for deterministic processing, though topological sort is more robust for CPM
        this.activities = new ArrayList<>(activities);
        // A simple sort by name might not be sufficient for complex predecessor chains.
        // For robustness, consider implementing a topological sort or ensuring activities are added in an order that respects dependencies.
        // For this specific case (VRP activities), the naming convention and sequential nature often work with a simple sort.
        this.activities.sort(Comparator.comparing(Activity::getName));
        this.criticalPath = new ArrayList<>();
    }

    public void calculateCPM() {
        // --- Forward Pass: Calculate Early Start (ES) and Early Finish (EF) ---
        // We need to ensure activities are processed only when all their predecessors have their ES/EF calculated.
        // A simple iterative loop over all activities, repeated until no changes occur, can work for many cases.
        // For very complex dependency graphs, a topological sort (if no cycles) or Kahn's algorithm is more robust.
        boolean changed;
        do {
            changed = false;
            for (Activity activity : activities) {
                double newEarlyStart = 0;
                if (!activity.getPredecessors().isEmpty()) {
                    // Find the maximum Early Finish of all predecessors
                    double maxPredecessorEF = 0; // This variable is now local to its scope
                    boolean allPredecessorsCalculated = true; // Check if all predecessors have valid EF
                    for (Activity pred : activity.getPredecessors()) {
                        if (pred.getEarlyFinish() == 0 && pred.getDuration() > 0 && pred.getName().startsWith("V")) { // Crude check if not yet calculated and not Project_Start
                             allPredecessorsCalculated = false;
                             break;
                        }
                        maxPredecessorEF = Math.max(maxPredecessorEF, pred.getEarlyFinish());
                    }
                    if (allPredecessorsCalculated) {
                        newEarlyStart = maxPredecessorEF;
                    } else {
                        // If predecessors not calculated, skip this activity for now, it will be processed in a later iteration
                        continue;
                    }
                }

                if (activity.getEarlyStart() != newEarlyStart) {
                    activity.setEarlyStart(newEarlyStart);
                    activity.setEarlyFinish(activity.getEarlyStart() + activity.getDuration());
                    changed = true;
                }
            }
        } while (changed);


        // Determine Project Duration (Max Early Finish of all activities)
        projectDuration = 0;
        for (Activity activity : activities) {
            projectDuration = Math.max(projectDuration, activity.getEarlyFinish());
        }


        // --- Backward Pass: Calculate Late Start (LS) and Late Finish (LF) ---
        // Initialize Late Finish for activities with no successors to project duration
        for (Activity activity : activities) {
            // Assume initial LF is project duration, then narrow it down
            activity.setLateFinish(projectDuration);
            activity.setLateStart(projectDuration - activity.getDuration()); // Initial LS
        }

        // Iterate backward to refine LS/LF based on successors
        do {
            changed = false;
            for (int i = activities.size() - 1; i >= 0; i--) { // Iterate backwards
                Activity activity = activities.get(i);
                
                double minSuccessorLS = projectDuration; // Default if no successors

                // Find activities that have 'activity' as a predecessor (its successors)
                // and calculate their Late Start
                for (Activity successor : activities) {
                    if (successor.getPredecessors().contains(activity)) {
                        minSuccessorLS = Math.min(minSuccessorLS, successor.getLateStart());
                    }
                }

                double newLateFinish = minSuccessorLS;
                if (Math.abs(activity.getLateFinish() - newLateFinish) > 0.001) { // Compare with tolerance
                    activity.setLateFinish(newLateFinish);
                    activity.setLateStart(activity.getLateFinish() - activity.getDuration());
                    changed = true;
                }
            }
        } while (changed);


        // --- Calculate Slack and Identify Critical Path ---
        criticalPath.clear();
        for (Activity activity : activities) {
            activity.setSlack(activity.getLateStart() - activity.getEarlyStart());
            // Activities on the critical path have zero slack (within a small tolerance for doubles)
            if (Math.abs(activity.getSlack()) < 0.001) {
                criticalPath.add(activity);
            }
        }
        // Sort the critical path activities by their Early Start time for clear display
        criticalPath.sort(Comparator.comparingDouble(Activity::getEarlyStart));
    }

    public double getProjectDuration() {
        return projectDuration;
    }

    public List<Activity> getCriticalPath() {
        return criticalPath;
    }

    public void printResults() {
        System.out.println("\n--- Hasil Analisis CPM ---");
        System.out.println("Durasi Proyek Keseluruhan: " + String.format("%.2f", projectDuration) + " menit");
        System.out.println("\nDaftar Aktivitas:");
        System.out.println(String.format("%-40s | %7s | %7s | %7s | %7s | %7s | %7s",
                                         "Nama Aktivitas", "Durasi", "ES", "EF", "LS", "LF", "Slack"));
        System.out.println("------------------------------------------+---------+---------+---------+---------+---------+---------");
        // Sort activities by ES for better readability in the output
        activities.sort(Comparator.comparingDouble(Activity::getEarlyStart));
        for (Activity activity : activities) {
            System.out.println(activity);
        }
        System.out.println("\nJalur Kritis (Critical Path):");
        if (criticalPath.isEmpty()) {
            System.out.println("Tidak ada jalur kritis ditemukan (mungkin proyek kosong, durasi nol, atau ada masalah perhitungan).");
        } else {
            StringBuilder cpString = new StringBuilder();
            for (int i = 0; i < criticalPath.size(); i++) {
                cpString.append(criticalPath.get(i).getName());
                if (i < criticalPath.size() - 1) {
                    cpString.append(" -> ");
                }
            }
            System.out.println(cpString.toString());
        }
        System.out.println("---------------------------");
    }
}