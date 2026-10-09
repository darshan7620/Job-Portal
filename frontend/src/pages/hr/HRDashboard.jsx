// oxlint-disable react/set-state-in-effect
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout'
import StatusBadge from '../../components/StatusBadge'
import StatusBreakdown from '../../components/StatusBreakdown'
import LoadingSpinner from '../../components/LoadingSpinner'
import ErrorMessage from '../../components/ErrorMessage'
import { formatDate } from '../../constants/jobs'
import * as dashboardService from '../../services/dashboardService'

const NAV_ITEMS = [
  { to: '/hr/dashboard', label: 'Dashboard' },
  { to: '/hr/jobs', label: 'Job postings' },
  { to: '/hr/jobs/new', label: 'Post a job' },
]

function StatCard({ label, value, tone }) {
  return (
    <article className={`stat-card${tone ? ` stat-card--${tone}` : ''}`}>
      <span className="stat-card__value">{value}</span>
      <span className="stat-card__label">{label}</span>
    </article>
  )
}

function HRDashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = () => {
    setLoading(true)
    setError('')
    dashboardService
      .getHrDashboard()
      .then(setData)
      .catch((err) => setError(err.detail || 'Could not load your dashboard.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  return (
    <DashboardLayout
      title="Recruiter dashboard"
      subtitle="Manage your postings and pipeline."
      navItems={NAV_ITEMS}
    >
      <ErrorMessage message={error} onDismiss={() => setError('')} />

      {loading ? (
        <LoadingSpinner label="Loading your dashboard…" />
      ) : data ? (
        <>
          <div className="stat-grid">
            <StatCard label="Total jobs" value={data.totalJobs} />
            <StatCard label="Published" value={data.publishedJobs} tone="success" />
            <StatCard label="Draft" value={data.draftJobs} tone="warning" />
            <StatCard label="Total applicants" value={data.totalApplicants} tone="primary" />
          </div>

          {data.totalJobs === 0 ? (
            <div className="empty-state">
              <h2>No job postings yet</h2>
              <p>Create your first role to start collecting applications.</p>
              <Link to="/hr/jobs/new" className="btn btn--primary">
                Post a job
              </Link>
            </div>
          ) : (
            <>
              <div className="panel-grid">
                <StatusBreakdown title="Jobs by status" items={data.jobsByStatus} />
                <StatusBreakdown title="Applications by status" items={data.applicationsByStatus} />
              </div>

              <section className="panel">
                <div className="panel__head">
                  <h2 className="panel__title">Applicants per job</h2>
                  <Link to="/hr/jobs" className="panel__link">
                    Manage postings
                  </Link>
                </div>
                {data.applicantsPerJob.length === 0 ? (
                  <p className="muted-text">No applications received yet.</p>
                ) : (
                  <ul className="recent-list">
                    {data.applicantsPerJob.map((job) => (
                      <li key={job.jobId} className="recent-list__item">
                        <div className="recent-list__main">
                          <Link to={`/hr/jobs/${job.jobId}/applicants`} className="recent-list__title">
                            {job.jobTitle}
                          </Link>
                          <span className="recent-list__meta">
                            {job.applicantCount} applicant{job.applicantCount === 1 ? '' : 's'}
                          </span>
                        </div>
                        <StatusBadge status={job.status} />
                      </li>
                    ))}
                  </ul>
                )}
              </section>

              <section className="panel">
                <h2 className="panel__title">Recent applications</h2>
                {data.recentApplications.length === 0 ? (
                  <p className="muted-text">No applications yet.</p>
                ) : (
                  <ul className="recent-list">
                    {data.recentApplications.map((application) => (
                      <li key={application.id} className="recent-list__item">
                        <div className="recent-list__main">
                          <span className="recent-list__title">
                            {application.candidateName || 'Candidate'}
                          </span>
                          <span className="recent-list__meta">
                            {application.jobTitle} · {formatDate(application.appliedAt)}
                          </span>
                        </div>
                        <StatusBadge status={application.status} />
                      </li>
                    ))}
                  </ul>
                )}
              </section>
            </>
          )}

          <div className="dashboard__actions">
            <Link to="/hr/jobs/new" className="btn btn--primary btn--sm">
              Post a job
            </Link>
            <Link to="/hr/jobs" className="btn btn--ghost btn--sm">
              Manage postings
            </Link>
          </div>
        </>
      ) : null}
    </DashboardLayout>
  )
}

export default HRDashboard
