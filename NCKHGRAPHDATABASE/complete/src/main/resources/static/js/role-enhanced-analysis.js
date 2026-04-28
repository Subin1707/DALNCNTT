function detectRoleBasePath() {
    const path = window.location.pathname || "";
    if (path.startsWith("/admin")) return "/admin";
    if (path.startsWith("/staff")) return "/staff";
    return "/customer";
}

async function enhancedShowNodeInfo(d) {
    const box = document.getElementById("nodeInfo");
    if (!box) return;

    const basePath = detectRoleBasePath();
    box.style.display = "flex";
    box.innerHTML = '<div style="text-align:center;padding:20px;"><p>Loading analysis...</p></div>';

    try {
        const query = `nodeId=${encodeURIComponent(d.id)}&nodeType=${encodeURIComponent(d.type)}&nodeValue=${encodeURIComponent(d.value)}&riskLevel=${encodeURIComponent(d.riskLevel)}&riskScore=${encodeURIComponent(d.riskScore ?? 0)}`;

        const decisionRes = await fetch(`${basePath}/node-decision?${query}`, { method: "POST" });
        if (!decisionRes.ok) throw new Error(`Decision request failed: HTTP ${decisionRes.status}`);
        const decision = await decisionRes.json();

        const chatbotRes = await fetch(`${basePath}/node-analysis?${query}`, { method: "POST" });
        if (!chatbotRes.ok) throw new Error(`Analysis request failed: HTTP ${chatbotRes.status}`);
        const chatbot = await chatbotRes.json();

        renderEnhancedNodeInfo(d, decision, chatbot);
    } catch (e) {
        console.error("enhancedShowNodeInfo error:", e);
        box.innerHTML = `<div style="color:red;padding:20px;">
            <p>Khong tai duoc phan giai thich node.</p>
            <p style="font-size:12px; color:#666;">${e?.message || "Unknown error"}</p>
        </div>`;
    }
}

function renderEnhancedNodeInfo(nodeData, decision, chatbot) {
    const box = document.getElementById("nodeInfo");
    box.innerHTML = "";

    const card = document.createElement("div");
    card.className = "node-popup-card";
    card.style.cssText = "max-height: 80vh; overflow-y: auto; font-size: 13px;";

    const header = document.createElement("div");
    header.style.cssText = "border-bottom: 2px solid #007bff; padding-bottom: 10px; margin-bottom: 15px;";

    const h3 = document.createElement("h3");
    h3.style.cssText = "margin: 0 0 10px 0; color: #0f172a;";
    h3.textContent = `${nodeData.type}: ${nodeData.value}`;
    header.appendChild(h3);

    const idSpan = document.createElement("small");
    idSpan.style.cssText = "color: #64748b;";
    idSpan.textContent = `ID: ${nodeData.id}`;
    header.appendChild(idSpan);
    card.appendChild(header);

    if (decision) {
        card.appendChild(createDecisionSection(decision));
    }
    if (chatbot) {
        card.appendChild(createTextSection("Analysis", "#8b5cf6", "#f9fafb", chatbot.status ? `Status: ${chatbot.status}\n\n${chatbot.analysisDescription || ""}` : (chatbot.analysisDescription || "")));
        card.appendChild(createTextSection("Risk Assessment", "#f59e0b", "#fffbeb", chatbot.riskAssessment || ""));
        card.appendChild(createThreatSection(chatbot));
        card.appendChild(createListSection("Recommended Actions", "#0ea5e9", "#eff6ff", chatbot.recommendedActions || []));
        card.appendChild(createGraphSection(chatbot));
    }

    const btn = document.createElement("button");
    btn.textContent = "Close";
    btn.style.cssText = "width: 100%; padding: 8px; margin-top: 15px; background: #64748b; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: 600;";
    btn.onclick = () => {
        box.style.display = "none";
    };
    card.appendChild(btn);
    box.appendChild(card);
}

function createDecisionSection(decision) {
    const section = document.createElement("div");
    section.style.cssText = "background: #f0f9ff; border-left: 4px solid #0ea5e9; padding: 10px; margin-bottom: 15px; border-radius: 4px;";

    const title = document.createElement("h4");
    title.style.cssText = "margin: 0 0 8px 0; color: #0c63e4;";
    title.innerHTML = `Decision: ${getDecisionBadge(decision.decision)}`;
    section.appendChild(title);

    const reason = document.createElement("p");
    reason.style.cssText = "margin: 0 0 8px 0; color: #333;";
    reason.textContent = decision.reason || "";
    section.appendChild(reason);

    const action = document.createElement("p");
    action.style.cssText = "margin: 0; color: #555; font-weight: 500; line-height: 1.5;";
    action.innerHTML = decision.actionDescription || "";
    section.appendChild(action);
    return section;
}

