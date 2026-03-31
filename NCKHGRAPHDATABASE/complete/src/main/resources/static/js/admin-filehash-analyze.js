document.addEventListener("DOMContentLoaded", () => {
    const resultDiv = document.getElementById("analyzeResult");

    const normalizeHash = (raw) => {
        if (raw == null) return null;
        const s = String(raw).trim().toLowerCase();
        return s ? s : null;
    };

    const handleAnalyze = async (hashRaw) => {
        const fileHash = normalizeHash(hashRaw);
        if (!fileHash) return alert("Vui lòng nhập File hash");

        try {
            if (resultDiv) resultDiv.innerHTML = "";

            const btn = document.getElementById("analyzeFileHashBtn");
            if (btn) btn.disabled = true;

            const res = await fetch("/admin/analyze", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ fileHash })
            });

            if (res.status === 403) {
                alert("Phiên admin đã hết hạn!");
                return;
            }
            if (!res.ok) throw new Error("Phân tích thất bại!");

            const data = await res.json();

            if (resultDiv) {
                resultDiv.innerHTML = `
                    <div style="
                        padding:14px;
                        border-radius:10px;
                        background:#fff;
                        box-shadow:0 8px 24px rgba(0,0,0,.25);
                        max-width:420px;
                        word-wrap:break-word;
                        font-family:sans-serif;
                    ">
                        <div style="display:flex;justify-content:space-between;align-items:center">
                            <b>Kết quả phân tích File hash</b>
                            <button id="closeAnalyzeResult"
                                style="background:none;border:none;font-size:16px;cursor:pointer">✖</button>
                        </div>
                        <hr>
                        <b>Verdict:</b> ${data.verdict || "—"}<br>
                        <b>Scam type:</b> ${data.scamType || "—"}<br>
                        <b>Risk score:</b> ${data.riskScore ?? 0}<br>
                        <b>Indicators:</b>
                        ${Array.isArray(data.indicators) && data.indicators.length
                            ? data.indicators.join(", ")
                            : "Không có"}
                    </div>
                `;
                resultDiv.style.display = "block";
                document.getElementById("closeAnalyzeResult").onclick =
                    () => resultDiv.style.display = "none";
            }

            const sessionId = document.getElementById("sessionSelect")?.value || null;
            if (typeof window.fetchGraph === "function") {
                await window.fetchGraph(sessionId, { force: true });
            }

        } catch (err) {
            console.error(err);
            alert(err?.message || "Không thể phân tích File hash");
        } finally {
            const btn = document.getElementById("analyzeFileHashBtn");
            if (btn) btn.disabled = false;
        }
    };

    document.getElementById("analyzeFileHashBtn")?.addEventListener("click", () => {
        const val = document.getElementById("analyzeFileHash")?.value || "";
        handleAnalyze(val);
    });
});

