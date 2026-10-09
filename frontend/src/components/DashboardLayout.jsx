import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

function DashboardLayout({ title, subtitle, navItems = [], children }) {
  const { user } = useAuth()

  return (
    <div className="container dashboard">
      <aside className="dashboard__sidebar">
        <div className="dashboard__user">
          <p className="dashboard__greeting">Signed in as</p>
          <p className="dashboard__user-name">{user?.fullName}</p>
          <span className="badge badge--role">{user?.role}</span>
        </div>

        <nav className="dashboard__nav" aria-label="Dashboard">
          {navItems.map((item) =>
            item.disabled ? (
              <span key={item.label} className="dashboard__nav-item dashboard__nav-item--disabled">
                {item.label}
                <span className="dashboard__soon">Soon</span>
              </span>
            ) : (
              <NavLink key={item.to} to={item.to} end className="dashboard__nav-item">
                {item.label}
              </NavLink>
            ),
          )}
        </nav>
      </aside>

      <section className="dashboard__content">
        <header className="dashboard__header">
          <h1>{title}</h1>
          {subtitle ? <p className="dashboard__subtitle">{subtitle}</p> : null}
        </header>
        {children}
      </section>
    </div>
  )
}

export default DashboardLayout
