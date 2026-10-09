import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import ErrorMessage from '../components/ErrorMessage'
import { useAuth } from '../context/useAuth'
import { ROLES, dashboardPathForRole } from '../routes/roles'

const COPY = {
  candidate: {
    eyebrow: 'Candidate access',
    title: 'Sign in to your job search',
    lead: 'Track applications and pick up where you left off.',
    submit: 'Sign in as candidate',
    expectedRole: ROLES.CANDIDATE,
    otherHint: 'Are you a recruiter?',
    otherLink: '/hr/login',
    otherLabel: 'Use the HR login',
  },
  hr: {
    eyebrow: 'Recruiter access',
    title: 'Sign in to your hiring desk',
    lead: 'Manage postings, applicants, and your pipeline.',
    submit: 'Sign in as recruiter',
    expectedRole: ROLES.HR,
    otherHint: 'Looking for a job?',
    otherLink: '/login',
    otherLabel: 'Use the candidate login',
  },
}

function LoginPage({ variant = 'candidate' }) {
  const copy = COPY[variant] || COPY.candidate
  const { user, initializing, login, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [username, setUsername] = useState(location.state?.username || '')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const registered = Boolean(location.state?.registered)

  if (!initializing && user) {
    return <Navigate to={dashboardPathForRole(user.role)} replace />
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)

    try {
      const authenticated = await login({ username: username.trim(), password })
      if (authenticated.role !== copy.expectedRole) {
        logout()
        setError(
          variant === 'hr'
            ? 'That account is not a recruiter account. Please use the candidate login.'
            : 'That account is not a candidate account. Please use the HR login.',
        )
        return
      }
      const redirectTo = location.state?.from || dashboardPathForRole(authenticated.role)
      navigate(redirectTo, { replace: true })
    } catch (apiError) {
      setError(apiError.detail || 'Unable to sign in. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="auth">
      <div className="auth__card">
        <p className="auth__eyebrow">{copy.eyebrow}</p>
        <h1 className="auth__title">{copy.title}</h1>
        <p className="auth__lead">{copy.lead}</p>

        {registered && !error ? (
          <div className="alert alert--success" role="status">
            Account created. Sign in with your new candidate credentials.
          </div>
        ) : null}

        <ErrorMessage message={error} onDismiss={() => setError('')} />

        <form className="form" onSubmit={handleSubmit} noValidate>
          <div className="form__group">
            <label htmlFor="username">Username or email</label>
            <input
              id="username"
              name="username"
              type="text"
              autoComplete="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
          </div>

          <div className="form__group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn btn--primary btn--block" disabled={submitting}>
            {submitting ? 'Signing in…' : copy.submit}
          </button>
        </form>

        <p className="auth__switch">
          {copy.otherHint} <Link to={copy.otherLink}>{copy.otherLabel}</Link>
        </p>

        {variant === 'candidate' ? (
          <p className="auth__switch">
            New here? <Link to="/register">Create a candidate account</Link>
          </p>
        ) : (
          <p className="auth__switch auth__switch--muted">
            Recruiter accounts are provisioned by an administrator.
          </p>
        )}
      </div>
    </section>
  )
}

export default LoginPage
