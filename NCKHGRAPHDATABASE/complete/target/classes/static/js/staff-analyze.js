// staff-analyze.js – FIX FULL (1 nút Phân tích)
// - POST /staff/analyze
// - Hiển thị kết quả
// - Reload graph
// - Highlight đúng node (normalize Email/IP/URL)

document.addEventListener("DOMContentLoaded", () => {

    const resultDiv = document.getElementById("analyzeResult");
    const analyzeBtn = document.getElementById("analyzeBtn");

    function normalize(type, value) {
        if (!value) return value;
        value = String(value).trim();
        if (type === "Email") return value.toLowerCase();
        if (type === "IPAddress") return value.replace(/[^0-9.]/g, "");
        if (type === "URL") return value.replace(/\s+/g, "");
        if (type === "Domain") return value.toLowerCase();
        if (type === "FileHash") return value.toLowerCase().replace(/\s+/g, "");
        return value;
    }

    function safeText(v) {
        return (v === null || v === undefined || v === "") ? "—" : String(v);
    }

    if (!analyzeBtn) return;

    analyzeBtn.addEventListener("click", async () => {
        const emailRaw = document.getElementById("analyzeEmail")?.value?.trim() || "";
        const ipRaw    = document.getElementById("analyzeIP")?.value?.trim() || "";
        const urlRaw   = document.getElementById("analyzeURL")?.value?.trim() || "";
        const domainRaw = document.getElementById("analyzeDomain")?.value?.trim() || "";
        const fileNodeRaw = document.getElementById("analyzeFileNode")?.value?.trim() || "";
        const fileHashRaw = document.getElementById("analyzeFileHash")?.value?.trim() || "";
        const victimRaw = document.getElementById("analyzeVictimAccount")?.value?.trim() || "";

        const email = normalize("Email", emailRaw);
        const ip    = normalize("IPAddress", ipRaw);
        const url   = normalize("URL", urlRaw);
        const domain = normalize("Domain", domainRaw);
        const fileNode = normalize("File", fileNodeRaw);
        const fileHash = normalize("FileHash", fileHashRaw);
        const victimAccount = normalize("VictimAccount", victimRaw);

        if (!email && !ip && !url && !domain && !fileNode && !fileHash && !victimAccount) {
            return alert("Vui lòng nhập ít nhất 1 trong: Email / IP / URL / Domain / File name / File hash / Victim account");
        }

        const payload = {};
        if (email) payload.email = email;
        if (ip) payload.ip = ip;
        if (url) payload.url = url;
        if (domain) payload.domain = domain;
        if (fileNode) payload.fileNode = fileNode;
        if (fileHash) payload.fileHash = fileHash;
        if (victimAccount) payload.victimAccount = victimAccount;

        try {
            if (resultDiv) {
                resultDiv.innerHTML = "";
                resultDiv.style.display = "none";
            }

            analyzeBtn.disabled = true;

            const res = await fetch("/staff/analyze", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (res.status === 403) {
                alert("Phiên staff đã hết hạn!");
                return;
            }
            if (!res.ok) {
                throw new Error("Phân tích thất bại!");
            }

            const data = await res.json();

            // Hiển thị kết quả phân tích
            if (resultDiv) {
                resultDiv.innerHTML = `
                    <div style="
                        padding:14px;
                        border-radius:10px;
                        background:#fff;
                        box-shadow:0 8px 24px rgba(0,0,0,.25);
                        max-width:400px;
                        word-wrap:break-word;
                        font-family:sans-serif;
                    ">
                        <div style="display:flex;justify-content:space-between;align-items:center">
                            <b>Kết quả phân tích</b>
                            <button id="closeAnalyzeResult"
                                style="background:none;border:none;font-size:16px;cursor:pointer">✖</button>
                        </div>
                        <hr>
                        <b>Verdict:</b> ${safeText(data.verdict)}<br>
                        <b>Scam type:</b> ${safeText(data.scamType)}<br>
                        <b>Risk score:</b> ${data.riskScore ?? 0}<br>
                        <b>Indicators:</b>
                        ${
                            Array.isArray(data.indicators) && data.indicators.length
                                ? data.indicators.join(", ")
                                : "Không có"
                        }
                    </div>
                `;
                resultDiv.style.display = "block";

                const closeBtn = document.getElementById("closeAnalyzeResult");
                if (closeBtn) closeBtn.onclick = () => (resultDiv.style.display = "none");
            }

            // REFRESH GRAPH + TABLE
            if (typeof window.fetchGraph === "function") {
                await window.fetchGraph(); // reload toàn bộ graph
            }

            // HIGHLIGHT node vừa phân tích (so sánh normalize để không lệch)
            if (typeof window.highlightNodeValues === "function") {
                const nodes = window.allNodes || [];

                const valuesToHighlight = [];
                if (email && nodes.some(n => normalize(n.type, n.value) === email)) valuesToHighlight.push(email);
                if (ip && nodes.some(n => normalize(n.type, n.value) === ip)) valuesToHighlight.push(ip);
                if (url && nodes.some(n => normalize(n.type, n.value) === url)) valuesToHighlight.push(url);
                if (domain && nodes.some(n => normalize(n.type, n.value) === domain)) valuesToHighlight.push(domain);
                if (fileNode && nodes.some(n => normalize(n.type, n.value) === fileNode)) valuesToHighlight.push(fileNode);
                if (fileHash && nodes.some(n => normalize(n.type, n.value) === fileHash)) valuesToHighlight.push(fileHash);
                if (victimAccount && nodes.some(n => normalize(n.type, n.value) === victimAccount)) valuesToHighlight.push(victimAccount);

                window.highlightNodeValues(valuesToHighlight);
            }

        } catch (err) {
            console.error(err);
            alert(err?.message || "Không thể phân tích!");
        } finally {
            analyzeBtn.disabled = false;
        }
    });

});
