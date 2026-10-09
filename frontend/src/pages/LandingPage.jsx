import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import JobCard from '../components/JobCard'
import LoadingSpinner from '../components/LoadingSpinner'
import * as jobService from '../services/jobService'

const FEATURES = [
  {
    title: 'Discover roles',
    body: 'Search openings by title, skills, or company and filter by location to find the right fit faster.',
  },
  {
    title: 'Track applications',
    body: 'Follow every application from submitted to shortlisted to hired in one clean view.',
  },
  {
    title: 'AI job matching',
    body: 'Compare your profile with a job description to see matching skills, gaps, and suggestions.',
  },
]

function LandingPage() {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const [location, setLocation] = useState('')
  const [jobs, setJobs] = useState([])
  const [loadingJobs, setLoadingJobs] = useState(true)

  useEffect(() => {
    let active = true
    jobService
      .searchJobs({ page: 0, size: 6, sort: 'newest' })
      .then((response) => {
        if (active) setJobs(response.content || [])
      })
      .catch(() => {
        if (active) setJobs([])
      })
      .finally(() => {
        if (active) setLoadingJobs(false)
      })
    return () => {
      active = false
    }
  }, [])

  const handleSearch = (event) => {
    event.preventDefault()
    const params = new URLSearchParams()
    if (query.trim()) params.set('keyword', query.trim())
    if (location.trim()) params.set('location', location.trim())
    navigate(`/jobs${params.toString() ? `?${params}` : ''}`)
  }

  return (
    <>
      <section className="hero">
        <div className="container hero__inner">
          <div className="hero__copy">
            <p className="hero__eyebrow">Job portal for candidates &amp; recruiters</p>
            <h1 className="hero__title">
              Find your next role. <span>Hire your next teammate.</span>
            </h1>
            <p className="hero__lead">
              Smart Job Tracker brings job discovery, application tracking, and AI-powered skill
              matching together in one place.
            </p>

            <form className="hero__search" onSubmit={handleSearch} aria-label="Job search">
              <div className="hero__search-field">
                <label htmlFor="job-query" className="sr-only">
                  Job title, skill, or company
                </label>
                <input
                  id="job-query"
                  type="search"
                  placeholder="Job title, skill, or company"
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                />
              </div>
              <div className="hero__search-field">
                <label htmlFor="job-location" className="sr-only">
                  Location
                </label>
                <input
                  id="job-location"
                  type="text"
                  placeholder="City or remote"
                  value={location}
                  onChange={(event) => setLocation(event.target.value)}
                />
              </div>
              <button type="submit" className="btn btn--primary">
                Search
              </button>
            </form>

            <div className="hero__cta">
              <Link to="/register" className="btn btn--primary">
                I&apos;m looking for a job
              </Link>
              <Link to="/hr/login" className="btn btn--ghost">
                I&apos;m hiring
              </Link>
            </div>
          </div>

          <div className="hero__visual" aria-hidden="true">
            <div className="hero__card">
              <div className="hero__card-head">
                <span className="dot dot--red" />
                <span className="dot dot--amber" />
                <span className="dot dot--green" />
              </div>
              <div className="hero__card-row">
                <span className="skeleton skeleton--title" />
                <span className="skeleton skeleton--pill" />
              </div>
              <span className="skeleton skeleton--line" />
              <span className="skeleton skeleton--line skeleton--short" />
              <div className="hero__card-tags">
                <span className="chip">React</span>
                <span className="chip">Spring</span>
                <span className="chip">SQL</span>
              </div>
            </div>
            <div className="hero__stat">
              <p className="hero__stat-value">Apply → Track → Match</p>
              <p className="hero__stat-label">One workflow, end to end</p>
            </div>
          </div>
        </div>
      </section>

      <section className="section" id="featured-jobs">
        <div className="container">
          <div className="section__head section__head--row">
            <div>
              <h2>Featured jobs</h2>
              <p>The latest roles posted by hiring teams.</p>
            </div>
            <Link to="/jobs" className="btn btn--ghost btn--sm">
              View all jobs
            </Link>
          </div>

          {loadingJobs ? (
            <LoadingSpinner label="Loading jobs…" />
          ) : jobs.length === 0 ? (
            <div className="empty-state">
              <h2>No jobs published yet</h2>
              <p>New roles will appear here as soon as recruiters publish them.</p>
            </div>
          ) : (
            <div className="job-grid">
              {jobs.map((job) => (
                <JobCard key={job.id} job={job} />
              ))}
            </div>
          )}
        </div>
      </section>

      <section className="section section--muted">
        <div className="container">
          <div className="section__head">
            <h2>Everything you need to hire and get hired</h2>
          </div>
          <div className="feature-grid">
            {FEATURES.map((feature) => (
              <article className="feature-card" key={feature.title}>
                <div className="feature-card__icon" aria-hidden="true">
                  <span />
                </div>
                <h3>{feature.title}</h3>
                <p>{feature.body}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="section">
        <div className="container cta-grid">
          <article className="cta-card">
            <h2>For candidates</h2>
            <p>Create a profile, apply in a click, and keep every application in one place.</p>
            <Link to="/register" className="btn btn--primary">
              Create your account
            </Link>
          </article>
          <article className="cta-card cta-card--dark">
            <h2>For recruiters</h2>
            <p>Post roles, review applicants, and move candidates through your pipeline with ease.</p>
            <Link to="/hr/login" className="btn btn--light">
              Recruiter sign in
            </Link>
          </article>
        </div>
      </section>
    </>
  )
}

export default LandingPage
