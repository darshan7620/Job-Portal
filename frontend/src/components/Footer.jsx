import { Link } from 'react-router-dom'

const CURRENT_YEAR = new Date().getFullYear()

function Footer() {
  return (
    <footer className="footer">
      <div className="container footer__inner">
        <div className="footer__brand">
          <span className="brand__mark" aria-hidden="true">
            SJT
          </span>
          <div>
            <p className="footer__title">Smart Job Tracker</p>
            <p className="footer__tagline">
              Find roles, track applications, and match with AI.
            </p>
          </div>
        </div>

        <nav className="footer__links" aria-label="Footer">
          <div className="footer__column">
            <p className="footer__heading">Explore</p>
            <Link to="/">Home</Link>
            <Link to="/#featured-jobs">Browse Jobs</Link>
          </div>
          <div className="footer__column">
            <p className="footer__heading">Account</p>
            <Link to="/login">Candidate Login</Link>
            <Link to="/hr/login">HR Login</Link>
            <Link to="/register">Register</Link>
          </div>
        </nav>
      </div>

      <div className="container footer__bottom">
        <p>© {CURRENT_YEAR} Smart Job Tracker — portfolio project.</p>
        <p className="footer__note">
          Job search, applications, and matching arrive in later stages.
        </p>
      </div>
    </footer>
  )
}

export default Footer
