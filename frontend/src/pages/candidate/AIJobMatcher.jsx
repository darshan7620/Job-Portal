import { useState } from 'react'
import DashboardLayout from '../../components/DashboardLayout'
import ErrorMessage from '../../components/ErrorMessage'
import * as aiService from '../../services/aiService'

const NAV_ITEMS = [
  { to: '/candidate/dashboard', label: 'Dashboard' },
  { to: '/jobs', label: 'Browse jobs' },
  { to: '/candidate/applications', label: 'My applications' },
  { to: '/candidate/ai-matcher', label: 'AI matcher' },
]

const MAX_DESCRIPTION_LENGTH = 4000
const MAX_SKILLS = 50

const FALLBACK_NOTE =
  'matchScore is an approximate, AI-generated estimate of how well your skills cover the job ' +
  'description. It is not an objective probability of being hired and is not used for hiring decisions.'

function parseSkills(value) {
  const seen = new Set()
  const skills = []
  value.split(',').forEach((part) => {
    const skill = part.trim()
    if (skill && !seen.has(skill.toLowerCase())) {
      seen.add(skill.toLowerCase())
      skills.push(skill)
    }
  })
  return skills
}

function SkillList({ title, skills, variant }) {
  return (
    <section className="panel">
      <h2 className="panel__title">{title}</h2>
      {skills.length === 0 ? (
        <p className="muted-text">None identified.</p>
      ) : (
        <div className="tag-row">
          {skills.map((skill) => (
            <span className={variant === 'missing' ? 'chip chip--missing' : 'chip'} key={skill}>
              {skill}
            </span>
          ))}
        </div>
      )}
    </section>
  )
}

function AIJobMatcher() {
  const [skillsText, setSkillsText] = useState('')
  const [description, setDescription] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')

    const skills = parseSkills(skillsText)
    if (skills.length === 0) {
      setError('skills: Add at least one skill')
      return
    }
    if (skills.length > MAX_SKILLS) {
      setError(`skills: Please provide at most ${MAX_SKILLS} skills`)
      return
    }
    if (!description.trim()) {
      setError('jobDescription: Job description is required')
      return
    }
    if (description.length > MAX_DESCRIPTION_LENGTH) {
      setError(`jobDescription: Job description must be at most ${MAX_DESCRIPTION_LENGTH} characters`)
      return
    }

    setLoading(true)
    setResult(null)
    try {
      const response = await aiService.matchJob({ skills, jobDescription: description })
      setResult(response)
    } catch (err) {
      setError(err.detail || 'Could not match your skills right now. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const hasEmptyResult =
    result &&
    result.matchingSkills.length === 0 &&
    result.missingSkills.length === 0 &&
    result.improvementSuggestions.length === 0

  return (
    <DashboardLayout
      title="AI job matcher"
      subtitle="Compare your skills against a job description using a local AI model."
      navItems={NAV_ITEMS}
    >
      <form className="form matcher-form" onSubmit={handleSubmit}>
        <div className="form__group">
          <label htmlFor="matcher-skills">Your skills (comma-separated)</label>
          <input
            id="matcher-skills"
            type="text"
            placeholder="Java, Spring Boot, MySQL, REST APIs"
            value={skillsText}
            onChange={(event) => setSkillsText(event.target.value)}
          />
        </div>

        <div className="form__group">
          <label htmlFor="matcher-description">Job description</label>
          <textarea
            id="matcher-description"
            rows="8"
            maxLength={MAX_DESCRIPTION_LENGTH}
            placeholder="Paste the job description here…"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />
          <p className="muted-text">
            {description.length}/{MAX_DESCRIPTION_LENGTH} characters
          </p>
        </div>

        <ErrorMessage message={error} onDismiss={() => setError('')} />

        <div className="form__actions">
          <button type="submit" className="btn btn--primary" disabled={loading}>
            {loading ? 'Matching…' : 'Match job'}
          </button>
        </div>
      </form>

      {loading ? (
        <p className="muted-text matcher-status">Analyzing your skills with the local model…</p>
      ) : null}

      {result ? (
        <div className="matcher-result">
          <section className="panel">
            <div className="matcher-score">
              <span className="matcher-score__value">{result.matchScore}</span>
              <span className="matcher-score__max">/ 100</span>
            </div>
            <p className="matcher-score__label">Approximate AI-generated match estimate</p>
            <p className="matcher-disclaimer">{result.matchScoreNote || FALLBACK_NOTE}</p>
          </section>

          {hasEmptyResult ? (
            <div className="empty-state">
              <h2>No comparison available</h2>
              <p>The model did not identify any overlapping or missing skills. Try adding more detail.</p>
            </div>
          ) : (
            <>
              <div className="panel-grid">
                <SkillList title="Matching skills" skills={result.matchingSkills} />
                <SkillList title="Missing skills" skills={result.missingSkills} variant="missing" />
              </div>

              <section className="panel">
                <h2 className="panel__title">Improvement suggestions</h2>
                {result.improvementSuggestions.length === 0 ? (
                  <p className="muted-text">No suggestions provided.</p>
                ) : (
                  <ul className="ai-suggestions">
                    {result.improvementSuggestions.map((suggestion) => (
                      <li key={suggestion}>{suggestion}</li>
                    ))}
                  </ul>
                )}
              </section>

              <section className="panel">
                <h2 className="panel__title">Summary</h2>
                <p className="ai-summary">{result.summary || 'No summary provided.'}</p>
              </section>
            </>
          )}
        </div>
      ) : null}
    </DashboardLayout>
  )
}

export default AIJobMatcher
