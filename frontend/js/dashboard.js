/* =====================================================
   dashboard.js - Analytics dashboard page logic
   ===================================================== */

function renderSkillTags(containerId, skills, emptyMessage, badgeClass) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!skills || skills.length === 0) {
        container.innerHTML = `<p class="text-muted">${escapeHtml(emptyMessage)}</p>`;
        return;
    }

    container.innerHTML = skills.map((skill) =>
        `<span class="badge ${badgeClass}">${escapeHtml(skill)}</span>`
    ).join(' ');
}

async function initDashboardPage() {
    initAppShell('dashboard');

    const user = ApiClient.getCurrentUser();
    const greetingEl = document.getElementById('dashboard-greeting');
    if (greetingEl && user) {
        greetingEl.textContent = `Welcome back, ${user.fullName.split(' ')[0]}`;
    }

    try {
        const response = await ApiClient.get('/dashboard');
        const data = response.data;

        document.getElementById('metric-ats-score').textContent =
            data.latestAtsScore !== null && data.latestAtsScore !== undefined ? data.latestAtsScore : '—';
        document.getElementById('metric-resume-count').textContent = data.resumeCount || 0;
        document.getElementById('metric-avg-interview-score').textContent =
            data.averageInterviewScore ? data.averageInterviewScore.toFixed(1) : '—';
        document.getElementById('metric-total-interviews').textContent = data.totalInterviewsAttempted || 0;

        renderSkillTags('dashboard-strong-skills', data.strongSkills, 'No strong skills identified yet.', 'badge-success');
        renderSkillTags('dashboard-weak-skills', data.weakSkills, 'No weak skills identified yet.', 'badge-danger');

        renderRecentInterviews(data.recentInterviews);
        renderRecentResumes(data.recentResumes);
    } catch (err) {
        showAlert('dashboard-alert', err.message);
    }
}

function renderRecentInterviews(interviews) {
    const container = document.getElementById('recent-interviews-list');
    if (!container) return;

    if (!interviews || interviews.length === 0) {
        container.innerHTML = '<p class="text-muted">No mock interviews attempted yet.</p>';
        return;
    }

    container.innerHTML = interviews.slice(0, 5).map((interview) => `
        <div class="recent-item">
            <div class="recent-item-main">
                <p class="recent-item-title">${escapeHtml(truncate(interview.questionText, 80))}</p>
                <span class="text-muted recent-item-date">${formatDate(interview.attemptedAt)}</span>
            </div>
            <span class="score-pill ${scoreClass(interview.score)}">${interview.score}</span>
        </div>
    `).join('');
}

function renderRecentResumes(resumes) {
    const container = document.getElementById('recent-resumes-list');
    if (!container) return;

    if (!resumes || resumes.length === 0) {
        container.innerHTML = '<p class="text-muted">No resumes uploaded yet.</p>';
        return;
    }

    container.innerHTML = resumes.slice(0, 5).map((resume) => `
        <div class="recent-item">
            <div class="recent-item-main">
                <p class="recent-item-title">${escapeHtml(resume.fileName)}</p>
                <span class="text-muted recent-item-date">${formatDate(resume.uploadDate)}</span>
            </div>
            ${resume.analysis && resume.analysis.atsScore !== null
                ? `<span class="score-pill ${scoreClass(resume.analysis.atsScore)}">${resume.analysis.atsScore}</span>`
                : '<span class="text-muted">—</span>'}
        </div>
    `).join('');
}

/* ---------- Interview History page ---------- */

async function initInterviewHistoryPage() {
    initAppShell('history');

    const container = document.getElementById('interview-history-list');
    const emptyState = document.getElementById('interview-history-empty');
    if (!container) return;

    try {
        const response = await ApiClient.get('/interview/history');
        const history = response.data || [];

        if (history.length === 0) {
            if (emptyState) emptyState.style.display = 'block';
            return;
        }

        container.innerHTML = history.map((item) => `
            <div class="card history-card">
                <div class="history-card-header">
                    <p class="history-question">${escapeHtml(item.questionText)}</p>
                    <span class="score-pill ${scoreClass(item.score)}">${item.score}</span>
                </div>
                <p class="text-secondary"><strong>Your answer:</strong> ${escapeHtml(truncate(item.answerText, 240))}</p>
                <p class="text-secondary"><strong>Feedback:</strong> ${escapeHtml(item.feedback)}</p>
                <span class="text-muted recent-item-date">${formatDate(item.attemptedAt)}</span>
            </div>
        `).join('');
    } catch (err) {
        showAlert('interview-history-alert', err.message);
    }
}

/* ---------- Profile page ---------- */

async function initProfilePage() {
    initAppShell('profile');

    try {
        const response = await ApiClient.get('/profile');
        const profile = response.data;

        document.getElementById('profile-fullName').value = profile.fullName || '';
        document.getElementById('profile-email').value = profile.email || '';
        document.getElementById('profile-phoneNumber').value = profile.phoneNumber || '';
        document.getElementById('profile-role').textContent = profile.role;
        document.getElementById('profile-created-at').textContent = formatDate(profile.createdAt);
    } catch (err) {
        showAlert('profile-alert', err.message);
    }

    const profileForm = document.getElementById('profile-form');
    if (profileForm) {
        profileForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            clearAlert('profile-alert');

            const fullName = document.getElementById('profile-fullName').value.trim();
            const phoneNumber = document.getElementById('profile-phoneNumber').value.trim();

            const submitBtn = document.getElementById('profile-submit');
            setButtonLoading(submitBtn, true, 'Saving...', 'Save changes');

            try {
                const response = await ApiClient.put('/profile', { fullName, phoneNumber });
                const updated = response.data;
                const user = ApiClient.getCurrentUser();
                user.fullName = updated.fullName;
                ApiClient.setCurrentUser(user);
                showAlert('profile-alert', 'Profile updated successfully.', 'success');
            } catch (err) {
                showAlert('profile-alert', err.message);
            } finally {
                setButtonLoading(submitBtn, false, '', 'Save changes');
            }
        });
    }

    const passwordForm = document.getElementById('password-form');
    if (passwordForm) {
        passwordForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            clearAlert('password-alert');

            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmNewPassword = document.getElementById('confirmNewPassword').value;

            if (newPassword !== confirmNewPassword) {
                showAlert('password-alert', 'New passwords do not match.');
                return;
            }

            const submitBtn = document.getElementById('password-submit');
            setButtonLoading(submitBtn, true, 'Updating...', 'Update password');

            try {
                await ApiClient.put('/profile/password', { currentPassword, newPassword });
                showAlert('password-alert', 'Password changed successfully.', 'success');
                passwordForm.reset();
            } catch (err) {
                showAlert('password-alert', err.message);
            } finally {
                setButtonLoading(submitBtn, false, '', 'Update password');
            }
        });
    }
}
