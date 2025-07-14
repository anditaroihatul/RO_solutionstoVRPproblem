import java.awt.*;  //for GUI components
import java.util.*;    //for drawing graphics
import java.util.List;  //collections for data handling
import javax.swing.*;  //for List interface

    //java program to visalize Vehicle Routing Problem (VRP) solution
public class VRPSolverVisualizer extends JPanel {
    
    // data structure geographic points
    static class GeoPoint {
        double lat, lon;
        String label;
        public GeoPoint(double lat, double lon, String label) {
            this.lat = lat;
            this.lon = lon;
            this.label = label;
        }
    }
    //points
    static class Point {
        int x, y;
        String label;
        public Point(int x, int y, String label) {
            this.x = x;
            this.y = y;
            this.label = label;
        }
    }

    // input vehicle count
    private final int vehicleCount = 3;
   
    // coordinate data with labels
    private final List<GeoPoint> geoPoints = List.of(
        new GeoPoint(-7.28436287327013,   112.77337207718494, "SDN 5 Klampis Ngasem"),
        new GeoPoint(-7.2838520452227495, 112.76805057447031, "SDN Manyar Sabrangan"),
        new GeoPoint(-7.289819920523529,  112.76852341347396, "Little Sun School"),
        new GeoPoint(-7.287470397796173,  112.76015415112313, "Yayasan Santa Clara"),
        new GeoPoint(-7.283170940280327,  112.75951042095116, "SD Muhammadiyah 4 Surabaya"),
        new GeoPoint(-7.280574218242535,  112.76161327284628, "Yayasan Maryam"),
        new GeoPoint(-7.297122176520045,  112.76884819971036, "SMA 17 Agustus Surabaya"),
        new GeoPoint(-7.299644875492887,  112.7575792304767,  "SDN Baratajaya"),
        new GeoPoint(-7.268916770213828,  112.74371432350654, "Blue Dolphin Playskool"),
        new GeoPoint(-7.2704493035595945, 112.75547312786144, "SDN Airlangga 1"),
        new GeoPoint(-7.297352924631167,  112.7552156359125,  "SD Muhammadiyah 16"),
        new GeoPoint(-7.291563674206592,  112.76989268383358, "Anak Bangsa Christian School"),
        new GeoPoint(-7.288723349628001,  112.75607027664445, "Learning Center")
    );
    //point and route
    private List<Point> points = new ArrayList<>();
    private List<List<Integer>> vehicleRoutes = new ArrayList<>();
   
    //initialization of the VRP visualizer
    //main constructor
    public VRPSolverVisualizer() {
        solveVRP();
    }

    // method to solve the VRP and visualize the routes    
    private void solveVRP() {
        //  convert coordinates to screen positions
        double minLat = geoPoints.stream().mapToDouble(p -> p.lat).min().getAsDouble();
        double maxLat = geoPoints.stream().mapToDouble(p -> p.lat).max().getAsDouble();
        double minLon = geoPoints.stream().mapToDouble(p -> p.lon).min().getAsDouble();
        double maxLon = geoPoints.stream().mapToDouble(p -> p.lon).max().getAsDouble();

        int width = 800, height = 600;
        for (GeoPoint gp : geoPoints) {
            int x = (int) ((gp.lon - minLon) / (maxLon - minLon) * (width - 100)) + 50;
            int y = (int) ((maxLat - gp.lat) / (maxLat - minLat) * (height - 100)) + 50;
            points.add(new Point(x, y, gp.label));
        }

        //  dsitribute delivery points to vehicles
        int depotIndex = geoPoints.size() - 1; // Last point = Depot
        List<Integer> deliveryPoints = new ArrayList<>();
        for (int i = 0; i < geoPoints.size() - 1; i++) {
            deliveryPoints.add(i);
        }

        Collections.shuffle(deliveryPoints); // balance assignment
        //assing vehicles to delivery points
        List<List<Integer>> assigned = new ArrayList<>();
        for (int i = 0; i < vehicleCount; i++) assigned.add(new ArrayList<>());

        for (int i = 0; i < deliveryPoints.size(); i++) {
            assigned.get(i % vehicleCount).add(deliveryPoints.get(i));
        }
        //  create routes for each vehicle
        for (List<Integer> route : assigned) {
            List<Integer> path = new ArrayList<>();
            Set<Integer> unvisited = new HashSet<>(route);
            int current = depotIndex;
            path.add(current);

            while (!unvisited.isEmpty()) {
                int next = -1;
                double bestDist = Double.MAX_VALUE;
                for (int u : unvisited) {
                    double d = haversine(geoPoints.get(current), geoPoints.get(u));
                    if (d < bestDist) {
                        bestDist = d;
                        next = u;
                    }
                }
                path.add(next);
                unvisited.remove(next);
                current = next;
            }

            path.add(depotIndex); // return to depot
            vehicleRoutes.add(path);
        }
    }

    //translate to real tisdanca
    private double haversine(GeoPoint p1, GeoPoint p2) {
        double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(p2.lat - p1.lat);
        double dLon = Math.toRadians(p2.lon - p1.lon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(p1.lat)) * Math.cos(Math.toRadians(p2.lat)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Override
    // draw points and routes
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawPoints(g);
        drawRoutes(g);
    }
    //point
    private void drawPoints(Graphics g) {
        for (Point p : points) {
            if (p.label.equals("Depot")) {
                g.setColor(Color.RED);
                g.fillOval(p.x - 6, p.y - 6, 12, 12);
            } else {
                g.setColor(Color.BLACK);
                g.fillOval(p.x - 4, p.y - 4, 8, 8);
            }
            g.drawString(p.label, p.x + 6, p.y);
        }
    }
    //route
    private void drawRoutes(Graphics g) {
        Color[] colors = {Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE};
        int i = 0;
        for (List<Integer> route : vehicleRoutes) {
            g.setColor(colors[i % colors.length]);
            for (int j = 0; j < route.size() - 1; j++) {
                Point p1 = points.get(route.get(j));
                Point p2 = points.get(route.get(j + 1));
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            i++;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("VRP - Auto Routing Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.add(new VRPSolverVisualizer());
        frame.setVisible(true);
    }
}
