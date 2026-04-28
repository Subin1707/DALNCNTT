# 🧪 Quick Testing Guide - New Features

## Test Scenarios

### Scenario 1: Test Decision Service
```java
// In any service/controller
@Autowired
private DecisionService decisionService;

public void testDecision() {
    // HIGH RISK
    DecisionDTO decision1 = decisionService.makeDecision(85, "HIGH");
    System.out.println(decision1.getDecision()); // BLOCK
    
    // MEDIUM RISK
    DecisionDTO decision2 = decisionService.makeDecision(55, "MEDIUM");
    System.out.println(decision2.getDecision()); // MONITOR
    
    // LOW RISK
    DecisionDTO decision3 = decisionService.makeDecision(25, "LOW");
    System.out.println(decision3.getDecision()); // ALLOW
}
```

---

### Scenario 2: Test Alert Logging
```java
// In any service
@Autowired
private AlertLoggingService alertLoggingService;

public void testAlertLogging() {
    // Log an alert
    alertLoggingService.logAlert(
        "IP_192.168.1.1",
        "IP",
        "192.168.1.1",
        "HIGH",
        85,
        "BLOCK"
    );
    
    // Log a detection
    alertLoggingService.logDetection(
        "IP_192.168.1.1",
        "IP",
        "192.168.1.1",
        "HIGH",
        "Suspicious IP with C2 characteristics",
        "C2 Command and Control activities detected",
        Arrays.asList("DOMAIN_evil.com", "URL_http://evil.com/payload")
    );
    
    // Get statistics
    Map<String, Object> stats = alertLoggingService.getStatistics();
    System.out.println("Total Alerts: " + stats.get("totalAlerts"));
    System.out.println("Block Actions: " + stats.get("blockActions"));
}
```

---

### Scenario 3: Test Enhanced Chatbot
```java
// In any service
@Autowired
private EnhancedChatbotService chatbotService;

public void testChatbot() {
    ChatbotResponseDTO response = chatbotService.generateAnalysis(
        "IP_192.168.1.1",    // nodeId
        "IP",                  // nodeType
        "192.168.1.1",        // nodeValue
        "HIGH",               // riskLevel
        85,                   // riskScore
        Arrays.asList(        // indicators
            "Communicating with known C2 server",
            "High volume of DDoS traffic",
            "Suspicious port scanning"
        )
    );
    
    System.out.println(response.getStatus()); // 🔴 MALICIOUS
    System.out.println(response.getAnalysisDescription());
    System.out.println(response.getRiskAssessment());
    System.out.println(response.getThreatExplanation());
    System.out.println(response.getRecommendedActions());
    System.out.println(response.getRelatedNodes());
}
```

---

### Scenario 4: Test API Endpoints

#### Test Decision API
```bash
curl -X POST "http://localhost:8080/customer/node-decision" \
  -G \
  -d "nodeId=IP_192.168.1.1" \
  -d "nodeType=IP" \
  -d "nodeValue=192.168.1.1" \
  -d "riskLevel=HIGH" \
  -d "riskScore=85"

# Response:
# {
#   "decision": "BLOCK",
#   "reason": "Risk score is HIGH (85). Potential threat detected.",
#   "riskScore": 85,
#   "riskLevel": "HIGH",
#   "actionDescription": "🚨 IMMEDIATE ACTION: Block this node and all related connections..."
# }
```

#### Test Chatbot API
```bash
curl -X POST "http://localhost:8080/customer/node-analysis" \
  -G \
  -d "nodeId=DOMAIN_example.com" \
  -d "nodeType=DOMAIN" \
  -d "nodeValue=example.com" \
  -d "riskLevel=HIGH" \
  -d "riskScore=75"

# Response includes:
# {
#   "nodeId": "DOMAIN_example.com",
#   "analysisDescription": "Domain Name: example.com...",
#   "status": "🔴 MALICIOUS",
#   "riskAssessment": "Risk Level: HIGH...",
#   "threatExplanation": "This domain shows HIGH risk...",
#   "specificDangers": [
#     "🔗 Can be exploited as intermediary for attacks",
#     "🎣 Could host phishing pages or malware",
#     ...
#   ],
#   "recommendedActions": [
#     "🚨 IMMEDIATE: Block this domain from accessing your systems",
#     ...
#   ],
#   "relatedNodes": [
#     {
#       "nodeId": "IP_192.168.1.1",
#       "nodeType": "IP",
#       "nodeValue": "192.168.1.1",
#       "riskLevel": "HIGH",
#       "relationship": "HOSTED_ON",
#       "reason": "This IP hosts this domain"
#     }
#   ],
#   "graphIntelligence": "Graph Analysis Found 3 related node(s)..."
# }
```

