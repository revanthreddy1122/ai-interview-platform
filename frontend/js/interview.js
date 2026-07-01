/* =====================================================
   interview.js - Mock interview chatbot logic
   ===================================================== */

let currentInterviewQuestion = null;

function appendChatMessage(role, html) {
    const chatLog = document.getElementById('chat-log');
    if (!chatLog) return;

    const messageEl = document.createElement('div');
    messageEl.innerHTML = html;
    chatLog.appendChild(messageEl.firstElementChild);
    chatLog.scrollTop = chatLog.scrollHeight;
}

function appendBotQuestion(questionText) {
    appendChatMessage('bot', `
        <div class="chat-bubble interviewer">
            <span class="chat-bubble-label">Interviewer</span>
            <p>${escapeHtml(questionText)}</p>
        </div>
    `);
}

function appendUserAnswer(answerText) {
    appendChatMessage('user', `
        <div class="chat-bubble candidate">
            <span class="chat-bubble-label">You</span>
            <p>${escapeHtml(answerText)}</p>
        </div>
    `);
}

function appendEvaluation(evaluation) {
    const overallClass = scoreClass(evaluation.score);
    appendChatMessage('bot', `
        <div class="evaluation-card">
            <span class="chat-bubble-label">Evaluation</span>
            <div class="evaluation-scores">
                <div class="evaluation-score-item">
                    <span class="evaluation-score-number">${evaluation.score}</span>
                    <span class="evaluation-score-label">Overall</span>
                </div>
                <div class="evaluation-score-item">
                    <span class="evaluation-score-number">${evaluation.correctnessScore}</span>
                    <span class="evaluation-score-label">Correctness</span>
                </div>
                <div class="evaluation-score-item">
                    <span class="evaluation-score-number">${evaluation.completenessScore}</span>
                    <span class="evaluation-score-label">Completeness</span>
                </div>
                <div class="evaluation-score-item">
                    <span class="evaluation-score-number">${evaluation.communicationScore}</span>
                    <span class="evaluation-score-label">Communication</span>
                </div>
            </div>
            <p><strong>Feedback:</strong> ${escapeHtml(evaluation.feedback)}</p>
            <p><strong>Suggestion:</strong> ${escapeHtml(evaluation.suggestions)}</p>
        </div>
    `);
}

function loadNextQuestion() {
    const stored = sessionStorage.getItem('ipp_practice_question');
    if (stored) {
        currentInterviewQuestion = JSON.parse(stored);
        sessionStorage.removeItem('ipp_practice_question');
        appendBotQuestion(currentInterviewQuestion.questionText);
        return;
    }

    appendBotQuestion(
        'Tell me about a challenging technical problem you solved recently and how you approached it.'
    );
    currentInterviewQuestion = {
        questionId: null,
        questionText: 'Tell me about a challenging technical problem you solved recently and how you approached it.'
    };
}

function initMockInterviewPage() {
    initAppShell('mock-interview');

    const chatLog = document.getElementById('chat-log');
    const form = document.getElementById('answer-form');
    const answerInput = document.getElementById('answer-input');

    if (!chatLog || !form) return;

    loadNextQuestion();

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const answer = answerInput.value.trim();
        if (!answer || !currentInterviewQuestion) return;

        appendUserAnswer(answer);
        answerInput.value = '';
        answerInput.disabled = true;

        const submitBtn = document.getElementById('answer-submit');
        setButtonLoading(submitBtn, true, '', '<span class="spinner"></span>');

        try {
            const response = await ApiClient.post('/interview/evaluate', {
                questionId: currentInterviewQuestion.questionId,
                question: currentInterviewQuestion.questionText,
                answer
            });
            appendEvaluation(response.data);
        } catch (err) {
            appendChatMessage('bot', `
                <div class="chat-bubble interviewer">
                    <span class="chat-bubble-label">System</span>
                    <p class="text-danger">${escapeHtml(err.message)}</p>
                </div>
            `);
        } finally {
            answerInput.disabled = false;
            setButtonLoading(submitBtn, false, '', 'Send');
            answerInput.focus();
        }
    });

    const newQuestionBtn = document.getElementById('new-question-btn');
    if (newQuestionBtn) {
        newQuestionBtn.addEventListener('click', () => {
            window.location.href = 'questions.html';
        });
    }
}
