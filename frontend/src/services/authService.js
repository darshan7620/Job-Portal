import apiClient from './apiClient'

// POST /api/auth/login -> { token, tokenType, expiresIn, user }
export async function login(credentials) {
  const { data } = await apiClient.post('/auth/login', credentials)
  return data
}

// POST /api/auth/register -> UserResponse (role is always CANDIDATE)
export async function register(payload) {
  const { data } = await apiClient.post('/auth/register', payload)
  return data
}

// GET /api/auth/me -> UserResponse
export async function fetchCurrentUser() {
  const { data } = await apiClient.get('/auth/me')
  return data
}
