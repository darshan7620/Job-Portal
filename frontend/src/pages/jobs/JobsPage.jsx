// oxlint-disable react/set-state-in-effect
import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import JobCard from '../../components/JobCard'
import LoadingSpinner from '../../components/LoadingSpinner'
import ErrorMessage from '../../components/ErrorMessage'
import { EMPLOYMENT_TYPES } from '../../constants/jobs'
import * as jobService from '../../services/jobService'

const PAGE_SIZE = 9

function JobsPage() {
  const [searchParams, setSearchParams] = useSearchParams()

  const keyword = searchParams.get('keyword') || ''
  const location = searchParams.get('location') || ''
  const type = searchParams.get('type') || ''
  const sort = searchParams.get('sort') || 'newest'
  const page = Number(searchParams.get('page') || '0')

  const [form, setForm] = useState({ keyword, location, type, sort })
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    setForm({ keyword, location, type, sort })
  }, [keyword, location, type, sort])

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    jobService
      .searchJobs({ keyword, location, type, sort, page, size: PAGE_SIZE })
      .then((response) => {
        if (active) setData(response)
      })
      .catch((err) => {
        if (active) setError(err.detail || 'Could not load jobs.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [keyword, location, type, sort, page])

  const applyParams = (updates) => {
    const next = new URLSearchParams(searchParams)
    Object.entries(updates).forEach(([key, value]) => {
      if (value) next.set(key, value)
      else next.delete(key)
    })
    next.delete('page')
    setSearchParams(next)
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    applyParams(form)
  }

  const handleReset = () => {
    setSearchParams(new URLSearchParams())
  }

  const goToPage = (nextPage) => {
    const next = new URLSearchParams(searchParams)
    next.set('page', String(nextPage))
    setSearchParams(next)
  }

  const jobs = data?.content || []
  const totalPages = data?.totalPages || 0
  const hasFilters = Boolean(keyword || location || type)

  return (
    <div className="page container">
      <header className="page__head">
        <h1>Browse jobs</h1>
        <p>Explore open roles from hiring teams on Smart Job Tracker.</p>
      </header>

      <form className="filters" onSubmit={handleSubmit}>
        <div className="filters__field">
          <label htmlFor="filter-keyword">Keyword</label>
          <input
            id="filter-keyword"
            type="search"
            placeholder="Title, skill, or company"
            value={form.keyword}
            onChange={(event) => setForm({ ...form, keyword: event.target.value })}
          />
        </div>
        <div className="filters__field">
          <label htmlFor="filter-location">Location</label>
          <input
            id="filter-location"
            type="text"
            placeholder="City or remote"
            value={form.location}
            onChange={(event) => setForm({ ...form, location: event.target.value })}
          />
        </div>
        <div className="filters__field">
          <label htmlFor="filter-type">Employment type</label>
          <select
            id="filter-type"
            value={form.type}
            onChange={(event) => setForm({ ...form, type: event.target.value })}
          >
            <option value="">Any type</option>
            {EMPLOYMENT_TYPES.map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>
        </div>
        <div className="filters__field">
          <label htmlFor="filter-sort">Sort by</label>
          <select
            id="filter-sort"
            value={form.sort}
            onChange={(event) => setForm({ ...form, sort: event.target.value })}
          >
            <option value="newest">Newest first</option>
            <option value="oldest">Oldest first</option>
            <option value="title">Title (A–Z)</option>
            <option value="title_desc">Title (Z–A)</option>
          </select>
        </div>
        <div className="filters__actions">
          <button type="submit" className="btn btn--primary">
            Search
          </button>
          {hasFilters ? (
            <button type="button" className="btn btn--ghost" onClick={handleReset}>
              Reset
            </button>
          ) : null}
        </div>
      </form>

      <ErrorMessage message={error} onDismiss={() => setError('')} />

      {loading ? (
        <LoadingSpinner label="Loading jobs…" />
      ) : jobs.length === 0 ? (
        <div className="empty-state">
          <h2>No jobs found</h2>
          <p>Try adjusting your search terms or filters.</p>
        </div>
      ) : (
        <>
          <p className="results-count">
            {data.totalElements} {data.totalElements === 1 ? 'role' : 'roles'} found
          </p>
          <div className="job-grid">
            {jobs.map((job) => (
              <JobCard key={job.id} job={job} />
            ))}
          </div>

          {totalPages > 1 ? (
            <nav className="pagination" aria-label="Pagination">
              <button
                type="button"
                className="btn btn--ghost btn--sm"
                disabled={page <= 0}
                onClick={() => goToPage(page - 1)}
              >
                Previous
              </button>
              <span className="pagination__status">
                Page {page + 1} of {totalPages}
              </span>
              <button
                type="button"
                className="btn btn--ghost btn--sm"
                disabled={page >= totalPages - 1}
                onClick={() => goToPage(page + 1)}
              >
                Next
              </button>
            </nav>
          ) : null}
        </>
      )}
    </div>
  )
}

export default JobsPage
