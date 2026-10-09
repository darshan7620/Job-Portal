import apiClient from './apiClient'

// GET /api/jobs (and /api/jobs/search) -> PagedResponse<JobResponse>
export async function searchJobs({ keyword, location, type, page = 0, size = 9, sort = 'newest' } = {}) {
  const { data } = await apiClient.get('/jobs', {
    params: { keyword, location, type, page, size, sort },
  })
  return data
}

// GET /api/jobs/{id} -> JobResponse (published jobs only)
export async function getJob(id) {
  const { data } = await apiClient.get(`/jobs/${id}`)
  return data
}

// GET /api/hr/jobs -> JobResponse[]
export async function listMyJobs() {
  const { data } = await apiClient.get('/hr/jobs')
  return data
}

// GET /api/hr/jobs/{id} -> JobResponse (any status, owner only)
export async function getMyJob(id) {
  const { data } = await apiClient.get(`/hr/jobs/${id}`)
  return data
}

// POST /api/jobs -> JobResponse
export async function createJob(payload) {
  const { data } = await apiClient.post('/jobs', payload)
  return data
}

// PUT /api/jobs/{id} -> JobResponse
export async function updateJob(id, payload) {
  const { data } = await apiClient.put(`/jobs/${id}`, payload)
  return data
}

// DELETE /api/jobs/{id}
export async function deleteJob(id) {
  await apiClient.delete(`/jobs/${id}`)
}
