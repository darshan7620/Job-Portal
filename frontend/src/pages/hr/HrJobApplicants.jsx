// oxlint-disable react/set-state-in-effect
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout'
import StatusBadge from '../../components/StatusBadge'
import LoadingSpinner from '../../components/LoadingSpinner'
import ErrorMessage from '../../components/ErrorMessage'
import { formatDate, nextApplicationStatuses, statusLabel } from '../../constants/jobs'
import * as jobService from '../../services/jobService'
import * as applicationService from '../../services/applicationService'

const NAV_ITEMS = [
  { to: '/hr/dashboard', label: 'Dashboard' },
  { to: '/hr/jobs', label: 'Job postings' },
  { to: '/hr/jobs/new', label: 'Post a job' },
]

function HrJobApplicants() {
  const { jobId } = useParams()

  const [job, setJob] = useState(null)
  const [applications, setApplications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    Promise.all([jobService.getMyJob(jobId), applicationService.listJobApplications(jobId)])
      .then(([jobData, applicationData]) => {
        if (!active) return
        setJob(jobData)
        setApplications(applicationData)
      })
      .catch((err) => {
        if (active) setError(err.detail || 'Could not load applicants.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [jobId])

  const changeStatus = async (application, status) => {
    setBusyId(application.id)
    setError('')
    try {
      const updated = await applicationService.updateApplicationStatus(application.id, status)
      setApplications((current) => current.map((item) => (item.id === updated.id ? updated : item)))
    } catch (err) {
      setError(err.detail || 'Could not update this application.')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <DashboardLayout
      title={job ? `Applicants — ${job.title}` : 'Applicants'}
      subtitle="Review candidates and move them through your pipeline."
      navItems={NAV_ITEMS}
    >
      <div className="toolbar">
        <Link to="/hr/jobs" className="btn btn--ghost btn--sm">
          ← All postings
        </Link>
        {job ? <StatusBadge status={job.status} /> : null}
      </div>

      <ErrorMessage message={error} onDismiss={() => setError('')} />

      {loading ? (
        <LoadingSpinner label="Loading applicants…" />
      ) : applications.length === 0 ? (
        <div className="empty-state">
          <h2>No applicants yet</h2>
          <p>Once candidates apply, they'll appear here.</p>
        </div>
      ) : (
        <div className="applicant-list">
          {applications.map((application) => {
            const options = nextApplicationStatuses(application.status)
            return (
              <article className="applicant-card" key={application.id}>
                <div className="applicant-card__head">
                  <div>
                    <h2 className="applicant-card__name">{application.candidateName}</h2>
                    <p className="table__secondary">Applied {formatDate(application.appliedAt)}</p>
                  </div>
                  <StatusBadge status={application.status} />
                </div>

                {application.coverLetter ? (
                  <p className="applicant-card__cover">{application.coverLetter}</p>
                ) : null}

                <div className="applicant-card__actions">
                  {options.length === 0 ? (
                    <span className="muted-text">No further actions — final state.</span>
                  ) : (
                    <label className="inline-field">
                      <span>Move to</span>
                      <select
                        value=""
                        disabled={busyId === application.id}
                        onChange={(event) => {
                          if (event.target.value) changeStatus(application, event.target.value)
                        }}
                      >
                        <option value="">Select status…</option>
                        {options.map((status) => (
                          <option key={status} value={status}>
                            {statusLabel(status)}
                          </option>
                        ))}
                      </select>
                    </label>
                  )}
                </div>
              </article>
            )
          })}
        </div>
      )}
    </DashboardLayout>
  )
}

export default HrJobApplicants
