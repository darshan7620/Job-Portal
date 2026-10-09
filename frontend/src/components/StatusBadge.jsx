import { statusLabel } from '../constants/jobs'

const TONES = {
  DRAFT: 'muted',
  PUBLISHED: 'success',
  CLOSED: 'muted',
  APPLIED: 'info',
  UNDER_REVIEW: 'warning',
  SHORTLISTED: 'primary',
  REJECTED: 'danger',
  HIRED: 'success',
}

function StatusBadge({ status }) {
  if (!status) return null
  const tone = TONES[status] || 'muted'
  return <span className={`badge badge--${tone}`}>{statusLabel(status)}</span>
}

export default StatusBadge
