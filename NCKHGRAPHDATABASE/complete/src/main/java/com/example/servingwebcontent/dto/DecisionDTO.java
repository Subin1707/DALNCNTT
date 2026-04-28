package com.example.servingwebcontent.dto;

public class DecisionDTO {
    private String decision; // ALLOW, MONITOR, BLOCK
    private String reason;
    private int riskScore;
    private String riskLevel;
    private String actionDescription;

    public DecisionDTO() {}

    public DecisionDTO(String decision, String reason, int riskScore, String riskLevel, String actionDescription) {
        this.decision = decision;
        this.reason = reason;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.actionDescription = actionDescription;
    }

    // Getters and Setters
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getActionDescription() { return actionDescription; }
    public void setActionDescription(String actionDescription) { this.actionDescription = actionDescription; }
}
