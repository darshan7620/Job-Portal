import { createContext } from 'react'

// Kept in its own module so AuthContext.jsx only exports components (which keeps
// React Fast Refresh happy) and both the provider and useAuth share one instance.
export const AuthContext = createContext(null)
