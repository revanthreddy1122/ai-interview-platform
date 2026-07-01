/* =====================================================
   api.js - Central API client
   Handles base URL, JWT attachment, and response parsing
   ===================================================== */

const API_BASE_URL = 'http://localhost:8080/api';
const TOKEN_KEY = 'interview_platform_token';
const USER_KEY = 'interview_platform_user';

const ApiClient = {

    getToken() {
        return localStorage.getItem(TOKEN_KEY);
    },

    setToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
    },

    clearToken() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
    },

    getCurrentUser() {
        const raw = localStorage.getItem(USER_KEY);
        return raw ? JSON.parse(raw) : null;
    },

    setCurrentUser(user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const headers = { ...(options.headers || {}) };

        const isFormData = options.body instanceof FormData;
        if (!isFormData) {
            headers['Content-Type'] = 'application/json';
        }

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        let response;
        try {
            response = await fetch(url, { ...options, headers });
        } catch (networkError) {
            throw new ApiError(
                'Unable to reach the server. Please check your connection and try again.',
                0,
                null
            );
        }

        let body = null;
        const contentType = response.headers.get('content-type') || '';
        if (contentType.includes('application/json')) {
            try {
                body = await response.json();
            } catch (parseError) {
                body = null;
            }
        }

        if (response.status === 401) {
            this.clearToken();
            if (!window.location.pathname.endsWith('login.html')
                && !window.location.pathname.endsWith('register.html')) {
                window.location.href = 'login.html';
            }
        }

        if (!response.ok) {
            const message = body && body.message ? body.message : `Request failed with status ${response.status}`;
            throw new ApiError(message, response.status, body);
        }

        return body;
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    post(endpoint, data) {
        const isFormData = data instanceof FormData;
        return this.request(endpoint, {
            method: 'POST',
            body: isFormData ? data : JSON.stringify(data)
        });
    },

    put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};

class ApiError extends Error {
    constructor(message, status, body) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.body = body;
        this.validationErrors = body && body.validationErrors ? body.validationErrors : null;
    }
}

function requireAuth() {
    if (!ApiClient.isAuthenticated()) {
        window.location.href = 'login.html';
    }
}

function redirectIfAuthenticated() {
    if (ApiClient.isAuthenticated()) {
        window.location.href = 'dashboard.html';
    }
}

/* =====================================================
   Shared UI helpers - used across all pages
   ===================================================== */

function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(str);
    return div.innerHTML;
}

function showAlert(containerId, message, type = 'danger') {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = `<div class="alert alert-${type}">${escapeHtml(message)}</div>`;
}

function clearAlert(containerId) {
    const container = document.getElementById(containerId);
    if (container) container.innerHTML = '';
}

function setButtonLoading(button, isLoading, loadingText, defaultText) {
    if (!button) return;
    button.disabled = isLoading;
    button.innerHTML = isLoading
        ? `<span class="spinner"></span> ${escapeHtml(loadingText)}`
        : defaultText;
}

function formatDate(isoString) {
    if (!isoString) return '—';
    const date = new Date(isoString);
    return date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
        + ' · ' + date.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
}

function formatFileSize(bytes) {
    if (!bytes) return '—';
    const kb = bytes / 1024;
    if (kb < 1024) return `${kb.toFixed(1)} KB`;
    return `${(kb / 1024).toFixed(1)} MB`;
}

function scoreClass(score) {
    if (score === null || score === undefined) return 'mid';
    if (score >= 75) return 'high';
    if (score >= 50) return 'mid';
    return 'low';
}

function truncate(str, maxLength) {
    if (!str) return '';
    return str.length > maxLength ? str.slice(0, maxLength) + '…' : str;
}

function showFieldError(fieldId, message) {
    const errorEl = document.getElementById(`${fieldId}-error`);
    const inputEl = document.getElementById(fieldId);
    if (errorEl) {
        errorEl.textContent = message;
        errorEl.classList.add('visible');
    }
    if (inputEl) {
        inputEl.style.borderColor = 'var(--color-danger)';
    }
}

function clearFieldErrors(fieldIds) {
    fieldIds.forEach((fieldId) => {
        const errorEl = document.getElementById(`${fieldId}-error`);
        const inputEl = document.getElementById(fieldId);
        if (errorEl) {
            errorEl.textContent = '';
            errorEl.classList.remove('visible');
        }
        if (inputEl) {
            inputEl.style.borderColor = '';
        }
    });
}

/* =====================================================
   Shared app shell (sidebar / topbar / theme) - all protected pages
   ===================================================== */

function initAppShell(activePage) {
    requireAuth();

    const user = ApiClient.getCurrentUser();
    const userNameEl = document.getElementById('current-user-name');
    if (userNameEl && user) {
        userNameEl.textContent = user.fullName;
    }

    document.querySelectorAll('.sidebar-link').forEach((link) => {
        if (link.dataset.page === activePage) {
            link.classList.add('active');
        }
    });

    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            ApiClient.clearToken();
            window.location.href = 'login.html';
        });
    }

    initThemeToggle();
}

function initThemeToggle() {
    const toggle = document.getElementById('theme-toggle');
    const stored = localStorage.getItem('ipp_theme');
    if (stored === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
    }
    updateThemeIcon();

    if (toggle) {
        toggle.addEventListener('click', () => {
            const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
            if (isDark) {
                document.documentElement.removeAttribute('data-theme');
                localStorage.setItem('ipp_theme', 'light');
            } else {
                document.documentElement.setAttribute('data-theme', 'dark');
                localStorage.setItem('ipp_theme', 'dark');
            }
            updateThemeIcon();
        });
    }
}

function updateThemeIcon() {
    const toggle = document.getElementById('theme-toggle');
    if (!toggle) return;
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    toggle.textContent = isDark ? '☀' : '☾';
}
