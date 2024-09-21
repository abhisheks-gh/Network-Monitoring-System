package com.jiobp.monitoring;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for checking the network status of both primary and secondary networks for a given RO.
 * It also logs if any network is down for more than 5 minutes.
 *
 * @author abhisheks-gh
 */
public class NetworkStatusChecker {
    private Map<String, Integer> downCounters;

    public NetworkStatusChecker() {
        downCounters = new HashMap<>();
    }

    /**
     * Checks if the given network IP is reachable (i.e., the network is up) using ping.
     *
     * @param ipAddress the IP address of the network to check.
     * @return true if the network is reachable, false otherwise.
     */
    public boolean isNetworkUp(String ipAddress, int timeout) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            boolean reachable = address.isReachable(timeout); // Ping-like check
            System.out.println("Network " + ipAddress + " is " + (reachable ? "UP" : "DOWN")); // Console feedback
            return reachable;
        } catch (IOException e) {
            System.out.println("Network " + ipAddress + " is DOWN.");  // Console feedback
            return false;
        }
    }

    /**
     * Checks the status of both primary and secondary networks for a given RO.
     * It updates the down counter and handles logging if either network is down for 5 consecutive minutes.
     *
     * @param roData a map containing RO data including primary/secondary IPs, RO code, and other metadata.
     * @throws IOException if an error occurs while checking network status.
     */
    public void checkNetworks(Map<String, String> roData, double bandwidth) throws IOException {
        String primaryIP = roData.get("primaryIP");
        String secondaryIP = roData.get("secondaryIP");
        String roCode = roData.get("roCode");

        boolean primaryStatus = isNetworkUp(primaryIP, 3000);
        boolean secondaryStatus = secondaryIP != null && isNetworkUp(secondaryIP, 3000);

        updateDownCounters(roCode, primaryStatus, secondaryStatus);
        handleDownNetworks(roData, primaryStatus, secondaryStatus, bandwidth);
    }

    /**
     * Updates the down counters for a given RO based on the current network status.
     * If either network is down, the counter increases. Otherwise, it resets the counter.
     *
     * @param roCode the code of the RO.
     * @param primaryStatus the status of the primary network (up/down).
     * @param secondaryStatus the status of the secondary network (up/down).
     */
    private void updateDownCounters(String roCode, boolean primaryStatus, boolean secondaryStatus) {
        int downTime = downCounters.getOrDefault(roCode, 0);

        if (!primaryStatus || !secondaryStatus) {
            downCounters.put(roCode, downTime + 1);
        } else {
            downCounters.put(roCode, 0);
        }
    }

    /**
     * Handles logging if either the primary or secondary network is down for 5 consecutive minutes.
     * If both networks are down, the RO status is logged as "Offline"; otherwise, it's logged as "Partial".
     *
     * @param roData a map containing RO data including primary/secondary IPs, RO code, and other metadata.
     * @param primaryStatus the status of the primary network (up/down).
     * @param secondaryStatus the status of the secondary network (up/down).
     * @param bandwidth the current bandwidth speed.
     * @throws IOException if an error occurs during logging.
     */
    private void handleDownNetworks(Map<String, String> roData, boolean primaryStatus, boolean secondaryStatus,
                                    double bandwidth) throws IOException {
        String roCode = roData.get("roCode");
        if (downCounters.get(roCode) >= 5) {
            String roStatus = (!primaryStatus && !secondaryStatus) ? "Offline" : "Partial";
            String networkType = !primaryStatus ? "Primary" : "Secondary";
            logDownNetwork(roData, primaryStatus, secondaryStatus, roStatus, networkType, bandwidth);
        }
    }

    /**
     * Logs the network down status to a CSV file.
     * It records the RO information, network type (primary/secondary), timestamp, and RO status.
     *
     * @param roData a map containing RO data including primary/secondary IPs, RO code, and other metadata.
     * @param primaryStatus the status of the primary network (up/down).
     * @param secondaryStatus the status of the secondary network (up/down).
     * @param roStatus the status of the RO ("Offline" or "Partial").
     * @param networkType the status of the RO ("Offline" or "Partial").
     * @param bandwidth the current bandwidth speed.
     * @throws IOException if an error occurs while writing to the log file.
     */
    private void logDownNetwork(Map<String, String> roData, boolean primaryStatus, boolean secondaryStatus,
                                String roStatus, String networkType, double bandwidth) throws IOException {
        LogWriter logger = new LogWriter();
        logger.writeLog(roData, primaryStatus, secondaryStatus, roStatus, networkType, bandwidth);
    }

}
