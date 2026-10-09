import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { dashboardPathForRole } from '../routes/roles'

function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  const closeMenu = () => setMenuOpen(false)

  const handleLogout = () => {
    closeMenu()
    logout()
    navigate('/')
  }

  const navClass = `navbar__nav${menuOpen ? ' navbar__nav--open' : ''}`

  return (
    <header className="navbar">
      <div className="container navbar__inner">
        <Link to="/" className="brand" onClick={closeMenu}>
          <span className="brand__mark" aria-hidden="true">
            SJT
          </span>
          <span className="brand__name">Smart Job Tracker</span>
        </Link>

        <button
          type="button"
          className="navbar__toggle"
          aria-expanded={menuOpen}
          aria-controls="primary-navigation"
          aria-label="Toggle navigation menu"
          onClick={() => setMenuOpen((open) => !open)}
        >
          <span className="navbar__toggle-bar" aria-hidden="true" />
          <span className="navbar__toggle-bar" aria-hidden="true" />
          <span className="navbar__toggle-bar" aria-hidden="true" />
        </button>

        <nav id="primary-navigation" className={navClass} aria-label="Primary">
          <NavLink to="/" end onClick={closeMenu}>
            Home
          </NavLink>
          <NavLink to="/jobs" onClick={closeMenu}>
            Browse Jobs
          </NavLink>

          {user ? (
            <div className="navbar__actions">
              <NavLink to={dashboardPathForRole(user.role)} className="navbar__user" onClick={closeMenu}>
                <span className="badge badge--role">{user.role}</span>
                <span className="navbar__user-name">{user.fullName}</span>
              </NavLink>
              <button type="button" className="btn btn--ghost btn--sm" onClick={handleLogout}>
                Log out
              </button>
            </div>
          ) : (
            <div className="navbar__actions">
              <NavLink to="/login" className="navbar__link-btn" onClick={closeMenu}>
                Candidate Login
              </NavLink>
              <NavLink to="/hr/login" className="navbar__link-btn" onClick={closeMenu}>
                HR Login
              </NavLink>
              <Link to="/register" className="btn btn--primary btn--sm" onClick={closeMenu}>
                Register
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  )
}

export default Navbar