#### Test Alerts API
```bash
curl "http://localhost:8080/customer/alerts"

# Response:
# {
#   "success": true,
#   "count": 5,
#   "alerts": [
#     {
#       "nodeId": "IP_192.168.1.1",
#       "nodeType": "IP",
#       "nodeValue": "192.168.1.1",
#       "riskLevel": "HIGH",
#       "riskScore": 85,
#       "decision": "BLOCK",
#       "timestamp": "2024-04-29T14:30:45.123"
#     }
#   ]
# }
```

#### Test Statistics API
```bash
curl "http://localhost:8080/customer/log-statistics"

# Response:
# {
#   "success": true,
#   "statistics": {
#     "totalAlerts": 42,
#     "totalDetections": 15,
#     "blockActions": 12,
#     "monitorActions": 20,
#     "allowActions": 10,
#     "highRiskAlerts": 8
#   }
# }
```

---

## Frontend Testing

### Test 1: Click on a node in the graph
1. Go to Customer Dashboard
2. Look at the graph visualization
3. Click on any node (IP, Domain, URL, Email)
4. Expected: Enhanced panel appears showing all 4 layers + decision

### Test 2: Verify decision badge color
- 🚨 BLOCK = Red
- ⚠️ MONITOR = Orange  
- ✅ ALLOW = Green

### Test 3: Verify 4-layer display
- Layer 1: "📋 Analysis" section appears
- Layer 2: "⚠️ Risk Assessment" section appears
- Layer 3: "🔥 Threat Explanation" with specific dangers for node type
- Layer 4: "✅ Recommended Actions" with numbered list

### Test 4: Verify graph intelligence
- Related nodes listed
- High-risk related nodes highlighted
- Relationship types shown
- Reasons for relationship displayed

---

## Log File Testing

### Check Alert Logs
```bash
cat logs/alerts.log
# Output should look like:
# [2024-04-29 14:30:45.123] 🔔 ALERT | Node: IP_192.168.1.1 (IP) | Value: 192.168.1.1 | Risk: HIGH (Score: 85) | Decision: BLOCK
# [2024-04-29 14:31:12.456] 🔔 ALERT | Node: DOMAIN_evil.com (DOMAIN) | Value: evil.com | Risk: HIGH (Score: 78) | Decision: BLOCK
```

### Check Detection Logs
```bash
cat logs/detections.log
# Output should look like:
# [2024-04-29 14:30:45.123] 🎯 DETECTION | Node: IP_192.168.1.1 (IP) | Value: 192.168.1.1 | Risk: HIGH | Analysis: IP Address: 192.168.1.1... | Threats: ... | Related: [DOMAIN_evil.com, URL_http://evil.com]
```

---

## Performance Notes

### DecisionService
- ✅ O(1) - Instant decision making
- No database queries
- No network calls

### AlertLoggingService  
- ✅ O(1) write to memory
- Async file I/O (non-blocking)
- Filtered queries are O(n) where n = total alerts

### EnhancedChatbotService
- ⚠️ Neo4j query for related nodes (adjustable limit)
- Default: 5 related nodes per query
- Configurable via LIMIT in queries
- ~100-200ms per call (depends on graph size)

### Frontend
- ✅ Parallel API calls (decision + analysis)
- ✅ Responsive UI updates
- Single panel for all information

---

## Troubleshooting

### Issue: Enhanced analysis not showing
**Solution**: 
1. Check browser console for errors
2. Verify `customer-enhanced-analysis.js` is loaded
3. Check that session is authenticated

### Issue: Logs not writing
**Solution**:
1. Verify `logs/` directory exists
2. Check file permissions
3. Verify disk space available

### Issue: Neo4j queries slow
**Solution**:
1. Add indexes to Neo4j database
2. Reduce LIMIT parameter (currently 5)
3. Optimize graph structure

---

## Notes

- All endpoints require authentication (session check)
- Logs are thread-safe using CopyOnWriteArrayList
- Decision thresholds can be adjusted in DecisionService
- Chatbot responses are customizable per node type
- Graph queries can be extended for deeper analysis

