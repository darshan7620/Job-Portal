// oxlint-disable react/set-state-in-effect
import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import LoadingSpinner from '../../components/LoadingSpinner'
import ErrorMessage from '../../components/ErrorMessage'
import { useAuth } from '../../context/useAuth'
import { ROLES } from '../../routes/roles'
import { employmentTypeLabel, formatDate, formatSalary } from '../../constants/jobs'
import * as jobService from '../../services/jobService'
import * as applicationService from '../../services/applicationService'

function JobDetailPage() {
  const { jobId } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()

  const [job, setJob] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [coverLetter, setCoverLetter] = useState('')
  const [applying, setApplying] = useState(false)
  const [applied, setApplied] = useState(false)
  const [applyError, setApplyError] = useState('')

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    jobService
      .getJob(jobId)
      .then((response) => {
        if (active) setJob(response)
      })
      .catch((err) => {
        if (active) setError(err.detail || 'Could not load this job.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [jobId])

  const handleApply = async (event) => {
    event.preventDefault()
    setApplying(true)
    setApplyError('')
    try {
      await applicationService.applyToJob(job.id, { coverLetter })
      setApplied(true)
      setCoverLetter('')
    } catch (err) {
      setApplyError(err.detail || 'Could not submit your application.')
    } finally {
      setApplying(false)
    }
  }

  if (loading) {
    return <LoadingSpinner full label="Loading job…" />
  }

  if (error || !job) {
    return (
      <div className="page container">
        <ErrorMessage message={error || 'Job not found.'} />
        <Link to="/jobs" className="btn btn--ghost">
          Back to jobs
        </Link>
      </div>
    )
  }

  const salary = formatSalary(job.salaryMin, job.salaryMax)

  return (
    <div className="page container">
      <button type="button" className="link-back" onClick={() => navigate(-1)}>
        ← Back
      </button>

      <div className="job-detail">
        <div className="job-detail__main">
          <header className="job-detail__head">
            <div className="job-detail__logo" aria-hidden="true">
              {job.company ? job.company.charAt(0) : 'J'}
            </div>
            <div>
              <h1>{job.title}</h1>
              <p className="job-detail__company">{job.company}</p>
            </div>
          </header>

          <dl className="meta-list">
            <div>
              <dt>Location</dt>
              <dd>{job.location || 'Not specified'}</dd>
            </div>
            <div>
              <dt>Employment type</dt>
              <dd>{job.employmentType ? employmentTypeLabel(job.employmentType) : 'Not specified'}</dd>
            </div>
            <div>
              <dt>Salary</dt>
              <dd>{salary || 'Not disclosed'}</dd>
            </div>
            <div>
              <dt>Posted</dt>
              <dd>{formatDate(job.createdAt) || '—'}</dd>
            </div>
            {job.expiresAt ? (
              <div>
                <dt>Apply before</dt>
                <dd>{formatDate(job.expiresAt)}</dd>
              </div>
            ) : null}
          </dl>

          <section className="job-detail__section">
            <h2>About the role</h2>
            <p className="pre-line">{job.description}</p>
          </section>

          {job.requiredSkills ? (
            <section className="job-detail__section">
              <h2>Required skills</h2>
              <div className="tag-row">
                {job.requiredSkills.split(',').map((skill) => (
                  <span className="chip" key={skill.trim()}>
                    {skill.trim()}
                  </span>
                ))}
              </div>
            </section>
          ) : null}
        </div>

        <aside className="job-detail__aside">
          {applied ? (
            <div className="alert alert--success" role="status">
              Your application was submitted.
            </div>
          ) : null}

          {!user ? (
            <div className="apply-card">
              <h2>Interested?</h2>
              <p>Sign in as a candidate to apply for this role.</p>
              <Link to="/login" state={{ from: `/jobs/${job.id}` }} className="btn btn--primary btn--block">
                Candidate login
              </Link>
              <Link to="/register" className="btn btn--ghost btn--block">
                Create an account
              </Link>
            </div>
          ) : user.role === ROLES.CANDIDATE ? (
            <form className="apply-card" onSubmit={handleApply}>
              <h2>Apply for this role</h2>
              <ErrorMessage message={applyError} onDismiss={() => setApplyError('')} />
              <div className="form__group">
                <label htmlFor="cover-letter">Cover letter (optional)</label>
                <textarea
                  id="cover-letter"
                  rows="6"
                  maxLength="2000"
                  placeholder="Tell the team why you're a great fit…"
                  value={coverLetter}
                  onChange={(event) => setCoverLetter(event.target.value)}
                />
              </div>
              <button type="submit" className="btn btn--primary btn--block" disabled={applying}>
                {applying ? 'Submitting…' : 'Submit application'}
              </button>
            </form>
          ) : (
            <div className="apply-card">
              <h2>Recruiter account</h2>
              <p>You're signed in as a recruiter. Manage this posting from your dashboard.</p>
              <Link to="/hr/jobs" className="btn btn--ghost btn--block">
                Manage job postings
              </Link>
            </div>
          )}
        </aside>
      </div>
    </div>
  )
}

export default JobDetailPage
