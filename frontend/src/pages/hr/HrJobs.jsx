// oxlint-disable react/set-state-in-effect
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout'
import StatusBadge from '../../components/StatusBadge'
import LoadingSpinner from '../../components/LoadingSpinner'
import ErrorMessage from '../../components/ErrorMessage'
import { formatDate } from '../../constants/jobs'
import * as jobService from '../../services/jobService'

const NAV_ITEMS = [
  { to: '/hr/dashboard', label: 'Dashboard' },
  { to: '/hr/jobs', label: 'Job postings' },
  { to: '/hr/jobs/new', label: 'Post a job' },
]

function HrJobs() {
  const [jobs, setJobs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)

  const load = () => {
    setLoading(true)
    setError('')
    jobService
      .listMyJobs()
      .then(setJobs)
      .catch((err) => setError(err.detail || 'Could not load your job postings.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const changeStatus = async (job, status) => {
    setBusyId(job.id)
    setError('')
    try {
      const updated = await jobService.updateJob(job.id, { status })
      setJobs((current) => current.map((item) => (item.id === updated.id ? updated : item)))
    } catch (err) {
      setError(err.detail || 'Could not update this posting.')
    } finally {
      setBusyId(null)
    }
  }

  const remove = async (job) => {
    setBusyId(job.id)
    setError('')
    try {
      await jobService.deleteJob(job.id)
      setJobs((current) => current.filter((item) => item.id !== job.id))
    } catch (err) {
      setError(err.detail || 'Could not delete this posting.')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <DashboardLayout
      title="Job postings"
      subtitle="Create, publish, and manage your open roles."
      navItems={NAV_ITEMS}
    >
      <div className="toolbar">
        <p className="muted-text">
          {jobs.length} {jobs.length === 1 ? 'posting' : 'postings'}
        </p>
        <Link to="/hr/jobs/new" className="btn btn--primary btn--sm">
          Post a job
        </Link>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError('')} />

      {loading ? (
        <LoadingSpinner label="Loading postings…" />
      ) : jobs.length === 0 ? (
        <div className="empty-state">
          <h2>No job postings yet</h2>
          <p>Create your first role to start receiving applications.</p>
          <Link to="/hr/jobs/new" className="btn btn--primary">
            Post a job
          </Link>
        </div>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th scope="col">Role</th>
                <th scope="col">Status</th>
                <th scope="col">Posted</th>
                <th scope="col" className="table__actions-col">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {jobs.map((job) => (
                <tr key={job.id}>
                  <td>
                    <p className="table__primary">{job.title}</p>
                    <p className="table__secondary">{job.location || job.company}</p>
                  </td>
                  <td>
                    <StatusBadge status={job.status} />
                  </td>
                  <td>{formatDate(job.createdAt)}</td>
                  <td>
                    <div className="table__actions">
                      <Link to={`/hr/jobs/${job.id}/applicants`} className="btn btn--ghost btn--sm">
                        Applicants
                      </Link>
                      <Link to={`/hr/jobs/${job.id}/edit`} className="btn btn--ghost btn--sm">
                        Edit
                      </Link>
                      {job.status === 'PUBLISHED' ? (
                        <button
                          type="button"
                          className="btn btn--ghost btn--sm"
                          disabled={busyId === job.id}
                          onClick={() => changeStatus(job, 'CLOSED')}
                        >
                          Close
                        </button>
                      ) : (
                        <button
                          type="button"
                          className="btn btn--ghost btn--sm"
                          disabled={busyId === job.id}
                          onClick={() => changeStatus(job, 'PUBLISHED')}
                        >
                          Publish
                        </button>
                      )}
                      <button
                        type="button"
                        className="btn btn--danger btn--sm"
                        disabled={busyId === job.id}
                        onClick={() => remove(job)}
                      >
                        Delete
                      </button>
                    </div>
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

export default HrJobs
