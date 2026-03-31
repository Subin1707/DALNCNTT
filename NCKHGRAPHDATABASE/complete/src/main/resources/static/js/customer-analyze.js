// customer-analyze.js – Sync with admin behavior (Session + Graph refresh + Highlight)
document.addEventListener("DOMContentLoaded", () => {

    const resultDiv = document.getElementById("analyzeResult");

    /* ================= UTIL ================= */
    function normalizeInput(type, value) {
        if (value == null) return null;
        value = String(value).trim();
        if (!value) return null;

        if (type === "email") return value.toLowerCase();
        if (type === "url")   return value.toLowerCase().replace(/\s+/g, "");
        if (type === "ip")    return value.replace(/[^0-9.]/g, "");
        if (type === "domain") return value.toLowerCase();
        if (type === "fileHash") return value.toLowerCase().replace(/\s+/g, "");
        return value;
    }

    function waitForFetchGraph() {
        return new Promise(resolve => {
            const timer = setInterval(() => {
                if (typeof window.fetchGraph === "function") {
                    clearInterval(timer);
                    resolve();
                }
            }, 50);
        });
    }

    /* ================= ANALYZE ================= */
    document.getElementById("analyzeBtn")?.addEventListener("click", async () => {

        const email = normalizeInput("email", document.getElementById("analyzeEmail")?.value);
        const ip    = normalizeInput("ip",    document.getElementById("analyzeIP")?.value);
        const url   = normalizeInput("url",   document.getElementById("analyzeURL")?.value);
        const domain = normalizeInput("domain", document.getElementById("analyzeDomain")?.value);
        const fileNode = normalizeInput("fileNode", document.getElementById("analyzeFileNode")?.value);
        const fileHash = normalizeInput("fileHash", document.getElementById("analyzeFileHash")?.value);
        const victimAccount = normalizeInput("victimAccount", document.getElementById("analyzeVictimAccount")?.value);

        if (!email && !ip && !url && !domain && !fileNode && !fileHash && !victimAccount) {
            alert("Vui lòng nhập ít nhất 1 trong: Email / IP / URL / Domain / File name / File hash / Victim account");
            return;
        }

        const payload = {};
        if (email) payload.email = email;
        if (ip)    payload.ip    = ip;
        if (url)   payload.url   = url;
        if (domain) payload.domain = domain;
        if (fileNode) payload.fileNode = fileNode;
        if (fileHash) payload.fileHash = fileHash;
        if (victimAccount) payload.victimAccount = victimAccount;

        const highlightValues = [email, ip, url, domain, fileNode, fileHash, victimAccount].filter(Boolean);

        try {

            if (resultDiv) {
                resultDiv.innerHTML = "";
                resultDiv.style.display = "none";
            }

            const btn = document.getElementById("analyzeBtn");
            if (btn) btn.disabled = true;

            const res = await fetch("/customer/analyze", {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const text = await res.text();
            let data = null;

            try {
                data = JSON.parse(text);
            } catch {
                console.error("Response không phải JSON:", text);
            }

            if (res.status === 403) {
                alert("Phiên customer đã hết hạn hoặc chưa đăng nhập!");
                return;
            }

            if (!res.ok) {
                console.error("HTTP ERROR:", res.status, text);
                alert(data?.message || "Phân tích thất bại!");
                return;
            }

            /* ================= SHOW RESULT ================= */
            if (resultDiv) {

                const riskLevel = (data?.scamType || "").toLowerCase();

                const verdictText =
                    data?.verdict === "ALLOW" ? "Cho phép" :
                    data?.verdict === "BLOCK" ? "Chặn" :
                    "—";

                const verdictClass =
                    data?.verdict === "ALLOW" ? "allow" : "block";

                const indicatorsHTML =
                    Array.isArray(data?.indicators) && data.indicators.length
                        ? data.indicators.join("<br>")
                        : "Không có dấu hiệu bất thường";

                resultDiv.innerHTML = `
                    <div class="result-card">

                        <div class="result-header">
                            <h3>Kết quả phân tích</h3>
                            <span class="badge ${riskLevel}">
                                ${data?.scamType || "Không xác định"}
                            </span>
                        </div>

                        <div class="result-row">
                            <span>Kết luận</span>
                            <span class="verdict ${verdictClass}">
                                ${verdictText}
                            </span>
                        </div>

                        <div class="result-row">
                            <span>Điểm rủi ro</span>
                            <span>${data?.riskScore ?? 0}</span>
                        </div>

                        <div class="progress-bar">
                            <div class="progress-fill"
                                 style="width:${Math.min(data?.riskScore ?? 0,100)}%">
                            </div>
                        </div>

                        <div class="result-row" style="flex-direction:column;margin-top:14px">
                            <span>Dấu hiệu phát hiện</span>
                            <div class="indicator-box">
                                ${indicatorsHTML}
                            </div>
                        </div>

                        <div style="text-align:right;margin-top:10px">
                            <button id="closeAnalyzeResult"
                                style="background:none;border:none;font-size:14px;cursor:pointer;color:#999">
                                Đóng
                            </button>
                        </div>

                    </div>
                `;

                resultDiv.style.display = "block";

                document.getElementById("closeAnalyzeResult").onclick =
                    () => resultDiv.style.display = "none";
            }

            /* ================= REFRESH GRAPH ================= */
            await waitForFetchGraph();

            const sessionId = data?.sessionId || null;

            const sessionSelect = document.getElementById("sessionSelect");
            if (sessionId && sessionSelect) {
                sessionSelect.value = sessionId;
            }

            if (typeof window.fetchGraph === "function") {
                await window.fetchGraph(sessionId, { force: true });
            }

        } catch (err) {
            console.error("JS ERROR:", err);
            alert("Không thể phân tích! Kiểm tra console.");
        } finally {
            const btn = document.getElementById("analyzeBtn");
            if (btn) btn.disabled = false;
        }
    });
});
