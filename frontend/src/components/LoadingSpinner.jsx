function LoadingSpinner({ label = 'Loading…', full = false }) {
  return (
    <div className={full ? 'spinner spinner--full' : 'spinner'} role="status" aria-live="polite">
      <span className="spinner__ring" aria-hidden="true" />
      <span className="spinner__label">{label}</span>
    </div>
  )
}

export default LoadingSpinner
