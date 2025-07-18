import java.awt.*;          // For GUI components
import java.util.*;         // For collections (List, Set, etc.)
import java.util.List;      // Specifically for List interface
import javax.swing.*;       // For Swing GUI components (JFrame, JPanel)

// Java program to visualize Vehicle Routing Problem (VRP) solution
public class VRPSolverVisualizer2 extends JPanel {

    // Data structure for geographic points (latitude, longitude)
    static class GeoPoint {
        double lat, lon;
        String label;
        double serviceDuration; // Durasi pelayanan di lokasi ini (misal dalam menit)

        public GeoPoint(double lat, double lon, String label, double serviceDuration) {
            this.lat = lat;
            this.lon = lon;
            this.label = label;
            this.serviceDuration = serviceDuration;
        }
        public GeoPoint(double lat, double lon, String label) { // Konstruktor untuk depot atau jika serviceDuration 0
            this(lat, lon, label, 0);
        }
    }

    // Data structure for screen points (x, y coordinates)
    static class Point {
        int x, y;
        String label;
        public Point(int x, int y, String label) {
            this.x = x;
            this.y = y;
            this.label = label;
        }
    }

    // Input vehicle count
    private final int vehicleCount = 5; // Can be changed

    // NEW COORDINATE DATA WITH ID 0 AS DEPOT
    // Tambahkan serviceDuration (dalam menit) untuk setiap lokasi
    private final List<GeoPoint> geoPoints = List.of(
        new GeoPoint(-7.28873756303259, 112.75604179350436, "Learning Center", 0), // ID 0 - DEPOT (serviceDuration 0)
        new GeoPoint(-7.288712497679808, 112.7719717006841, "SDN 5 Ngagelrejo", 15), // Asumsi 15 menit pelayanan
        new GeoPoint(-7.288031336496422, 112.76632463768294, "SMPN 48 Surabaya", 20),
        new GeoPoint(-7.277031737190226, 112.75338165977935, "SD Santa Maria", 10),
        new GeoPoint(-7.289393520541995, 112.75057049755106, "SMPN 12 Surabaya", 15),
        new GeoPoint(-7.275798135899215, 112.75702213799651, "SMA Muhammadiyah 2", 25),
        new GeoPoint(-7.274666497558661, 112.75949512399993, "SMP Muhammadiyah 5", 20),
        new GeoPoint(-7.271183626600889, 112.76011400263659, "sdn 5 kertajaya", 10),
        new GeoPoint(-7.271190881944807, 112.7600862804245, "sdn 1 kertajaya", 10),
        new GeoPoint(-7.265061750278144, 112.75836417530663, "smpn 6 surabaya", 20),
        new GeoPoint(-7.264785590924197, 112.76008064603957, "SMP GIKI 2 Surabaya", 15),
        new GeoPoint(-7.277066493641427, 112.76037701768822, "SDN 4 Kertajaya", 10),
        new GeoPoint(-7.29108110996841, 112.769917637683, "SDN 3 Ngagelrejo", 15),
        new GeoPoint(-7.287675685714761, 112.77259020478759, "SDN 1 Ngagelrejo", 10),
        new GeoPoint(-7.291439998188159, 112.76989268383358, "Anak Bangsa Christian School", 25),
        new GeoPoint(-7.297399432650868, 112.7552156359125, "SD Muhammadiyah 16", 20),
        new GeoPoint(-7.287012727142718, 112.76015415112313, "Yayasan Santa Clara", 15),
        new GeoPoint(-7.28436287327013, 112.77337207718494, "SDN 5 Klampis Ngasem", 10),
        new GeoPoint(-7.2704493035595945, 112.75547312786144, "SDN Airlangga I", 15),
        new GeoPoint(-7.299644875492887, 112.7575792304767, "SDN Baratajaya", 10),
        new GeoPoint(-7.297122176520045, 112.76884819971036, "SMA 17 Agustus", 25),
        new GeoPoint(-7.280574218242535, 112.76161327284628, "Yayasan Maryam", 15),
        new GeoPoint(-7.283170940280327, 112.75951042095116, "SD Muhammadiyah 4", 20),
        new GeoPoint(-7.289819920523529, 112.76852341347396, "Little Sun School", 10),
        new GeoPoint(-7.2838520452227495, 112.76805057447031, "SDN Manyar Sabrangan", 15),
        new GeoPoint(-7.268916770213828, 112.74371432350654, "Blue Dolphin Playskool", 20)
    );

    private final List<Point> points = new ArrayList<>();
    private final List<List<Integer>> vehicleRoutes = new ArrayList<>();
    
    // Matriks waktu tempuh antar lokasi dalam menit
    private double[][] travelTimes; 
    private final double AVERAGE_SPEED_KM_PER_HOUR = 20.0; // Kecepatan rata-rata asumsi

