/* =====================================================
   auth.js - Registration & Login page logic
   (Shared helpers: escapeHtml, showAlert, clearAlert, setButtonLoading,
    showFieldError, clearFieldErrors, initAppShell - all defined in api.js)
   ===================================================== */

function initRegisterPage() {
    redirectIfAuthenticated();

    const form = document.getElementById('register-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAlert('register-alert');
        clearFieldErrors(['fullName', 'email', 'password', 'phoneNumber']);

        const fullName = document.getElementById('fullName').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const phoneNumber = document.getElementById('phoneNumber').value.trim();

        if (password !== confirmPassword) {
            showFieldError('confirmPassword', 'Passwords do not match');
            return;
        }

        const submitBtn = document.getElementById('register-submit');
        setButtonLoading(submitBtn, true, 'Creating account...', 'Create account');

        try {
            const response = await ApiClient.post('/auth/register', {
                fullName, email, password, phoneNumber: phoneNumber || null
            });
            const data = response.data;
            ApiClient.setToken(data.token);
            ApiClient.setCurrentUser({
                userId: data.userId,
                fullName: data.fullName,
                email: data.email,
                role: data.role
            });
            window.location.href = 'dashboard.html';
        } catch (err) {
            if (err.validationErrors) {
                Object.entries(err.validationErrors).forEach(([field, msg]) => showFieldError(field, msg));
            } else {
                showAlert('register-alert', err.message);
            }
        } finally {
            setButtonLoading(submitBtn, false, '', 'Create account');
        }
    });
}

function initLoginPage() {
    redirectIfAuthenticated();

    const form = document.getElementById('login-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAlert('login-alert');
        clearFieldErrors(['email', 'password']);

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        const submitBtn = document.getElementById('login-submit');
        setButtonLoading(submitBtn, true, 'Signing in...', 'Sign in');

        try {
            const response = await ApiClient.post('/auth/login', { email, password });
            const data = response.data;
            ApiClient.setToken(data.token);
            ApiClient.setCurrentUser({
                userId: data.userId,
                fullName: data.fullName,
                email: data.email,
                role: data.role
            });
            window.location.href = 'dashboard.html';
        } catch (err) {
            if (err.validationErrors) {
                Object.entries(err.validationErrors).forEach(([field, msg]) => showFieldError(field, msg));
            } else {
                showAlert('login-alert', err.message);
            }
        } finally {
            setButtonLoading(submitBtn, false, '', 'Sign in');
        }
    });
}
