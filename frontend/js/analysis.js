/* =====================================================
   analysis.js - ATS analysis & job description matching
   ===================================================== */

function renderAnalysisList(containerId, items, emptyMessage, iconType, iconChar) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!items || items.length === 0) {
        container.innerHTML = `<p class="text-muted">${escapeHtml(emptyMessage)}</p>`;
        return;
    }

    container.innerHTML = `<ul class="analysis-list">${
        items.map((item) => `
            <li>
                <span class="analysis-list-icon ${iconType}">${iconChar}</span>
                <span>${escapeHtml(item)}</span>
            </li>
        `).join('')
    }</ul>`;
}

function renderGauge(score) {
    const fillEl = document.getElementById('gauge-tick-fill');
    const valueEl = document.getElementById('gauge-value-number');
    if (!fillEl || !valueEl) return;

    const safeScore = score === null || score === undefined ? 0 : score;
    const radius = 70;
    const circumference = 2 * Math.PI * radius;
    const offset = circumference - (safeScore / 100) * circumference;

    fillEl.setAttribute('stroke-dasharray', `${circumference}`);
    fillEl.setAttribute('stroke-dashoffset', `${offset}`);

    valueEl.textContent = score === null || score === undefined ? '—' : score;
}

function initAnalysisPage() {
    initAppShell('upload-resume');

    const stored = sessionStorage.getItem('ipp_last_analysis');
    const container = document.getElementById('analysis-container');
    const emptyState = document.getElementById('analysis-empty');

    if (!stored) {
        if (container) container.style.display = 'none';
        if (emptyState) emptyState.style.display = 'block';
        return;
    }

    const { resumeId, fileName, analysis } = JSON.parse(stored);

    document.getElementById('analysis-file-name').textContent = fileName;
    renderGauge(analysis.atsScore);
    renderAnalysisList('analysis-strengths', analysis.strengths, 'No specific strengths identified.', 'strength', '✓');
    renderAnalysisList('analysis-weaknesses', analysis.weaknesses, 'No specific weaknesses identified.', 'weakness', '!');
    renderAnalysisList('analysis-missing-skills', analysis.missingSkills, 'No missing skills detected.', 'weakness', '?');
    renderAnalysisList('analysis-suggestions', analysis.suggestions, 'No suggestions available.', 'suggestion', '→');

    const matchForm = document.getElementById('job-match-form');
    if (matchForm) {
        matchForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            clearAlert('job-match-alert');

            const jobDescription = document.getElementById('job-description').value.trim();
            if (!jobDescription) return;

            const submitBtn = document.getElementById('job-match-submit');
            setButtonLoading(submitBtn, true, 'Matching...', 'Match against this resume');

            try {
                const response = await ApiClient.post('/resume/match', {
                    resumeId: Number(resumeId),
                    jobDescription
                });
                const matchResult = response.data;

                document.getElementById('job-match-results').style.display = 'block';
                document.getElementById('match-percentage').textContent = `${matchResult.matchPercentage}%`;
                document.getElementById('match-percentage-fill').style.width = `${matchResult.matchPercentage}%`;
                renderAnalysisList('match-missing-skills', matchResult.missingSkills, 'No missing skills found.', 'weakness', '?');
                renderAnalysisList('match-suggestions', matchResult.suggestions, 'No suggestions available.', 'suggestion', '→');
            } catch (err) {
                showAlert('job-match-alert', err.message);
            } finally {
                setButtonLoading(submitBtn, false, '', 'Match against this resume');
            }
        });
    }
}
