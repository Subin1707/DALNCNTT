package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.DecisionDTO;
import org.springframework.stereotype.Service;

@Service
public class DecisionService {

    // Threshold definitions
    private static final int HIGH_RISK_THRESHOLD = 70;
    private static final int MEDIUM_RISK_THRESHOLD = 40;
    private static final int LOW_RISK_THRESHOLD = 0;

    /**
     * Convert risk score to decision (ALLOW/MONITOR/BLOCK)
     * HIGH (>= 70) → BLOCK
     * MEDIUM (40-69) → MONITOR
     * LOW (< 40) → ALLOW
     */
    public DecisionDTO makeDecision(int riskScore, String riskLevel) {
        String decision;
        String reason;
        String actionDescription;

        if (riskScore >= HIGH_RISK_THRESHOLD) {
            decision = "BLOCK";
            reason = "Risk score is HIGH (" + riskScore + "). Potential threat detected.";
            actionDescription = "🚨 IMMEDIATE ACTION: Block this node and all related connections. Notify security team. Perform detailed investigation.";
        } else if (riskScore >= MEDIUM_RISK_THRESHOLD) {
            decision = "MONITOR";
            reason = "Risk score is MEDIUM (" + riskScore + "). Suspicious activity detected.";
            actionDescription = "⚠️ MONITORING: Keep this node under surveillance. Set up alerts for suspicious activity. Review related nodes. Consider temporary blocking if risk increases.";
        } else {
            decision = "ALLOW";
            reason = "Risk score is LOW (" + riskScore + "). No immediate threat.";
            actionDescription = "✅ ALLOW: Node is safe to proceed. Continue regular monitoring. Check periodically for changes.";
        }

        return new DecisionDTO(decision, reason, riskScore, riskLevel, actionDescription);
    }

    public DecisionDTO makeDecisionFromDTO(com.example.servingwebcontent.dto.HybridRiskScoreDTO riskResult) {
        if (riskResult == null) {
            return new DecisionDTO("ALLOW", "No risk data available", 0, "LOW", "✅ ALLOW: No threat detected");
        }
        return makeDecision((int) riskResult.getFinalScore(), riskResult.getRiskLevel());
    }

    public int getHighRiskThreshold() {
        return HIGH_RISK_THRESHOLD;
    }

    public int getMediumRiskThreshold() {
        return MEDIUM_RISK_THRESHOLD;
    }

    public int getLowRiskThreshold() {
        return LOW_RISK_THRESHOLD;
    }
}
