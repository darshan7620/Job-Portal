// JWT persistence for the Smart Job Tracker frontend.
//
// The backend is fully stateless: every request is authorised independently from
// the signed token sent in the Authorization header (there is no server session).
// To keep the user signed in across page reloads we persist the token in
// localStorage.
//
// SECURITY NOTE: localStorage is readable by any JavaScript running on the page,
// so it does NOT protect the token from XSS. This is an accepted trade-off for a
// portfolio app; a hardened production deployment would use an HttpOnly, Secure,
// SameSite cookie instead. Regardless of storage, the backend validates the token
// signature/expiry and the user's role on every request, so an attacker cannot
// mint access by editing client-side state.
const STORAGE_KEY = 'sjt.auth.token'

let token = readInitialToken()

function readInitialToken() {
  try {
    return window.localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}

export function getToken() {
  return token
}

export function setToken(nextToken) {
  token = nextToken || null
  try {
    if (token) {
      window.localStorage.setItem(STORAGE_KEY, token)
    } else {
      window.localStorage.removeItem(STORAGE_KEY)
    }
  } catch {
    // Storage may be unavailable (e.g. private browsing); fall back to memory.
  }
}

export function clearToken() {
  setToken(null)
}
