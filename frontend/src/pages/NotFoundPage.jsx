import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <section className="not-found">
      <div className="container not-found__inner">
        <p className="not-found__code">404</p>
        <h1>We couldn&apos;t find that page</h1>
        <p>The link may be broken or the page may have moved.</p>
        <div className="not-found__actions">
          <Link to="/" className="btn btn--primary">
            Back to home
          </Link>
          <Link to="/login" className="btn btn--ghost">
            Candidate login
          </Link>
        </div>
      </div>
    </section>
  )
}

export default NotFoundPage
