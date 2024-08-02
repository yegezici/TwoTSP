import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TwoTSPNearestNeighbor {

    public static void main(String[] args) {
        String inputFile = "test-input-4.txt"; // Path to the input file containing city coordinates
        String outputFile = "test-output-4.txt"; // Path to the output file to write results

        try {
            // Read input file and get the list of city coordinates
            List<int[]> cities = readInput(inputFile);
            
            // Define starting cities for both salesmen
            int startCity1 = 0; // Starting city index for salesman 1
            int startCity2 = 1; // Starting city index for salesman 2

            // Generate initial paths for both salesmen using the nearest neighbor algorithm
            int[][] paths = nearestNeighborForTwo(cities, startCity1, startCity2);
            int[] path1 = paths[0]; // Path for salesman 1
            int[] path2 = paths[1]; // Path for salesman 2

            // Optimize the generated paths using the 2-opt algorithm
            path1 = optimizeWith2Opt(path1, cities);
            path2 = optimizeWith2Opt(path2, cities);

            // Calculate the total cost for each path
            int cost1 = calculateCost(path1, cities);
            int cost2 = calculateCost(path2, cities);
            int totalCost = cost1 + cost2; // Sum of costs of both paths

            // Write the results to the output file
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(totalCost + "\n"); // Write total cost
                writer.write(cost1 + " " + (path1.length - 1) + "\n"); // Write cost and number of cities for path1
                for (int i = 0; i < path1.length - 1; i++) {
                    writer.write(path1[i] + "\n"); // Write each city in path1
                }
                writer.write("\n");
                writer.write(cost2 + " " + (path2.length - 1) + "\n"); // Write cost and number of cities for path2
                for (int i = 0; i < path2.length - 1; i++) {
                    writer.write(path2[i] + "\n"); // Write each city in path2
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Print stack trace in case of an IOException
        }
    }

    // Reads the input file and returns a list of cities with their coordinates
    public static List<int[]> readInput(String inputFile) throws IOException {
        List<int[]> cities = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\s+"); // Split the line into parts
            int x = Integer.parseInt(parts[1]); // Parse x-coordinate
            int y = Integer.parseInt(parts[2]); // Parse y-coordinate
            cities.add(new int[]{x, y}); // Add the city coordinates to the list
        }
        reader.close(); // Close the BufferedReader

        return cities; // Return the list of cities
    }

    // Generates two initial paths using the nearest neighbor algorithm
    public static int[][] nearestNeighborForTwo(List<int[]> cities, int start1, int start2) {
        int numCities = cities.size();
        boolean[] visited = new boolean[numCities]; // Array to keep track of visited cities
        int[] path1 = new int[numCities + 1]; // Path for salesman 1
        int[] path2 = new int[numCities + 1]; // Path for salesman 2
        int pathIndex1 = 0, pathIndex2 = 0;

        int currentCity1 = start1;
        int currentCity2 = start2;

        visited[start1] = true; // Mark starting city of salesman 1 as visited
        visited[start2] = true; // Mark starting city of salesman 2 as visited

        path1[pathIndex1++] = start1; // Add starting city to path of salesman 1
        path2[pathIndex2++] = start2; // Add starting city to path of salesman 2

        // Continue until all cities are visited
        while (pathIndex1 + pathIndex2 < numCities) {
            int nearestNeighbor1 = -1; // Nearest neighbor for salesman 1
            int nearestNeighbor2 = -1; // Nearest neighbor for salesman 2
            int nearestDistance1 = Integer.MAX_VALUE; // Distance to nearest neighbor for salesman 1
            int nearestDistance2 = Integer.MAX_VALUE; // Distance to nearest neighbor for salesman 2

            // Find the nearest unvisited neighbor for both salesmen
            for (int city = 0; city < numCities; city++) {
                if (!visited[city]) {
                    int distance1 = calculateDistance(cities.get(currentCity1), cities.get(city)); // Distance from current city of salesman 1
                    int distance2 = calculateDistance(cities.get(currentCity2), cities.get(city)); // Distance from current city of salesman 2

                    if (distance1 < nearestDistance1) {
                        nearestNeighbor1 = city; // Update nearest neighbor for salesman 1
                        nearestDistance1 = distance1; // Update nearest distance for salesman 1
                    }

                    if (distance2 < nearestDistance2) {
                        nearestNeighbor2 = city; // Update nearest neighbor for salesman 2
                        nearestDistance2 = distance2; // Update nearest distance for salesman 2
                    }
                }
            }

            // Choose the closest neighbor and update paths and visited cities accordingly
            if (nearestNeighbor1 != -1 && (nearestNeighbor2 == -1 || nearestDistance1 <= nearestDistance2)) {
                path1[pathIndex1++] = nearestNeighbor1; // Add nearest neighbor to path of salesman 1
                currentCity1 = nearestNeighbor1; // Update current city of salesman 1
                visited[nearestNeighbor1] = true; // Mark nearest neighbor as visited
            }

            if (nearestNeighbor2 != -1 && (nearestNeighbor1 == -1 || nearestDistance2 < nearestDistance1)) {
                path2[pathIndex2++] = nearestNeighbor2; // Add nearest neighbor to path of salesman 2
                currentCity2 = nearestNeighbor2; // Update current city of salesman 2
                visited[nearestNeighbor2] = true; // Mark nearest neighbor as visited
            }
        }

        path1[pathIndex1++] = start1; // Return to starting city for salesman 1 to complete the tour
        path2[pathIndex2++] = start2; // Return to starting city for salesman 2 to complete the tour

        return new int[][]{Arrays.copyOf(path1, pathIndex1), Arrays.copyOf(path2, pathIndex2)}; // Return the paths
    }

    // Optimizes the given path using the 2-opt algorithm
    public static int[] optimizeWith2Opt(int[] path, List<int[]> cities) {
        int n = path.length;
        boolean improved = true;

        // Repeat until no improvement is found
        while (improved) {
            improved = false;

            // Iterate over all possible pairs of edges
            for (int i = 1; i < n - 2; i++) {
                for (int j = i + 1; j < n - 1; j++) {
                    int delta = calculate2OptGain(path, i, j, cities);
                    if (delta < 0) {
                        path = apply2OptSwap(path, i, j); // Apply the 2-opt swap
                        improved = true; // Set improved to true to continue optimization
                    }
                }
            }
        }

        return path; // Return the optimized path
    }

    // Calculates the gain from a potential 2-opt swap
    public static int calculate2OptGain(int[] path, int i, int j, List<int[]> cities) {
        int n = path.length;
        int[] a = cities.get(path[i - 1]);
        int[] b = cities.get(path[i]);
        int[] c = cities.get(path[j]);
        int[] d = cities.get(path[(j + 1) % n]);

        int currentCost = calculateDistance(a, b) + calculateDistance(c, d);
        int newCost = calculateDistance(a, c) + calculateDistance(b, d);

        return newCost - currentCost; // Return the cost difference (gain) from the 2-opt swap
    }

    // Applies the 2-opt swap to the path
    public static int[] apply2OptSwap(int[] path, int i, int j) {
        int[] newPath = new int[path.length];
        System.arraycopy(path, 0, newPath, 0, i); // Copy the unchanged part of the path

        int dec = 0;
        // Reverse the segment of the path between i and j
        for (int k = j; k >= i; k--) {
            newPath[i + dec] = path[k];
            dec++;
        }
        System.arraycopy(path, j + 1, newPath, j + 1, path.length - j - 1); // Copy the remaining part of the path
        return newPath; // Return the new path after the 2-opt swap
    }

    // Calculates the total cost of the given path
    public static int calculateCost(int[] path, List<int[]> cities) {
        int cost = 0;
        // Sum up the distances between consecutive cities in the path
        for (int i = 0; i < path.length - 1; i++) {
            cost += calculateDistance(cities.get(path[i]), cities.get(path[i + 1]));
        }
        return cost; // Return the total cost of the path
    }

    // Calculates the Euclidean distance between two cities
    public static int calculateDistance(int[] city1, int[] city2) {
        int x1 = city1[0];
        int y1 = city1[1];
        int x2 = city2[0];
        int y2 = city2[1];
        return (int) Math.round(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2))); // Return the Euclidean distance rounded to the nearest integer
    }
}