/* =====================================================
   resume.js - Resume upload & history page logic
   (Shared helpers: formatDate, formatFileSize, scoreClass, escapeHtml,
    showAlert, clearAlert, setButtonLoading, initAppShell - in api.js)
   ===================================================== */

function initUploadResumePage() {
    initAppShell('upload-resume');

    const dropZone = document.getElementById('dropzone');
    const fileInput = document.getElementById('resume-file-input');
    const uploadBtn = document.getElementById('upload-submit');
    const fileNameDisplay = document.getElementById('dropzone-filename');

    if (!dropZone || !fileInput) return;

    let selectedFile = null;

    function handleFileSelection(file) {
        if (!file) return;
        if (!file.name.toLowerCase().endsWith('.pdf')) {
            showAlert('upload-alert', 'Only PDF files are supported.');
            return;
        }
        if (file.size > 10 * 1024 * 1024) {
            showAlert('upload-alert', 'File size must not exceed 10MB.');
            return;
        }
        selectedFile = file;
        clearAlert('upload-alert');
        fileNameDisplay.textContent = file.name;
        fileNameDisplay.classList.add('visible');
        uploadBtn.disabled = false;
    }

    dropZone.addEventListener('click', () => fileInput.click());

    dropZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropZone.classList.add('dragover');
    });

    dropZone.addEventListener('dragleave', () => {
        dropZone.classList.remove('dragover');
    });

    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.classList.remove('dragover');
        const file = e.dataTransfer.files[0];
        handleFileSelection(file);
    });

    fileInput.addEventListener('change', (e) => {
        handleFileSelection(e.target.files[0]);
    });

    uploadBtn.addEventListener('click', async () => {
        if (!selectedFile) return;

        clearAlert('upload-alert');
        setButtonLoading(uploadBtn, true, 'Uploading...', 'Upload resume');

        try {
            const formData = new FormData();
            formData.append('file', selectedFile);

            const response = await ApiClient.post('/resume/upload', formData);
            const resume = response.data;

            showAlert('upload-alert', 'Resume uploaded successfully. Running AI analysis...', 'info');

            const analysisResponse = await ApiClient.post(`/resume/analyze/${resume.resumeId}`, {});
            sessionStorage.setItem('ipp_last_analysis', JSON.stringify({
                resumeId: resume.resumeId,
                fileName: resume.fileName,
                analysis: analysisResponse.data
            }));

            window.location.href = 'analysis.html';
        } catch (err) {
            showAlert('upload-alert', err.message);
            setButtonLoading(uploadBtn, false, '', 'Upload resume');
        }
    });
}

async function initResumeHistoryPage() {
    initAppShell('history');

    const listContainer = document.getElementById('resume-history-list');
    const emptyState = document.getElementById('resume-history-empty');
    if (!listContainer) return;

    try {
        const response = await ApiClient.get('/resume/history');
        const resumes = response.data || [];

        if (resumes.length === 0) {
            if (emptyState) emptyState.style.display = 'block';
            return;
        }

        listContainer.innerHTML = resumes.map((resume) => {
            const atsScore = resume.analysis ? resume.analysis.atsScore : null;
            const scoreBadge = atsScore !== null && atsScore !== undefined
                ? `<span class="score-pill ${scoreClass(atsScore)}">${atsScore}</span>`
                : '<span class="text-muted">Not analyzed</span>';

            return `
                <div class="resume-history-item">
                    <div class="resume-history-meta">
                        <div class="resume-history-icon">📄</div>
                        <div>
                            <p class="recent-item-title">${escapeHtml(resume.fileName)}</p>
                            <span class="text-muted recent-item-date">
                                ${formatFileSize(resume.fileSize)} · ${formatDate(resume.uploadDate)}
                            </span>
                        </div>
                    </div>
                    <div style="display:flex; align-items:center; gap: var(--space-4);">
                        ${scoreBadge}
                        <button class="btn btn-outline btn-sm" data-resume-id="${resume.resumeId}" data-action="view">
                            View analysis
                        </button>
                    </div>
                </div>
            `;
        }).join('');

        listContainer.querySelectorAll('[data-action="view"]').forEach((btn) => {
            btn.addEventListener('click', () => {
                viewResumeAnalysis(btn.dataset.resumeId);
            });
        });
    } catch (err) {
        showAlert('resume-history-alert', err.message);
    }
}

async function viewResumeAnalysis(resumeId) {
    try {
        const response = await ApiClient.get(`/resume/${resumeId}`);
        const resume = response.data;

        if (!resume.analysis) {
            const analysisResponse = await ApiClient.post(`/resume/analyze/${resumeId}`, {});
            resume.analysis = analysisResponse.data;
        }

        sessionStorage.setItem('ipp_last_analysis', JSON.stringify({
            resumeId: resume.resumeId,
            fileName: resume.fileName,
            analysis: resume.analysis
        }));

        window.location.href = 'analysis.html';
    } catch (err) {
        showAlert('resume-history-alert', err.message);
    }
}
