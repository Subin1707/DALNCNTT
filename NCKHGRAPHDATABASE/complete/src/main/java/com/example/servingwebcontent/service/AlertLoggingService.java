package com.example.servingwebcontent.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AlertLoggingService {

    private static final String LOG_DIR = "logs";
    private static final String ALERT_LOG_FILE = "logs/alerts.log";
    private static final String DETECTION_LOG_FILE = "logs/detections.log";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final List<AlertLog> alertMemory = new CopyOnWriteArrayList<>();
    private final List<DetectionLog> detectionMemory = new CopyOnWriteArrayList<>();

    public AlertLoggingService() {
        initializeLogDirectory();
    }

    private void initializeLogDirectory() {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    /**
     * Log a detection alert
     */
    public void logAlert(String nodeId, String nodeType, String nodeValue, String riskLevel, int riskScore, String decision) {
        LocalDateTime timestamp = LocalDateTime.now();
        AlertLog alert = new AlertLog(
                nodeId,
                nodeType,
                nodeValue,
                riskLevel,
                riskScore,
                decision,
                timestamp
        );

        // Save to memory
        alertMemory.add(alert);

        // Save to file
        saveAlertToFile(alert);

        // Console output
        System.out.println(formatAlertMessage(alert));
    }

    /**
     * Log detection with detailed analysis
     */
    public void logDetection(String nodeId, String nodeType, String nodeValue, String riskLevel, 
                            String analysis, String threats, List<String> relatedNodes) {
        LocalDateTime timestamp = LocalDateTime.now();
        DetectionLog detection = new DetectionLog(
                nodeId,
                nodeType,
                nodeValue,
                riskLevel,
                analysis,
                threats,
                relatedNodes,
                timestamp
        );

        // Save to memory
        detectionMemory.add(detection);

        // Save to file
        saveDetectionToFile(detection);

        // Console output
        System.out.println(formatDetectionMessage(detection));
    }

    /**
     * Log a blocking action
     */
    public void logBlockAction(String nodeId, String nodeType, String nodeValue, String reason, String decision) {
        LocalDateTime timestamp = LocalDateTime.now();
        String message = String.format(
                "[%s] 🚨 BLOCK ACTION | Node: %s (%s) | Value: %s | Reason: %s | Decision: %s",
                DATE_FORMAT.format(timestamp),
                nodeId,
                nodeType,
                nodeValue,
                reason,
                decision
        );

        writeToFile(ALERT_LOG_FILE, message);
        System.out.println(message);
    }

    /**
     * Get all alerts
     */
    public List<AlertLog> getAllAlerts() {
        return new ArrayList<>(alertMemory);
    }

    /**
     * Get all detections
     */
    public List<DetectionLog> getAllDetections() {
        return new ArrayList<>(detectionMemory);
    }

    /**
     * Get recent alerts (last N)
     */
    public List<AlertLog> getRecentAlerts(int limit) {
        return alertMemory.stream()
                .skip(Math.max(0, alertMemory.size() - limit))
                .toList();
    }

    /**
     * Get recent detections (last N)
     */
    public List<DetectionLog> getRecentDetections(int limit) {
        return detectionMemory.stream()
                .skip(Math.max(0, detectionMemory.size() - limit))
                .toList();
    }

    /**
     * Get alerts by risk level
     */
    public List<AlertLog> getAlertsByRiskLevel(String riskLevel) {
        return alertMemory.stream()
                .filter(a -> a.riskLevel.equalsIgnoreCase(riskLevel))
                .toList();
    }

    /**
     * Get alerts by decision
     */
    public List<AlertLog> getAlertsByDecision(String decision) {
        return alertMemory.stream()
                .filter(a -> a.decision.equalsIgnoreCase(decision))
                .toList();
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAlerts", alertMemory.size());
        stats.put("totalDetections", detectionMemory.size());
        stats.put("blockActions", alertMemory.stream().filter(a -> "BLOCK".equals(a.decision)).count());
        stats.put("monitorActions", alertMemory.stream().filter(a -> "MONITOR".equals(a.decision)).count());
        stats.put("allowActions", alertMemory.stream().filter(a -> "ALLOW".equals(a.decision)).count());
        stats.put("highRiskAlerts", alertMemory.stream().filter(a -> "HIGH".equalsIgnoreCase(a.riskLevel)).count());
        return stats;
    }

    // ============ PRIVATE HELPER METHODS ============

    private void saveAlertToFile(AlertLog alert) {
        String message = formatAlertMessage(alert);
        writeToFile(ALERT_LOG_FILE, message);
    }

    private void saveDetectionToFile(DetectionLog detection) {
        String message = formatDetectionMessage(detection);
        writeToFile(DETECTION_LOG_FILE, message);
    }

    private String formatAlertMessage(AlertLog alert) {
        return String.format(
                "[%s] 🔔 ALERT | Node: %s (%s) | Value: %s | Risk: %s (Score: %d) | Decision: %s",
                DATE_FORMAT.format(alert.timestamp),
                alert.nodeId,
                alert.nodeType,
                alert.nodeValue,
                alert.riskLevel,
                alert.riskScore,
                alert.decision
        );
    }

    private String formatDetectionMessage(DetectionLog detection) {
        String relatedNodesStr = detection.relatedNodes != null ? String.join(", ", detection.relatedNodes) : "None";
        return String.format(
                "[%s] 🎯 DETECTION | Node: %s (%s) | Value: %s | Risk: %s | Analysis: %s | Threats: %s | Related: [%s]",
                DATE_FORMAT.format(detection.timestamp),
                detection.nodeId,
                detection.nodeType,
                detection.nodeValue,
                detection.riskLevel,
                detection.analysis,
                detection.threats,
                relatedNodesStr
        );
    }

    private synchronized void writeToFile(String filePath, String message) {
        try (FileWriter fw = new FileWriter(filePath, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(message);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    // ============ INNER CLASSES ============

    public static class AlertLog {
        public String nodeId;
        public String nodeType;
        public String nodeValue;
        public String riskLevel;
        public int riskScore;
        public String decision;
        public LocalDateTime timestamp;

        public AlertLog(String nodeId, String nodeType, String nodeValue, String riskLevel, 
                       int riskScore, String decision, LocalDateTime timestamp) {
            this.nodeId = nodeId;
            this.nodeType = nodeType;
            this.nodeValue = nodeValue;
            this.riskLevel = riskLevel;
            this.riskScore = riskScore;
            this.decision = decision;
            this.timestamp = timestamp;
        }
    }

    public static class DetectionLog {
        public String nodeId;
        public String nodeType;
        public String nodeValue;
        public String riskLevel;
        public String analysis;
        public String threats;
        public List<String> relatedNodes;
        public LocalDateTime timestamp;

        public DetectionLog(String nodeId, String nodeType, String nodeValue, String riskLevel,
                           String analysis, String threats, List<String> relatedNodes, LocalDateTime timestamp) {
            this.nodeId = nodeId;
            this.nodeType = nodeType;
            this.nodeValue = nodeValue;
            this.riskLevel = riskLevel;
            this.analysis = analysis;
            this.threats = threats;
            this.relatedNodes = relatedNodes;
            this.timestamp = timestamp;
        }
    }
}
