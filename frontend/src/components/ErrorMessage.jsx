function ErrorMessage({ message, onDismiss }) {
  if (!message) return null

  return (
    <div className="alert alert--error" role="alert">
      <span className="alert__text">{message}</span>
      {onDismiss ? (
        <button
          type="button"
          className="alert__close"
          onClick={onDismiss}
          aria-label="Dismiss message"
        >
          ×
        </button>
      ) : null}
    </div>
  )
}

export default ErrorMessage
