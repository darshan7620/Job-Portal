export const ROLES = {
  CANDIDATE: 'CANDIDATE',
  HR: 'HR',
  ADMIN: 'ADMIN',
}

export function dashboardPathForRole(role) {
  if (role === ROLES.HR) return '/hr/dashboard'
  if (role === ROLES.CANDIDATE) return '/candidate/dashboard'
  return '/'
}

export function loginPathForRole(role) {
  return role === ROLES.HR ? '/hr/login' : '/login'
}
