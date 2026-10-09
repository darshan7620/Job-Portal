// oxlint-disable react/set-state-in-effect
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout'
import LoadingSpinner from '../../components/LoadingSpinner'
import ErrorMessage from '../../components/ErrorMessage'
import { EMPLOYMENT_TYPES, JOB_STATUSES } from '../../constants/jobs'
import { toFieldErrors } from '../../services/errorUtils'
import * as jobService from '../../services/jobService'

const NAV_ITEMS = [
  { to: '/hr/dashboard', label: 'Dashboard' },
  { to: '/hr/jobs', label: 'Job postings' },
  { to: '/hr/jobs/new', label: 'Post a job' },
]

const EMPTY_FORM = {
  title: '',
  company: '',
  location: '',
  employmentType: '',
  salaryMin: '',
  salaryMax: '',
  expiresAt: '',
  status: 'DRAFT',
  requiredSkills: '',
  description: '',
}

function HrJobForm() {
  const { jobId } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(jobId)

  const [form, setForm] = useState(EMPTY_FORM)
  const [loading, setLoading] = useState(isEdit)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})

  useEffect(() => {
    if (!isEdit) return undefined
    let active = true
    setLoading(true)
    jobService
      .getMyJob(jobId)
      .then((job) => {
        if (!active) return
        setForm({
          title: job.title || '',
          company: job.company || '',
          location: job.location || '',
          employmentType: job.employmentType || '',
          salaryMin: job.salaryMin ?? '',
          salaryMax: job.salaryMax ?? '',
          expiresAt: job.expiresAt || '',
          status: job.status || 'DRAFT',
          requiredSkills: job.requiredSkills || '',
          description: job.description || '',
        })
      })
      .catch((err) => {
        if (active) setError(err.detail || 'Could not load this posting.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [isEdit, jobId])

  const update = (key) => (event) => setForm((current) => ({ ...current, [key]: event.target.value }))

  const buildPayload = () => {
    const payload = {
      title: form.title.trim(),
      company: form.company.trim(),
      location: form.location.trim(),
      employmentType: form.employmentType,
      status: form.status,
      requiredSkills: form.requiredSkills.trim(),
      description: form.description,
    }
    if (form.salaryMin !== '') payload.salaryMin = Number(form.salaryMin)
    if (form.salaryMax !== '') payload.salaryMax = Number(form.salaryMax)
    if (form.expiresAt) payload.expiresAt = form.expiresAt
    return payload
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    setFieldErrors({})
    try {
      if (isEdit) {
        await jobService.updateJob(jobId, buildPayload())
      } else {
        await jobService.createJob(buildPayload())
      }
      navigate('/hr/jobs')
    } catch (err) {
      setError(err.detail || 'Could not save this posting.')
      setFieldErrors(toFieldErrors(err.detail))
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <DashboardLayout title="Edit job" navItems={NAV_ITEMS}>
        <LoadingSpinner label="Loading posting…" />
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout
      title={isEdit ? 'Edit job posting' : 'Post a job'}
      subtitle={isEdit ? 'Update the details of your posting.' : 'Publish a new role for candidates.'}
      navItems={NAV_ITEMS}
    >
      <ErrorMessage message={error} onDismiss={() => setError('')} />

      <form className="form form--grid card" onSubmit={handleSubmit}>
        <div className="form__group form__group--full">
          <label htmlFor="title">Job title</label>
          <input
            id="title"
            value={form.title}
            onChange={update('title')}
            aria-invalid={Boolean(fieldErrors.title)}
            required
          />
          {fieldErrors.title ? <p className="form__error">{fieldErrors.title}</p> : null}
        </div>

        <div className="form__group">
          <label htmlFor="company">Company</label>
          <input
            id="company"
            value={form.company}
            onChange={update('company')}
            aria-invalid={Boolean(fieldErrors.company)}
            required
          />
          {fieldErrors.company ? <p className="form__error">{fieldErrors.company}</p> : null}
        </div>

        <div className="form__group">
          <label htmlFor="location">Location</label>
          <input id="location" value={form.location} onChange={update('location')} />
        </div>

        <div className="form__group">
          <label htmlFor="employmentType">Employment type</label>
          <select id="employmentType" value={form.employmentType} onChange={update('employmentType')}>
            <option value="">Not specified</option>
            {EMPLOYMENT_TYPES.map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>
        </div>

        <div className="form__group">
          <label htmlFor="status">Status</label>
          <select id="status" value={form.status} onChange={update('status')}>
            {JOB_STATUSES.map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>
        </div>

        <div className="form__group">
          <label htmlFor="salaryMin">Salary min</label>
          <input
            id="salaryMin"
            type="number"
            min="0"
            value={form.salaryMin}
            onChange={update('salaryMin')}
            aria-invalid={Boolean(fieldErrors.salaryMin)}
          />
          {fieldErrors.salaryMin ? <p className="form__error">{fieldErrors.salaryMin}</p> : null}
        </div>

        <div className="form__group">
          <label htmlFor="salaryMax">Salary max</label>
          <input
            id="salaryMax"
            type="number"
            min="0"
            value={form.salaryMax}
            onChange={update('salaryMax')}
            aria-invalid={Boolean(fieldErrors.salaryMax)}
          />
          {fieldErrors.salaryMax ? <p className="form__error">{fieldErrors.salaryMax}</p> : null}
        </div>

        <div className="form__group">
          <label htmlFor="expiresAt">Application deadline</label>
          <input id="expiresAt" type="date" value={form.expiresAt} onChange={update('expiresAt')} />
          {fieldErrors.expiresAt ? <p className="form__error">{fieldErrors.expiresAt}</p> : null}
        </div>

        <div className="form__group form__group--full">
          <label htmlFor="requiredSkills">Required skills</label>
          <input
            id="requiredSkills"
            placeholder="Comma separated, e.g. Java, Spring, SQL"
            value={form.requiredSkills}
            onChange={update('requiredSkills')}
          />
        </div>

        <div className="form__group form__group--full">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            rows="8"
            value={form.description}
            onChange={update('description')}
            aria-invalid={Boolean(fieldErrors.description)}
            required
          />
          {fieldErrors.description ? <p className="form__error">{fieldErrors.description}</p> : null}
        </div>

        <div className="form__actions form__group--full">
          <button type="submit" className="btn btn--primary" disabled={submitting}>
            {submitting ? 'Saving…' : isEdit ? 'Save changes' : 'Create posting'}
          </button>
          <button
            type="button"
            className="btn btn--ghost"
            onClick={() => navigate('/hr/jobs')}
            disabled={submitting}
          >
            Cancel
          </button>
        </div>
      </form>
    </DashboardLayout>
  )
}

export default HrJobForm
