import axios from 'axios'
import { getToken } from './tokenStore'

const baseURL = import.meta.env.VITE_API_URL || '/api'

const apiClient = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

// Attach the bearer token when one is available. Importing only tokenStore here
// (never AuthContext) keeps this module free of circular dependencies.
apiClient.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Normalise every failure into a single shape: an Error with `status` and
// `detail` (the backend's {"detail": "..."} message when present).
apiClient.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(toApiError(error)),
)

function extractDetail(data) {
  if (!data) return null
  if (typeof data === 'string') return data
  if (typeof data.detail === 'string') return data.detail
  if (typeof data.message === 'string') return data.message
  return null
}

function defaultDetail(status) {
  if (status === 401) return 'Incorrect username or password.'
  if (status === 403) return 'You are not allowed to perform this action.'
  if (status === 404) return 'The requested resource was not found.'
  return 'Something went wrong. Please try again.'
}

export function toApiError(error) {
  const apiError = new Error()
  apiError.name = 'ApiError'
  if (error.response) {
    apiError.status = error.response.status
    apiError.detail = extractDetail(error.response.data) || defaultDetail(error.response.status)
  } else if (error.request) {
    apiError.status = 0
    apiError.detail = 'Unable to reach the server. Please check that the backend is running.'
  } else {
    apiError.status = 0
    apiError.detail = error.message || 'Unexpected error.'
  }
  apiError.message = apiError.detail
  return apiError
}

export default apiClient