    // Initialization of the VRP visualizer
    public VRPSolverVisualizer2() {
        solveVRP();
        printVehicleRoutes(); // Print routes with total duration
        analyzeCPM(); // Perform CPM analysis
    }

    // Fungsi Haversine untuk menghitung jarak great-circle antara dua titik geografis (dalam KM)
    private double haversineDistance(GeoPoint p1, GeoPoint p2) {
        final int R = 6371; // Radius bumi dalam kilometer

        double latDistance = Math.toRadians(p2.lat - p1.lat);
        double lonDistance = Math.toRadians(p2.lon - p1.lon);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(p1.lat)) * Math.cos(Math.toRadians(p2.lat))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Jarak dalam kilometer
    }

    // Method to solve the VRP and visualize the routes
    private void solveVRP() {
        // 1. Convert geographic coordinates to screen positions
        double minLat = geoPoints.stream().mapToDouble(p -> p.lat).min().getAsDouble();
        double maxLat = geoPoints.stream().mapToDouble(p -> p.lat).max().getAsDouble();
        double minLon = geoPoints.stream().mapToDouble(p -> p.lon).min().getAsDouble();
        double maxLon = geoPoints.stream().mapToDouble(p -> p.lon).max().getAsDouble();

        int width = 800, height = 600;
        int padding = 50;
        int drawableWidth = width - 2 * padding;
        int drawableHeight = height - 2 * padding;

        for (GeoPoint gp : geoPoints) {
            int x = (int) ((gp.lon - minLon) / (maxLon - minLon) * drawableWidth) + padding;
            int y = (int) ((maxLat - gp.lat) / (maxLat - minLat) * drawableHeight) + padding;
            points.add(new Point(x, y, gp.label));
        }

        // 2. Calculate the travel time matrix using Haversine distance (NOT Euclidean)
        travelTimes = new double[geoPoints.size()][geoPoints.size()];
        for (int i = 0; i < geoPoints.size(); i++) {
            for (int j = 0; j < geoPoints.size(); j++) {
                if (i == j) {
                    travelTimes[i][j] = 0;
                } else {
                    double distanceKm = haversineDistance(geoPoints.get(i), geoPoints.get(j));
                    // Convert distance (km) to time (minutes)
                    travelTimes[i][j] = (distanceKm / AVERAGE_SPEED_KM_PER_HOUR) * 60; 
                }
            }
        }

        // 3. Define Depot and Customer Points
        int depotIndex = 0; // Depot is Learning Center (ID 0)
        List<Integer> customerIndices = new ArrayList<>();
        for (int i = 0; i < geoPoints.size(); i++) {
            if (i != depotIndex) {
                customerIndices.add(i);
            }
        }

        // --- 4. Fixed Optimal Routes (Simulated Output) ---
        vehicleRoutes.clear();

        List<List<Integer>> assignedCustomersPerVehicle = new ArrayList<>();
        for (int i = 0; i < vehicleCount; i++) {
            assignedCustomersPerVehicle.add(new ArrayList<>());
        }

        for (int i = 0; i < customerIndices.size(); i++) {
            int customerId = customerIndices.get(i);
            assignedCustomersPerVehicle.get(i % vehicleCount).add(customerId);
        }
        
        for (List<Integer> assigned : assignedCustomersPerVehicle) {
            List<Integer> finalRoute = new ArrayList<>();
            finalRoute.add(depotIndex); // Start at depot
            finalRoute.addAll(assigned); // Add assigned customers
            finalRoute.add(depotIndex); // Return to depot
            vehicleRoutes.add(finalRoute);
        }
    }

    // Print vehicle routes to console with total duration
    private void printVehicleRoutes() {
        System.out.println("--- Daftar Rute Kendaraan ---");
        for (int i = 0; i < vehicleRoutes.size(); i++) {
            StringBuilder routeString = new StringBuilder();
            double totalRouteDuration = 0; 
            routeString.append("Vehicle ").append(i + 1).append(": ");
            List<Integer> route = vehicleRoutes.get(i);
            for (int j = 0; j < route.size(); j++) {
                int pointIndex = route.get(j);
                routeString.append(geoPoints.get(pointIndex).label);

                // Add service duration at each customer point
                // Only add if it's a customer (not depot at start/end) and has a service duration
                if (pointIndex != 0 && j > 0 && j < route.size() - 1) { 
                    totalRouteDuration += geoPoints.get(pointIndex).serviceDuration;
                }

                if (j < route.size() - 1) {
                    int nextPointIndex = route.get(j + 1);
                    totalRouteDuration += travelTimes[pointIndex][nextPointIndex]; 
                    routeString.append(" - ");
                }
            }
            System.out.println(routeString.toString() + " (Total Duration: " + String.format("%.2f", totalRouteDuration) + " minutes)");
        }
        System.out.println("-----------------------------");
    }

    // NEW METHOD: Analyze CPM based on generated routes
    private void analyzeCPM() {
        List<Activity> projectActivities = new ArrayList<>();
        Map<String, Activity> activityMap = new HashMap<>(); 

        // 1. Define "Project Start" activity
        Activity projectStart = new Activity("Project_Start", 0);
        projectActivities.add(projectStart);
        activityMap.put(projectStart.getName(), projectStart);

        List<Activity> vehicleLastActivities = new ArrayList<>(); // To track the last activity for each vehicle

        // 2. Create activities for each vehicle's route
        for (int v = 0; v < vehicleRoutes.size(); v++) {
            List<Integer> route = vehicleRoutes.get(v);
            Activity lastActivityForVehicle = projectStart; // All vehicles start after "Project Start"

            // Loop through each segment of the vehicle's route
            for (int j = 0; j < route.size() - 1; j++) {
                int currentPointIndex = route.get(j);
                int nextPointIndex = route.get(j + 1);
                
                // --- Travel Activity ---
                String travelActivityName = "V" + (v + 1) + "_Travel_" + currentPointIndex + "_" + nextPointIndex;
                double travelDuration = travelTimes[currentPointIndex][nextPointIndex];

                Activity travelActivity = activityMap.get(travelActivityName);
                if (travelActivity == null) { // Create if not exists (e.g., if a segment is reused, though unlikely here)
                    travelActivity = new Activity(travelActivityName, travelDuration);
                    projectActivities.add(travelActivity);
                    activityMap.put(travelActivityName, travelActivity);
                }
                travelActivity.addPredecessor(lastActivityForVehicle);
                lastActivityForVehicle = travelActivity;

                // --- Service Activity (if not returning to depot and service duration > 0) ---
                if (nextPointIndex != 0 && geoPoints.get(nextPointIndex).serviceDuration > 0) { 
                    String serviceActivityName = "V" + (v + 1) + "_Service_" + nextPointIndex;
                    double serviceDur = geoPoints.get(nextPointIndex).serviceDuration;
                    
                    Activity serviceActivity = activityMap.get(serviceActivityName);
                    if (serviceActivity == null) {
                        serviceActivity = new Activity(serviceActivityName, serviceDur);
                        projectActivities.add(serviceActivity);
                        activityMap.put(serviceActivityName, serviceActivity);
                    }
                    serviceActivity.addPredecessor(lastActivityForVehicle);
                    lastActivityForVehicle = serviceActivity;
                }
            }
            vehicleLastActivities.add(lastActivityForVehicle); // Store the last activity for this vehicle's route
        }

        // 3. Define "Project End" activity and link all vehicle's last activities to it
        Activity projectEnd = new Activity("Project_End", 0);
        for (Activity endAct : vehicleLastActivities) {
            projectEnd.addPredecessor(endAct);
        }
        projectActivities.add(projectEnd);
        activityMap.put(projectEnd.getName(), projectEnd);

        // 4. Perform CPM Calculation
        CPMCalculator cpm = new CPMCalculator(projectActivities);
        cpm.calculateCPM();
        cpm.printResults(); // Print CPM results to console
    }


    @Override
    // Override the paintComponent method to draw points and routes
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawPoints(g);
        drawRoutes(g);
    }

    // Helper method to draw points on the panel
    private void drawPoints(Graphics g) {
        for (Point p : points) {
            if (p.label.equals("Learning Center")) { // Check for the depot label
                g.setColor(Color.RED);
                g.fillOval(p.x - 6, p.y - 6, 12, 12);
            } else {
                g.setColor(Color.BLACK);
                g.fillOval(p.x - 4, p.y - 4, 8, 8);
            }
            g.drawString(p.label, p.x + 6, p.y);
        }
    }

    // Helper method to draw routes on the panel
    private void drawRoutes(Graphics g) {
        Color[] colors = {Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.CYAN, Color.PINK, Color.GRAY, Color.YELLOW.darker(), Color.RED.darker(), Color.BLUE.darker()};
        int i = 0;
        for (List<Integer> route : vehicleRoutes) {
            g.setColor(colors[i % colors.length]);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(2)); // Make lines thicker

            for (int j = 0; j < route.size() - 1; j++) {
                Point p1 = points.get(route.get(j));
                Point p2 = points.get(route.get(j + 1));
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            i++;
        }
    }

    // Main method to run the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("VRP - Auto Routing & CPM Visualizer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.add(new VRPSolverVisualizer2()); // Add the custom JPanel to the frame
            frame.setVisible(true);
        });
    }
}