function createTextSection(titleText, accentColor, bgColor, contentText) {
    const section = document.createElement("div");
    section.style.cssText = `background: ${bgColor}; border-left: 4px solid ${accentColor}; padding: 10px; margin-bottom: 12px; border-radius: 4px;`;

    const title = document.createElement("h4");
    title.style.cssText = `margin: 0 0 8px 0; color: ${accentColor};`;
    title.textContent = titleText;
    section.appendChild(title);

    const content = document.createElement("p");
    content.style.cssText = "margin: 0; color: #555; line-height: 1.5; white-space: pre-wrap;";
    content.textContent = contentText || "No data";
    section.appendChild(content);
    return section;
}

function createThreatSection(chatbot) {
    const section = createTextSection("Threat Explanation", "#ef4444", "#fee2e2", chatbot.threatExplanation || "");
    if (chatbot.specificDangers && chatbot.specificDangers.length > 0) {
        const list = document.createElement("ul");
        list.style.cssText = "margin: 8px 0 0 0; padding-left: 20px; color: #555;";
        chatbot.specificDangers.slice(0, 5).forEach(item => {
            const li = document.createElement("li");
            li.textContent = item;
            list.appendChild(li);
        });
        section.appendChild(list);
    }
    return section;
}

function createListSection(titleText, accentColor, bgColor, items) {
    const section = document.createElement("div");
    section.style.cssText = `background: ${bgColor}; border-left: 4px solid ${accentColor}; padding: 10px; margin-bottom: 12px; border-radius: 4px;`;

    const title = document.createElement("h4");
    title.style.cssText = `margin: 0 0 8px 0; color: ${accentColor};`;
    title.textContent = titleText;
    section.appendChild(title);

    const list = document.createElement("ol");
    list.style.cssText = "margin: 0; padding-left: 20px; color: #555;";
    (items || []).forEach(item => {
        const li = document.createElement("li");
        li.style.cssText = "margin: 4px 0; line-height: 1.4;";
        li.textContent = item;
        list.appendChild(li);
    });
    section.appendChild(list);
    return section;
}

function createGraphSection(chatbot) {
    const section = createTextSection("Graph Intelligence", "#10b981", "#f0fdf4", chatbot.graphIntelligence || "No related nodes detected");
    if (chatbot.relatedNodes && chatbot.relatedNodes.length > 0) {
        const title = document.createElement("p");
        title.style.cssText = "margin: 8px 0 4px 0; font-weight: 600; color: #333;";
        title.textContent = `Related Nodes (${chatbot.relatedNodes.length}):`;
        section.appendChild(title);

        chatbot.relatedNodes.slice(0, 5).forEach(relNode => {
            const nodeDiv = document.createElement("div");
            nodeDiv.style.cssText = "background: white; border: 1px solid #d1d5db; padding: 6px; margin: 4px 0; border-radius: 3px; font-size: 12px;";
            nodeDiv.innerHTML = `
                <p style="margin:0 0 2px 0; font-weight:600;">${relNode.nodeType}: ${relNode.nodeValue}</p>
                <p style="margin:0; color:#666; font-size:11px;">
                    Risk: <span style="color:${getRiskColor(relNode.riskLevel)}">${relNode.riskLevel}</span>
                    | Via: ${relNode.relationship || "unknown"}<br/>
                    Reason: ${relNode.reason || "N/A"}
                </p>`;
            section.appendChild(nodeDiv);
        });
    }
    return section;
}

function getDecisionBadge(decision) {
    const styles = {
        BLOCK: { bg: "#ef4444", text: "white" },
        MONITOR: { bg: "#f59e0b", text: "white" },
        ALLOW: { bg: "#10b981", text: "white" }
    };
    const style = styles[decision] || styles.ALLOW;
    return `<span style="background:${style.bg}; color:${style.text}; padding:4px 12px; border-radius:4px; font-weight:600;">${decision || "ALLOW"}</span>`;
}

function getRiskColor(riskLevel) {
    const colors = { HIGH: "#ef4444", MEDIUM: "#f59e0b", LOW: "#10b981" };
    return colors[(riskLevel || "").toUpperCase()] || "#666";
}

window.enhancedShowNodeInfo = enhancedShowNodeInfo;
