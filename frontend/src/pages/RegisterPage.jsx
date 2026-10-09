import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import ErrorMessage from '../components/ErrorMessage'
import { useAuth } from '../context/useAuth'
import { toFieldErrors } from '../services/errorUtils'
import { dashboardPathForRole } from '../routes/roles'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validate(values) {
  const errors = {}
  if (!values.fullName.trim()) errors.fullName = 'Full name is required.'
  if (!values.email.trim()) {
    errors.email = 'Email is required.'
  } else if (!EMAIL_PATTERN.test(values.email.trim())) {
    errors.email = 'Enter a valid email address.'
  }
  if (!values.username.trim()) {
    errors.username = 'Username is required.'
  } else if (values.username.trim().length < 3 || values.username.trim().length > 50) {
    errors.username = 'Username must be 3-50 characters.'
  }
  if (!values.password) {
    errors.password = 'Password is required.'
  } else if (values.password.length < 8 || values.password.length > 100) {
    errors.password = 'Password must be 8-100 characters.'
  }
  if (values.confirmPassword !== values.password) {
    errors.confirmPassword = 'Passwords do not match.'
  }
  return errors
}

function RegisterPage() {
  const { user, initializing, register } = useAuth()
  const navigate = useNavigate()

  const [values, setValues] = useState({
    fullName: '',
    email: '',
    username: '',
    phone: '',
    password: '',
    confirmPassword: '',
  })
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (!initializing && user) {
    return <Navigate to={dashboardPathForRole(user.role)} replace />
  }

  const handleChange = (event) => {
    const { name, value } = event.target
    setValues((prev) => ({ ...prev, [name]: value }))
    setFieldErrors((prev) => {
      if (!prev[name]) return prev
      const next = { ...prev }
      delete next[name]
      return next
    })
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')

    const validationErrors = validate(values)
    if (Object.keys(validationErrors).length > 0) {
      setFieldErrors(validationErrors)
      return
    }

    setSubmitting(true)
    try {
      await register({
        fullName: values.fullName.trim(),
        email: values.email.trim(),
        username: values.username.trim(),
        password: values.password,
        phone: values.phone.trim() || undefined,
      })
      navigate('/login', {
        replace: true,
        state: { registered: true, username: values.username.trim() },
      })
    } catch (apiError) {
      setFieldErrors(toFieldErrors(apiError.detail))
      setError(apiError.detail || 'Unable to create your account.')
    } finally {
      setSubmitting(false)
    }
  }

  const renderError = (field) =>
    fieldErrors[field] ? <p className="form__error">{fieldErrors[field]}</p> : null

  return (
    <section className="auth">
      <div className="auth__card auth__card--wide">
        <p className="auth__eyebrow">Candidate registration</p>
        <h1 className="auth__title">Create your candidate account</h1>
        <p className="auth__lead">
          Public registration is for job seekers only. Recruiter accounts are issued by an
          administrator.
        </p>

        <ErrorMessage message={error} onDismiss={() => setError('')} />

        <form className="form form--grid" onSubmit={handleSubmit} noValidate>
          <div className="form__group form__group--full">
            <label htmlFor="fullName">Full name</label>
            <input
              id="fullName"
              name="fullName"
              type="text"
              autoComplete="name"
              value={values.fullName}
              onChange={handleChange}
              aria-invalid={Boolean(fieldErrors.fullName)}
              required
            />
            {renderError('fullName')}
          </div>

          <div className="form__group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              value={values.email}
              onChange={handleChange}
              aria-invalid={Boolean(fieldErrors.email)}
              required
            />
            {renderError('email')}
          </div>

          <div className="form__group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              name="username"
              type="text"
              autoComplete="username"
              value={values.username}
              onChange={handleChange}
              aria-invalid={Boolean(fieldErrors.username)}
              required
            />
            {renderError('username')}
          </div>

          <div className="form__group">
            <label htmlFor="phone">Phone (optional)</label>
            <input
              id="phone"
              name="phone"
              type="tel"
              autoComplete="tel"
              value={values.phone}
              onChange={handleChange}
            />
          </div>

          <div className="form__group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="new-password"
              value={values.password}
              onChange={handleChange}
              aria-invalid={Boolean(fieldErrors.password)}
              required
            />
            {renderError('password')}
          </div>

          <div className="form__group">
            <label htmlFor="confirmPassword">Confirm password</label>
            <input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              autoComplete="new-password"
              value={values.confirmPassword}
              onChange={handleChange}
              aria-invalid={Boolean(fieldErrors.confirmPassword)}
              required
            />
            {renderError('confirmPassword')}
          </div>

          <div className="form__actions form__group--full">
            <button type="submit" className="btn btn--primary btn--block" disabled={submitting}>
              {submitting ? 'Creating account…' : 'Create account'}
            </button>
          </div>
        </form>

        <p className="auth__switch">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </section>
  )
}

export default RegisterPage
