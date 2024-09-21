package com.jiobp.monitoring;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Main class serves as the entry point to the program.
 * It reads RO data from a CSV file, checks the status of primary and secondary networks for each RO every minute,
 * and logs any issues that persist for 5 consecutive minutes. It also measures bandwidth speed periodically.
 *
 * @author abhisheks-gh
 */
public class Main {

    private static final String RO_DATA_FILE = "src/main/resources/ro_data.csv";
    private static double bandwidth = 0.0; // Store the current bandwidth

    public static void main(String[] args) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        NetworkStatusChecker networkStatusChecker = new NetworkStatusChecker();
        BandwidthChecker bandwidthChecker = new BandwidthChecker();

        // Schedule the network status check every 1 minute
        executorService.scheduleAtFixedRate(() -> {
            try {
                Map<String, Map<String, String>> roData = readROData();

                for (Map.Entry<String, Map<String, String>> entry : roData.entrySet()) {
                    String roCode = entry.getKey();
                    Map<String, String> details = entry.getValue();

                    String primaryIP = details.get("primaryIP");
                    String secondaryIP = details.get("secondaryIP");

                    // Check Primary network
                    boolean primaryStatus = networkStatusChecker.isNetworkUp(primaryIP, 10000);  // 10 seconds timeout

                    // Check Secondary network, if exists
                    boolean secondaryStatus = secondaryIP != null && !secondaryIP.isEmpty() && networkStatusChecker.isNetworkUp(secondaryIP, 10000);

                    // Output to console for testing
                    System.out.println("RO: " + roCode + " | Primary: " + primaryIP + " | Status: " + (primaryStatus ? "UP" : "DOWN"));
                    if (secondaryIP != null && !secondaryIP.isEmpty()) {
                        System.out.println("RO: " + roCode + " | Secondary: " + secondaryIP + " | Status: " + (secondaryStatus ? "UP" : "DOWN"));
                    }

                    // Add logic to handle the 5-minute downtime check and logging...
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES); // Check every 1 minute
    }

    /**
     * Reads the RO (Retail Outlet) data from a CSV file and returns it as a map.
     *
     * @return a map where each key is an RO code and the value is another map containing the RO details.
     * @throws IOException if there is an issue reading the CSV file.
     */
    private static Map<String, Map<String, String>> readROData() throws IOException {
        Map<String, Map<String, String>> roDataMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(RO_DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                Map<String, String> roData = new HashMap<>();
                roData.put("roCode", fields[0]);
                roData.put("primaryIP", fields[1]);
                roData.put("secondaryIP", fields.length > 2 ? fields[2] : null);
                roData.put("city", fields[3]);
                roData.put("state", fields[4]);
                roData.put("region", fields[5]);
                roDataMap.put(fields[0], roData);
            }
        }

        return roDataMap;
    }

}
