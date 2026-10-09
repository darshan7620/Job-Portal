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
  { to: '/candidate/dashboard', label: 'Dashboard' },
  { to: '/jobs', label: 'Browse jobs' },
  { to: '/candidate/applications', label: 'My applications' },
  { to: '/candidate/ai-matcher', label: 'AI matcher' },
]

function StatCard({ label, value, tone }) {
  return (
    <article className={`stat-card${tone ? ` stat-card--${tone}` : ''}`}>
      <span className="stat-card__value">{value}</span>
      <span className="stat-card__label">{label}</span>
    </article>
  )
}

function ApplicationList({ items, dateField, emptyText }) {
  if (items.length === 0) {
    return <p className="muted-text">{emptyText}</p>
  }
  return (
    <ul className="recent-list">
      {items.map((application) => (
        <li key={application.id} className="recent-list__item">
          <div className="recent-list__main">
            <Link to={`/jobs/${application.jobId}`} className="recent-list__title">
              {application.jobTitle}
            </Link>
            <span className="recent-list__meta">{formatDate(application[dateField])}</span>
          </div>
          <StatusBadge status={application.status} />
        </li>
      ))}
    </ul>
  )
}

function CandidateDashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = () => {
    setLoading(true)
    setError('')
    dashboardService
      .getCandidateDashboard()
      .then(setData)
      .catch((err) => setError(err.detail || 'Could not load your dashboard.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  return (
    <DashboardLayout
      title="Candidate dashboard"
      subtitle="Your job search at a glance."
      navItems={NAV_ITEMS}
    >
      <ErrorMessage message={error} onDismiss={() => setError('')} />

      {loading ? (
        <LoadingSpinner label="Loading your dashboard…" />
      ) : data ? (
        <>
          <div className="stat-grid">
            <StatCard label="Total applications" value={data.totalApplications} />
            <StatCard label="Under review" value={data.underReviewApplications} tone="warning" />
            <StatCard label="Shortlisted" value={data.shortlistedApplications} tone="primary" />
            <StatCard label="Offers" value={data.offers} tone="success" />
          </div>

          {data.totalApplications === 0 ? (
            <div className="empty-state">
              <h2>No applications yet</h2>
              <p>Browse open roles and submit your first application to see your stats here.</p>
              <Link to="/jobs" className="btn btn--primary">
                Browse jobs
              </Link>
            </div>
          ) : (
            <>
              <div className="panel-grid">
                <StatusBreakdown title="Applications by status" items={data.applicationsByStatus} />
                <section className="panel">
                  <h2 className="panel__title">Recent applications</h2>
                  <ApplicationList
                    items={data.recentApplications}
                    dateField="appliedAt"
                    emptyText="No recent applications."
                  />
                </section>
              </div>

              <section className="panel">
                <h2 className="panel__title">Recently updated</h2>
                <ApplicationList
                  items={data.recentlyUpdatedApplications}
                  dateField="updatedAt"
                  emptyText="Nothing updated yet."
                />
              </section>
            </>
          )}

          <div className="dashboard__actions">
            <Link to="/jobs" className="btn btn--primary btn--sm">
              Find roles
            </Link>
            <Link to="/candidate/applications" className="btn btn--ghost btn--sm">
              View all applications
            </Link>
          </div>
        </>
      ) : null}
    </DashboardLayout>
  )
}

export default CandidateDashboard
