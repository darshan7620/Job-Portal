// oxlint-disable react/set-state-in-effect
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout'
import StatusBadge from '../../components/StatusBadge'
import LoadingSpinner from '../../components/LoadingSpinner'
import ErrorMessage from '../../components/ErrorMessage'
import { formatDate, isTerminalApplicationStatus } from '../../constants/jobs'
import * as applicationService from '../../services/applicationService'

const NAV_ITEMS = [
  { to: '/candidate/dashboard', label: 'Dashboard' },
  { to: '/jobs', label: 'Browse jobs' },
  { to: '/candidate/applications', label: 'My applications' },
  { to: '/candidate/ai-matcher', label: 'AI matcher' },
]

function CandidateApplications() {
  const [applications, setApplications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)

  const load = () => {
    setLoading(true)
    setError('')
    applicationService
      .listMyApplications()
      .then(setApplications)
      .catch((err) => setError(err.detail || 'Could not load your applications.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const handleWithdraw = async (application) => {
    setBusyId(application.id)
    setError('')
    try {
      await applicationService.withdrawApplication(application.id)
      setApplications((current) => current.filter((item) => item.id !== application.id))
    } catch (err) {
      setError(err.detail || 'Could not withdraw this application.')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <DashboardLayout
      title="My applications"
      subtitle="Track the status of every role you've applied to."
      navItems={NAV_ITEMS}
    >
      <ErrorMessage message={error} onDismiss={() => setError('')} />

      {loading ? (
        <LoadingSpinner label="Loading applications…" />
      ) : applications.length === 0 ? (
        <div className="empty-state">
          <h2>No applications yet</h2>
          <p>Browse open roles and submit your first application.</p>
          <Link to="/jobs" className="btn btn--primary">
            Browse jobs
          </Link>
        </div>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th scope="col">Role</th>
                <th scope="col">Applied</th>
                <th scope="col">Status</th>
                <th scope="col" className="table__actions-col">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {applications.map((application) => (
                <tr key={application.id}>
                  <td>
                    <Link to={`/jobs/${application.jobId}`}>{application.jobTitle}</Link>
                  </td>
                  <td>{formatDate(application.appliedAt)}</td>
                  <td>
                    <StatusBadge status={application.status} />
                  </td>
                  <td>
                    {isTerminalApplicationStatus(application.status) ? (
                      <span className="muted-text">Closed</span>
                    ) : (
                      <button
                        type="button"
                        className="btn btn--ghost btn--sm"
                        disabled={busyId === application.id}
                        onClick={() => handleWithdraw(application)}
                      >
                        {busyId === application.id ? 'Withdrawing…' : 'Withdraw'}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </DashboardLayout>
  )
}

export default CandidateApplications
