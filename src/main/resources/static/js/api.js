/* API Client & Utility Library */
const API_BASE_URL = '/api';

class ApiClient {
  static getToken() {
    return localStorage.getItem('jwt_token');
  }

  static setToken(token) {
    if (token) {
      localStorage.setItem('jwt_token', token);
    } else {
      localStorage.removeItem('jwt_token');
    }
  }

  static clearAuth() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('current_user');
  }

  static async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const token = this.getToken();

    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
      ...options,
      headers,
    };

    try {
      const response = await fetch(url, config);

      if (response.status === 401) {
        // Token expired or invalid -> clear auth and redirect to login
        this.clearAuth();
        if (!window.location.pathname.endsWith('index.html') && window.location.pathname !== '/') {
          window.location.href = '/index.html';
        }
      }

      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        const errorMsg = data.message || data.error || `HTTP Error ${response.status}`;
        throw new Error(errorMsg);
      }

      return data;
    } catch (err) {
      throw err;
    }
  }

  static get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  }

  static post(endpoint, body) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(body),
    });
  }

  static put(endpoint, body) {
    return this.request(endpoint, {
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    });
  }
}

// Global Toast Notification Helper
function showToast(message, type = 'success') {
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 4000);
}
