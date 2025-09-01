// API Base URL
const BASE_URL = 'http://localhost:4322';  // User management service port

// DOM Elements
const authContainer = document.getElementById('auth-container');
const appContainer = document.getElementById('app-container');
const authForm = document.getElementById('auth-form');
const authTitle = document.getElementById('auth-title');
const authButton = document.getElementById('auth-button');
const toggleAuth = document.getElementById('toggle-auth');
// const toggleAuth2 = document.getElementById('toggle-auth2');
const errorContainer = document.getElementById('error-container');
const successContainer = document.getElementById('success-container');
const userDisplay = document.getElementById('user-display');

// Auth state
let isLoginMode = true;
let currentUser = null;
let authToken = null;

// Check if user is already logged in (from localStorage)
function checkAuthState() {
  const token = localStorage.getItem('token');
  const username = localStorage.getItem('username');
  const role = localStorage.getItem('role');
  
  if (token && username) {
    authToken = token;
    currentUser = { username, role };
    showApp();
  } else {
    showAuth();
  }
}

// Switch between login and register forms
toggleAuth.addEventListener('click', () => {
  isLoginMode = !isLoginMode;
  authTitle.textContent = isLoginMode ? 'Login' : 'Register';
  authButton.textContent = isLoginMode ? 'Login' : 'Register';
  toggleAuth.textContent = isLoginMode ? 'Register here' : 'Login here';
  // toggleAuth2.textContent = isLoginMode ? "Don't have an account?" : 'Already have an account?';
  errorContainer.textContent = '';
  successContainer.textContent = '';
});

// Handle auth form submission
authForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;
  console.log("username:",username);
  if (isLoginMode) {
    // Login
    try {
      const response = await fetch(`${BASE_URL}/api/users/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
      });
      
      if (!response.ok) {
        throw new Error('Login failed');
      }
      
      const data = await response.json();
      
      // Save auth data
      authToken = data.token;
      currentUser = {
        username: data.username,
        role: data.role
      };
      
      // Save in localStorage
      localStorage.setItem('token', authToken);
      localStorage.setItem('username', currentUser.username);
      localStorage.setItem('role', currentUser.role);
      
      showApp();
    } catch (error) {
      console.error('Login error:', error);
      errorContainer.textContent = 'Invalid username or password';
    }
  } else {
    // Register
    try {
      // First check if username exists
      const checkResponse = await fetch(`${BASE_URL}/api/users/check-username/${username}`);
      if (!checkResponse.ok) {
        throw new Error('Failed to check username availability');
      }
      
      const checkData = await checkResponse.json();
      console.log('Username check response:', checkData);
      
      if (checkData.exists) {
        errorContainer.textContent = 'Username already exists';
        return;
      }
      
      // Register new user
      const response = await fetch(`${BASE_URL}/api/users`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
      });
      
      if (!response.ok) {
        throw new Error('Registration failed');
      }
      
      // Switch to login mode after successful registration
      isLoginMode = true;
      authTitle.textContent = 'Login';
      authButton.textContent = 'Login';
      toggleAuth.textContent = 'Register here';
      // toggleAuth2.textContent = "Don't have an account?";
      successContainer.textContent = 'Registration successful! Please login.';
      errorContainer.textContent = '';
    } catch (error) {
      console.error('Registration error:', error);
      errorContainer.textContent = 'Registration failed: ' + error.message;
    }
  }
});

// Show auth form
function showAuth() {
  authContainer.style.display = 'flex';
  appContainer.style.display = 'none';
}

// Show main app
function showApp() {
  authContainer.style.display = 'none';
  appContainer.style.display = 'flex';
  
  // Update user display
  userDisplay.textContent = `Logged in as: ${currentUser.username} (${currentUser.role})`;
  
  // Load initial content
  loadProfile();
}

// Logout function
async function logout() {
  try {
    await fetch(`${BASE_URL}/api/users/logout`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
  } catch (error) {
    console.error('Logout API call failed:', error);
  }
  
  // Clear auth data regardless of API call success
  localStorage.removeItem('token');
  localStorage.removeItem('username');
  localStorage.removeItem('role');
  authToken = null;
  currentUser = null;
  
  showAuth();
}

// Profile page
function loadProfile() {
  const container = document.getElementById('main-content');
  container.innerHTML = `
    <h2>User Profile</h2>
    <div class="card">
      <h3>Username: ${currentUser.username}</h3>
      <p>Role: ${currentUser.role}</p>
    </div>
  `;
}

document.addEventListener('DOMContentLoaded', () => {  
  checkAuthState();
});
