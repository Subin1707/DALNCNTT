package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.ChatbotResponseDTO;
import com.example.servingwebcontent.dto.ChatbotResponseDTO.RelatedNodeDTO;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class EnhancedChatbotService {

    private final Neo4jClient neo4j;
    private final AlertLoggingService alertLoggingService;

    public EnhancedChatbotService(Neo4jClient neo4j, AlertLoggingService alertLoggingService) {
        this.neo4j = neo4j;
        this.alertLoggingService = alertLoggingService;
    }

    private enum ThreatPattern {
        PHISHING("Phishing", "Mimics legitimate services to steal credentials"),
        C2_COMMAND("C2 Communication", "Command-and-control communication with an attacker"),
        LATERAL_MOVEMENT("Lateral Movement", "Attempts to move within the internal network"),
        BOTNET("Botnet Activity", "Part of botnet infrastructure"),
        MALWARE_HOSTING("Malware Hosting", "Hosts or distributes malware"),
        DDoS_SOURCE("DDoS Source", "Used as a source for DDoS attacks"),
        DATA_EXFILTRATION("Data Exfiltration", "Attempts to steal sensitive data"),
        NONE("Clean", "No suspicious patterns detected");

        private final String displayName;
        private final String description;

        ThreatPattern(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    public ChatbotResponseDTO generateAnalysis(
            String nodeId,
            String nodeType,
            String nodeValue,
            String riskLevel,
            int riskScore,
            List<String> indicators
    ) {
        ChatbotResponseDTO response = new ChatbotResponseDTO(nodeId, nodeType, nodeValue);
        response.setRiskLevel(riskLevel);
        response.setRiskScore(riskScore);

        ThreatPattern pattern = detectThreatPattern(nodeType, nodeValue, riskScore);
        List<RelatedNodeDTO> relatedNodes = findRelatedNodes(nodeId, nodeType);

        response.setStatus(getNodeStatus(riskScore));
        response.setAnalysisDescription(generateAnalysisLayer(nodeType, nodeValue, riskScore, pattern));
        response.setRiskAssessment(generateRiskAssessment(riskLevel, riskScore, indicators, pattern));
        response.setSpecificDangers(generateThreatExplanation(nodeType, riskLevel, pattern));
        response.setThreatExplanation(generateThreatNarrative(nodeType, riskLevel, pattern));
        response.setRecommendedActions(generateRecommendedActions(riskLevel, nodeType, pattern));
        response.setRelatedNodes(relatedNodes);
        response.setGraphIntelligence(generateGraphIntelligence(relatedNodes, riskScore));

        return response;
    }

    private ThreatPattern detectThreatPattern(String nodeType, String nodeValue, int riskScore) {
        if (riskScore < 20) {
            return ThreatPattern.NONE;
        }

        String safeType = safeUpper(nodeType);
        String lowerValue = nodeValue == null ? "" : nodeValue.toLowerCase();

        switch (safeType) {
            case "URL":
                if (containsAny(lowerValue, "login", "secure", "verify", "confirm", "account", "password")) {
                    return ThreatPattern.PHISHING;
                }
                if (containsAny(lowerValue, "payload", "malware", "exploit", "dropper")) {
                    return ThreatPattern.MALWARE_HOSTING;
                }
                break;
            case "IP":
                if (isInternalIP(nodeValue) && riskScore >= 50) {
                    return ThreatPattern.LATERAL_MOVEMENT;
                }
                if (riskScore >= 70) {
                    return ThreatPattern.C2_COMMAND;
                }
                if (riskScore >= 60) {
                    return ThreatPattern.DDoS_SOURCE;
                }
                break;
            case "DOMAIN":
                if (containsAny(lowerValue, "c2", "command", "control")) {
                    return ThreatPattern.C2_COMMAND;
                }
                if (riskScore >= 70) {
                    return ThreatPattern.MALWARE_HOSTING;
                }
                break;
            case "EMAIL":
                if (riskScore >= 70 || containsAny(lowerValue, "support", "security", "billing", "verify")) {
                    return ThreatPattern.PHISHING;
                }
                break;
            case "FILE":
                if (riskScore >= 70) {
                    return ThreatPattern.MALWARE_HOSTING;
                }
                break;
            default:
                break;
        }

        if (riskScore >= 70) {
            return ThreatPattern.MALWARE_HOSTING;
        }
        if (riskScore >= 50) {
            return ThreatPattern.BOTNET;
        }
        return ThreatPattern.NONE;
    }

    private String generateAnalysisLayer(String nodeType, String nodeValue, int riskScore, ThreatPattern pattern) {
        String normalizedType = safeUpper(nodeType);
        String summary = switch (normalizedType) {
            case "IP" -> "IP address observed in the fraud graph. Classification: "
                    + (isInternalIP(nodeValue) ? "internal/private" : "external/public") + ".";
            case "DOMAIN" -> "Domain observed in the fraud graph and linked to other entities.";
            case "URL" -> "URL observed in messages, browsing activity, or network telemetry.";
            case "EMAIL" -> "Email address observed in the fraud graph and linked to communications.";
            case "FILE" -> "File artifact observed in the fraud graph and linked to system activity.";
            default -> "Entity observed in the fraud graph.";
        };

        return "Type: " + nodeType + "\n"
                + "Value: " + nodeValue + "\n"
                + "Summary: " + summary + "\n"
                + "Risk Score: " + riskScore + "/100\n"
                + "Detected Pattern: " + pattern.getDisplayName() + "\n"
                + "Why: " + pattern.getDescription();
    }

    private String generateRiskAssessment(String riskLevel, int riskScore, List<String> indicators, ThreatPattern pattern) {
        StringBuilder builder = new StringBuilder();
        builder.append("Risk Level: ").append(riskLevel).append('\n');
        builder.append("Risk Score: ").append(riskScore).append("/100\n");
        builder.append("Threat Pattern: ").append(pattern.getDisplayName()).append('\n');

        if (indicators != null && !indicators.isEmpty()) {
            builder.append("Indicators:\n");
            indicators.stream().limit(4).forEach(indicator -> builder.append("- ").append(indicator).append('\n'));
        }

        builder.append("Assessment: ");
        if (riskScore >= 70) {
            builder.append("High-confidence malicious behavior. Immediate containment is appropriate.");
        } else if (riskScore >= 40) {
            builder.append("Suspicious behavior. Investigate context and related nodes before taking broad action.");
        } else if (riskScore >= 20) {
            builder.append("Low to moderate concern. Monitor for escalation or additional corroborating signals.");
        } else {
            builder.append("Low concern based on current evidence.");
        }

        return builder.toString();
    }

    private List<String> generateThreatExplanation(String nodeType, String riskLevel, ThreatPattern pattern) {
        List<String> dangers = new ArrayList<>();

        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            dangers.add("This entity is strongly associated with malicious activity.");
        } else if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            dangers.add("This entity has suspicious characteristics that require review.");
        } else {
            dangers.add("Current evidence does not show strong malicious behavior.");
        }

        switch (pattern) {
            case PHISHING -> {
                dangers.add("May steal credentials or payment information.");
                dangers.add("Often impersonates a trusted service or workflow.");
            }
            case C2_COMMAND -> {
                dangers.add("May coordinate compromised hosts or malware.");
                dangers.add("Can support persistence, tasking, and data theft.");
            }
            case LATERAL_MOVEMENT -> {
                dangers.add("May indicate an internal host is probing or spreading.");
                dangers.add("Can expose additional systems through trusted network paths.");
            }
            case MALWARE_HOSTING -> {
                dangers.add("May deliver or host malicious payloads.");
                dangers.add("Systems interacting with it may be at risk of compromise.");
            }
            case DDoS_SOURCE -> {
                dangers.add("May be used to generate disruptive traffic.");
                dangers.add("Can affect service availability or hide parallel activity.");
            }
            case BOTNET -> {
                dangers.add("May be part of coordinated malicious infrastructure.");
                dangers.add("Behavior may escalate quickly if attacker tasking changes.");
            }
            default -> dangers.add("Review linked entities for additional context.");
        }

        return dangers;
    }

    private String generateThreatNarrative(String nodeType, String riskLevel, ThreatPattern pattern) {
        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            return "This " + nodeType + " is high risk and aligns with " + pattern.getDisplayName()
                    + " behavior. Treat it as potentially malicious until proven otherwise.";
        }
        if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            return "This " + nodeType + " is suspicious and should be investigated with its graph context."
                    + " The observed pattern is " + pattern.getDisplayName() + ".";
        }
        return "This " + nodeType + " is currently low risk. Continue monitoring and review graph relationships"
                + " if new indicators appear.";
    }

    private List<String> generateRecommendedActions(String riskLevel, String nodeType, ThreatPattern pattern) {
        List<String> actions = new ArrayList<>();

        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            actions.add("Block or isolate this " + nodeType.toLowerCase() + " where operationally possible.");
            actions.add("Review all related nodes and recent activity for the same pattern.");
            actions.add("Notify the security team and open an incident for containment.");
        } else if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            actions.add("Enable enhanced monitoring for this " + nodeType.toLowerCase() + ".");
            actions.add("Review related nodes, event logs, and enrichment sources.");
            actions.add("Prepare containment steps if risk score or indicators increase.");
        } else {
            actions.add("Continue routine monitoring.");
            actions.add("Alert on meaningful reputation or behavior changes.");
        }

        switch (pattern) {
            case PHISHING -> actions.add("Check for impacted users, inboxes, and credential exposure.");
            case C2_COMMAND -> actions.add("Review outbound connections and inspect potentially compromised hosts.");
            case LATERAL_MOVEMENT -> actions.add("Inspect east-west traffic and validate internal account usage.");
            case MALWARE_HOSTING -> actions.add("Scan systems that accessed the related resource.");
            case DDoS_SOURCE -> actions.add("Review traffic volume, target patterns, and upstream controls.");
            case BOTNET -> actions.add("Look for recurring beaconing or coordinated connections.");
            default -> { }
        }

        return actions;
    }

    @SuppressWarnings("unchecked")
    private List<RelatedNodeDTO> findRelatedNodes(String nodeId, String nodeType) {
        List<RelatedNodeDTO> relatedNodes = new ArrayList<>();

        try {
            String label = safeUpper(nodeType);
            String query = switch (label) {
                case "IP", "DOMAIN", "URL", "EMAIL", "FILE" -> """
                        MATCH (n:%s {id: $nodeId})-[r]-(related)
                        RETURN related.id AS id,
                               labels(related)[0] AS type,
                               related.value AS value,
                               related.riskLevel AS risk,
                               type(r) AS rel
                        LIMIT 5
                        """.formatted(label);
                default -> """
                        MATCH (n)-[r]-(related)
                        WHERE n.id = $nodeId
                        RETURN related.id AS id,
                               labels(related)[0] AS type,
                               related.value AS value,
                               related.riskLevel AS risk,
                               type(r) AS rel
                        LIMIT 5
                        """;
            };

            Collection<Map<String, Object>> results = (Collection<Map<String, Object>>) (Collection<?>) neo4j.query(query)
                    .bind(nodeId).to("nodeId")
                    .fetchAs(Map.class)
                    .all();

            for (Map<String, Object> row : results) {
                String relatedId = (String) row.get("id");
                String relatedType = (String) row.get("type");
                String relatedValue = (String) row.get("value");
                String riskLevel = (String) row.get("risk");
                String relationship = (String) row.get("rel");

                relatedNodes.add(new RelatedNodeDTO(
                        relatedId,
                        relatedType,
                        relatedValue,
                        riskLevel != null ? riskLevel : "UNKNOWN",
                        relationship,
                        generateRelationshipReason(relatedType, relationship)
                ));
            }
        } catch (Exception ignored) {
            // Keep chatbot responses available even when graph enrichment fails.
        }

        return relatedNodes;
    }

    private String generateRelationshipReason(String relatedType, String relationship) {
        if (relationship == null || relationship.isBlank()) {
            return "Connected in the graph.";
        }

        return switch (relationship.toUpperCase()) {
            case "SENT_FROM_IP" -> "Linked through source IP activity.";
            case "HOSTED_ON" -> "Linked through hosting infrastructure.";
            case "CONTAINS_URL" -> "Linked because one entity references the URL.";
            case "BELONGS_TO_DOMAIN" -> "Linked through domain ownership or naming.";
            case "HAS_EMAIL" -> "Linked through email association.";
            case "HAS_IP" -> "Linked through IP association.";
            case "HAS_URL" -> "Linked through URL association.";
            default -> "Connected via relationship: " + relationship;
        };
    }

    private String generateGraphIntelligence(List<RelatedNodeDTO> relatedNodes, int nodeRiskScore) {
        if (relatedNodes == null || relatedNodes.isEmpty()) {
            return "No related nodes found in the graph. This entity currently appears isolated.";
        }

        long highRiskCount = relatedNodes.stream()
                .filter(node -> "HIGH".equalsIgnoreCase(node.getRiskLevel()))
                .count();
        long mediumRiskCount = relatedNodes.stream()
                .filter(node -> "MEDIUM".equalsIgnoreCase(node.getRiskLevel()))
                .count();

        StringBuilder builder = new StringBuilder();
        builder.append("Related nodes found: ").append(relatedNodes.size()).append('\n');
        builder.append("High-risk related nodes: ").append(highRiskCount).append('\n');
        builder.append("Medium-risk related nodes: ").append(mediumRiskCount).append('\n');

        if (nodeRiskScore >= 70 && highRiskCount > 0) {
            builder.append("This looks like part of a broader malicious cluster.");
        } else if (nodeRiskScore < 20 && highRiskCount > 0) {
            builder.append("This entity is low risk on its own but is connected to higher-risk infrastructure.");
        } else {
            builder.append("Use the connected entities to refine triage priority and investigation scope.");
        }

        return builder.toString();
    }

    private String getNodeStatus(int riskScore) {
        if (riskScore >= 70) {
            return "MALICIOUS";
        }
        if (riskScore >= 40) {
            return "SUSPICIOUS";
        }
        if (riskScore >= 20) {
            return "CAUTION";
        }
        return "SAFE";
    }

    private boolean isInternalIP(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        return ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.startsWith("172.16.")
                || ip.startsWith("172.17.")
                || ip.startsWith("172.18.")
                || ip.startsWith("172.19.")
                || ip.startsWith("172.2")
                || ip.startsWith("172.30.")
                || ip.startsWith("172.31.")
                || ip.startsWith("127.")
                || ip.equals("::1")
                || ip.startsWith("fe80:");
    }

    private boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String safeUpper(String value) {
        return value == null ? "" : value.toUpperCase();
    }
}
