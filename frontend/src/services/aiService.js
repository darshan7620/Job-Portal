import apiClient from './apiClient'

// POST /api/ai/match -> MatchResponse
// The score returned is an approximate AI estimate, not a hiring probability.
export async function matchJob({ skills, jobDescription }) {
  const { data } = await apiClient.post('/ai/match', { skills, jobDescription })
  return data
}
