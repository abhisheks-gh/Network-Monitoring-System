package com.jiobp.monitoring;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * This class handles the logging of network statuses into a CSV file when either of the network go down.
 *
 * @author abhisheks-gh
 */
public class LogWriter {
    private static final String LOG_FILE = "logs/network_status_log.csv";

    public LogWriter() {
        initializeLogFile();
    }

    /**
     * Initializes the log file by adding a header to the CSV file if it doesn't exist.
     */
    private void initializeLogFile() {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            if (writer.getEncoding().isEmpty()) {
                writer.append("RO code, IP, Network Type, Timestamp, RO Status, City, State, Region, Bandwidth (Mbps)\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a log entry into the CSV file, detailing the network down status.
     *
     * @param roData a map containing RO data including primary/secondary IPs, RO code, and other metadata.
     * @param primaryStatus the status of the primary network (up/down).
     * @param secondaryStatus the status of the secondary network (up/down).
     * @param roStatus the status of the RO ("Offline" or "Partial").
     * @param networkType the type of the network that is down ("Primary" or "Secondary").
     * @throws IOException if an error occurs while writing to the log file.
     */
    public void writeLog(Map<String, String> roData, boolean primaryStatus, boolean secondaryStatus,
                         String roStatus, String networkType, double bandwidth) throws IOException {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            String logEntry = String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                    roData.get("roCode"),
                    networkType.equals("Primary") ? roData.get("primaryIP") : roData.get("secondaryIP"),
                    networkType,
                    LocalDateTime.now(),
                    roData.get("city"),
                    roData.get("state"),
                    roData.get("region")
            );

            writer.append(logEntry);
        }
    }
}
