const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'
const STORAGE_KEY = 'expenseTrackerAuth'

export function loadAuth() {
  const raw = sessionStorage.getItem(STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    sessionStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export function saveAuth(auth) {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(auth))
  window.dispatchEvent(new Event('expense-auth-changed'))
}

export function clearAuth() {
  sessionStorage.removeItem(STORAGE_KEY)
  window.dispatchEvent(new Event('expense-auth-changed'))
}

async function parseResponse(response) {
  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) return response.json()
  const text = await response.text()
  return text ? { message: text } : null
}

function errorFrom(data, status) {
  const validation = data?.validationErrors
    ? Object.values(data.validationErrors).join(', ')
    : ''
  return new Error(validation || data?.message || `Request failed with status ${status}`)
}

async function publicPost(path, payload) {
  const response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  const data = await parseResponse(response)
  if (!response.ok) throw errorFrom(data, response.status)
  return data
}

async function refreshSession() {
  const current = loadAuth()
  if (!current?.refreshToken) return null

  const response = await fetch(`${API_BASE}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken: current.refreshToken }),
  })

  const data = await parseResponse(response)
  if (!response.ok) {
    clearAuth()
    return null
  }

  saveAuth(data)
  return data
}

export async function apiRequest(path, options = {}, allowRefresh = true) {
  const current = loadAuth()
  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(current?.accessToken ? { Authorization: `Bearer ${current.accessToken}` } : {}),
    ...(options.headers || {}),
  }

  let response = await fetch(`${API_BASE}${path}`, { ...options, headers })

  if (response.status === 401 && allowRefresh && current?.refreshToken) {
    const refreshed = await refreshSession()
    if (refreshed) {
      response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: { ...headers, Authorization: `Bearer ${refreshed.accessToken}` },
      })
    }
  }

  const data = await parseResponse(response)
  if (!response.ok) throw errorFrom(data, response.status)
  return data
}

export async function registerAccount(username, email, password) {
  return publicPost('/auth/register', { username, email, password })
}

export async function login(username, password) {
  const data = await publicPost('/auth/login', { username, password })
  saveAuth(data)
  return data
}

export async function checkForgotPasswordUser(username) {
  return publicPost('/auth/forgot-password/check-user', { username })
}

export async function verifyForgotPassword(username, email) {
  return publicPost('/auth/forgot-password/verify', { username, email })
}

export async function resetPassword(resetToken, newPassword) {
  return publicPost('/auth/forgot-password/reset', { resetToken, newPassword })
}

export async function logout() {
  const current = loadAuth()
  if (!current) return
  try {
    await apiRequest('/auth/logout', {
      method: 'POST',
      body: JSON.stringify({ refreshToken: current.refreshToken }),
    }, false)
  } finally {
    clearAuth()
  }
}
