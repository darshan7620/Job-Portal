import apiClient from './apiClient'

// GET /api/candidates/dashboard -> CandidateDashboardResponse
export async function getCandidateDashboard() {
  const { data } = await apiClient.get('/candidates/dashboard')
  return data
}

// GET /api/hr/dashboard -> HrDashboardResponse
export async function getHrDashboard() {
  const { data } = await apiClient.get('/hr/dashboard')
  return data
}
