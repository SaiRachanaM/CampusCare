/* Authentication UI Logic */
document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('login-form');
  const registerForm = document.getElementById('register-form');
  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');
  const togglePasswordBtn = document.getElementById('toggle-password');
  const passwordInput = document.getElementById('login-password');

  // Check if user is already logged in
  checkExistingAuth();

  if (tabLogin && tabRegister) {
    tabLogin.addEventListener('click', () => switchTab('login'));
    tabRegister.addEventListener('click', () => switchTab('register'));
  }

  if (togglePasswordBtn && passwordInput) {
    togglePasswordBtn.addEventListener('click', () => {
      const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
      passwordInput.setAttribute('type', type);
      togglePasswordBtn.textContent = type === 'password' ? 'Show' : 'Hide';
    });
  }

  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const email = document.getElementById('login-email').value.trim();
      const password = document.getElementById('login-password').value;
      const submitBtn = loginForm.querySelector('button[type="submit"]');

      try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Logging in...';

        const res = await ApiClient.post('/auth/login', { email, password });
        if (res.success && res.data.token) {
          ApiClient.setToken(res.data.token);
          localStorage.setItem('current_user', JSON.stringify(res.data.user));
          showToast('Login successful! Redirecting...', 'success');

          setTimeout(() => {
            redirectBasedOnRole(res.data.user.role);
          }, 800);
        }
      } catch (err) {
        showToast(err.message || 'Login failed. Please check your credentials.', 'error');
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Login';
      }
    });
  }

  if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const name = document.getElementById('reg-name').value.trim();
      const email = document.getElementById('reg-email').value.trim();
      const password = document.getElementById('reg-password').value;
      const submitBtn = registerForm.querySelector('button[type="submit"]');

      try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Registering...';

        const res = await ApiClient.post('/auth/register', { name, email, password });
        if (res.success) {
          showToast('Account registered successfully! Please log in.', 'success');
          switchTab('login');
          document.getElementById('login-email').value = email;
        }
      } catch (err) {
        showToast(err.message || 'Registration failed. Please try again.', 'error');
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Create User Account';
      }
    });
  }
});

function switchTab(tab) {
  const loginForm = document.getElementById('login-form');
  const registerForm = document.getElementById('register-form');
  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');

  if (tab === 'login') {
    loginForm.style.display = 'block';
    registerForm.style.display = 'none';
    tabLogin.classList.add('active');
    tabRegister.classList.remove('active');
  } else {
    loginForm.style.display = 'none';
    registerForm.style.display = 'block';
    tabRegister.classList.add('active');
    tabLogin.classList.remove('active');
  }
}

async function checkExistingAuth() {
  const token = ApiClient.getToken();
  if (token && window.location.pathname.endsWith('index.html')) {
    try {
      const res = await ApiClient.get('/auth/me');
      if (res.success && res.data) {
        redirectBasedOnRole(res.data.role);
      }
    } catch (e) {
      ApiClient.clearAuth();
    }
  }
}

function redirectBasedOnRole(role) {
  if (role === 'MAINTENANCE_STAFF') {
    window.location.href = '/staff-dashboard.html';
  } else {
    window.location.href = '/user-dashboard.html';
  }
}

function handleLogout() {
  ApiClient.clearAuth();
  showToast('Logged out successfully', 'success');
  window.location.href = '/index.html';
}
