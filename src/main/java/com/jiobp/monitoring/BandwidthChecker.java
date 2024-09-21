package com.jiobp.monitoring;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class checks the bandwidth speed of a network by downloading a test file calculating download speed.
 *
 * @author abhisheks-gh
 */
public class BandwidthChecker {

    /**
     * Measures download speed by downloading a file and calculating the time taken.
     *
     * @param fileUrl the URL of the file to download.
     * @return estimated download bandwidth in Mbps.
     * @throws Exception if any error occurs.
     */
    public double measureDownloadSpeed(String fileUrl) throws Exception {
        URL url = new URL(fileUrl);
        URLConnection connection = url.openConnection();
        InputStream inputStream = connection.getInputStream();

        long fileSize = connection.getContentLengthLong();
        long startTime = System.nanoTime();

        byte[] buffer = new byte[1024];
        int bytesRead;
        long totalBytesRead = 0;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            totalBytesRead += bytesRead;
        }

        long endTime = System.nanoTime();

        // Calculate the time taken in seconds
        double timeTakenSeconds = (endTime - startTime) / 1_000_000_000.0;

        // Convert byte to megabits
        double fileSizeMegabits = (totalBytesRead * 8) / (1024.0 * 1024.0);

        // Calculate bandwidth in Mbps (Megabits per second)
        double bandwidthMbps = fileSizeMegabits / timeTakenSeconds;

        return bandwidthMbps;
    }

}
