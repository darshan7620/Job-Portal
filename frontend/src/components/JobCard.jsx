import { Link } from 'react-router-dom'
import { employmentTypeLabel, formatSalary } from '../constants/jobs'

function JobCard({ job }) {
  const salary = formatSalary(job.salaryMin, job.salaryMax)

  return (
    <article className="job-card">
      <div className="job-card__top">
        <div className="job-card__logo" aria-hidden="true">
          {job.company ? job.company.charAt(0) : 'J'}
        </div>
        {job.employmentType ? (
          <span className="chip">{employmentTypeLabel(job.employmentType)}</span>
        ) : null}
      </div>
      <h3 className="job-card__title">{job.title}</h3>
      <p className="job-card__company">{job.company}</p>
      <p className="job-card__meta">{job.location || 'Location not specified'}</p>
      {salary ? <p className="job-card__salary">{salary}</p> : null}
      <Link to={`/jobs/${job.id}`} className="btn btn--primary btn--block">
        View details
      </Link>
    </article>
  )
}

export default JobCard
