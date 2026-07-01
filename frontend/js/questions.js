/* =====================================================
   questions.js - AI question generator page logic
   ===================================================== */

let generatedQuestionsCache = [];

function renderQuestionsList(questions) {
    const container = document.getElementById('questions-list');
    const emptyState = document.getElementById('questions-empty');
    if (!container) return;

    if (!questions || questions.length === 0) {
        container.innerHTML = '';
        if (emptyState) emptyState.style.display = 'block';
        return;
    }

    if (emptyState) emptyState.style.display = 'none';

    container.innerHTML = questions.map((q, index) => `
        <div class="question-card">
            <div class="question-card-header">
                <span class="badge badge-accent">${escapeHtml(q.category)}</span>
                ${q.difficulty ? `<span class="badge badge-neutral">${escapeHtml(q.difficulty)}</span>` : ''}
            </div>
            <p class="question-text">${escapeHtml(q.questionText)}</p>
            <button class="btn btn-outline btn-sm" data-question-index="${index}" data-action="practice">
                Practice this question
            </button>
        </div>
    `).join('');

    container.querySelectorAll('[data-action="practice"]').forEach((btn) => {
        btn.addEventListener('click', () => {
            const q = generatedQuestionsCache[Number(btn.dataset.questionIndex)];
            sessionStorage.setItem('ipp_practice_question', JSON.stringify(q));
            window.location.href = 'mock-interview.html';
        });
    });
}

function initQuestionsPage() {
    initAppShell('questions');

    const form = document.getElementById('generate-questions-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAlert('questions-alert');

        const category = document.getElementById('category').value;
        const difficulty = document.getElementById('difficulty').value;
        const count = Number(document.getElementById('count').value) || 5;

        const submitBtn = document.getElementById('generate-submit');
        setButtonLoading(submitBtn, true, 'Generating...', 'Generate questions');

        try {
            const response = await ApiClient.post('/interview/questions', {
                category, difficulty: difficulty || null, count
            });
            generatedQuestionsCache = response.data;
            renderQuestionsList(generatedQuestionsCache);
        } catch (err) {
            showAlert('questions-alert', err.message);
        } finally {
            setButtonLoading(submitBtn, false, '', 'Generate questions');
        }
    });

    loadQuestionHistory();
}

async function loadQuestionHistory() {
    try {
        const response = await ApiClient.get('/interview/questions/history');
        const questions = response.data || [];

        if (questions.length === 0) {
            return;
        }

        generatedQuestionsCache = questions;
        renderQuestionsList(questions.slice(0, 10));
    } catch (err) {
        // Silently ignore - history is a nice-to-have on initial load,
        // and the empty state already covers the "nothing yet" case.
    }
}
