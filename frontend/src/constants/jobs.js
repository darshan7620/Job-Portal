export const EMPLOYMENT_TYPES = [
  { value: 'FULL_TIME', label: 'Full-time' },
  { value: 'PART_TIME', label: 'Part-time' },
  { value: 'CONTRACT', label: 'Contract' },
  { value: 'INTERNSHIP', label: 'Internship' },
  { value: 'REMOTE', label: 'Remote' },
]

export const JOB_STATUSES = [
  { value: 'DRAFT', label: 'Draft' },
  { value: 'PUBLISHED', label: 'Published' },
  { value: 'CLOSED', label: 'Closed' },
]

// Mirrors the backend's ApplicationStatus enum. There is no INTERVIEW value in
// the agreed data model; SHORTLISTED represents the interview stage for now.
export const APPLICATION_STATUSES = [
  { value: 'APPLIED', label: 'Applied' },
  { value: 'UNDER_REVIEW', label: 'Under review' },
  { value: 'SHORTLISTED', label: 'Shortlisted' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'HIRED', label: 'Hired' },
]

export const TERMINAL_APPLICATION_STATUSES = ['REJECTED', 'HIRED']

// Allowed transitions mirror JobApplicationService.ALLOWED_TRANSITIONS so the
// UI only offers moves the backend accepts.
const APPLICATION_TRANSITIONS = {
  APPLIED: ['UNDER_REVIEW', 'SHORTLISTED', 'REJECTED'],
  UNDER_REVIEW: ['SHORTLISTED', 'REJECTED', 'HIRED'],
  SHORTLISTED: ['UNDER_REVIEW', 'REJECTED', 'HIRED'],
  REJECTED: [],
  HIRED: [],
}

export function nextApplicationStatuses(status) {
  return APPLICATION_TRANSITIONS[status] || []
}

export function isTerminalApplicationStatus(status) {
  return TERMINAL_APPLICATION_STATUSES.includes(status)
}

export function employmentTypeLabel(value) {
  const match = EMPLOYMENT_TYPES.find((item) => item.value === value)
  return match ? match.label : value || ''
}

export function statusLabel(value) {
  const match = [...JOB_STATUSES, ...APPLICATION_STATUSES].find((item) => item.value === value)
  if (match) return match.label
  if (!value) return ''
  return value
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

export function formatSalary(min, max) {
  if (!min && !max) return ''
  const format = (value) => (value >= 1000 ? `${Math.round(value / 1000)}k` : value)
  if (min && max) return `$${format(min)} – $${format(max)}`
  if (min) return `From $${format(min)}`
  return `Up to $${format(max)}`
}

export function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
}
