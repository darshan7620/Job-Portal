import apiClient from './apiClient'

// POST /api/jobs/{jobId}/applications -> ApplicationResponse
export async function applyToJob(jobId, payload = {}) {
  const { data } = await apiClient.post(`/jobs/${jobId}/applications`, payload)
  return data
}

// GET /api/applications/{id} -> ApplicationResponse (candidate owner or job's HR)
export async function getApplication(id) {
  const { data } = await apiClient.get(`/applications/${id}`)
  return data
}

// DELETE /api/applications/{id}
export async function withdrawApplication(id) {
  await apiClient.delete(`/applications/${id}`)
}

// GET /api/candidates/applications -> ApplicationResponse[]
export async function listMyApplications() {
  const { data } = await apiClient.get('/candidates/applications')
  return data
}

// GET /api/hr/jobs/{jobId}/applications -> ApplicationResponse[]
export async function listJobApplications(jobId) {
  const { data } = await apiClient.get(`/hr/jobs/${jobId}/applications`)
  return data
}

// PATCH /api/hr/applications/{id}/status -> ApplicationResponse
export async function updateApplicationStatus(id, status) {
  const { data } = await apiClient.patch(`/hr/applications/${id}/status`, { status })
  return data
}
