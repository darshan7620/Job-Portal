import { useCallback, useEffect, useMemo, useState } from 'react'
import { AuthContext } from './authContextObject'
import * as authService from '../services/authService'
import { clearToken, getToken, setToken } from '../services/tokenStore'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  // Only block the UI while we try to restore a session from a persisted token.
  // When no token is stored this is already false, so nothing synchronously
  // needs to change during the initial effect.
  const [initializing, setInitializing] = useState(() => Boolean(getToken()))

  useEffect(() => {
    if (!getToken()) {
      return undefined
    }

    let active = true
    authService
      .fetchCurrentUser()
      .then((current) => {
        if (active) setUser(current)
      })
      .catch(() => {
        if (active) {
          clearToken()
          setUser(null)
        }
      })
      .finally(() => {
        if (active) setInitializing(false)
      })

    return () => {
      active = false
    }
  }, [])

  // Resolves with the authenticated user so the caller can route by role.
  const login = useCallback(async (credentials) => {
    const data = await authService.login(credentials)
    setToken(data.token)
    setUser(data.user)
    return data.user
  }, [])

  const register = useCallback(async (payload) => {
    return authService.register(payload)
  }, [])

  const logout = useCallback(() => {
    clearToken()
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({
      user,
      initializing,
      isAuthenticated: Boolean(user),
      login,
      register,
      logout,
    }),
    [user, initializing, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
