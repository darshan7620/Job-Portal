import StatusBadge from './StatusBadge'

function StatusBreakdown({ title, items = [] }) {
  const max = items.reduce((peak, item) => Math.max(peak, item.count), 0)

  return (
    <section className="panel">
      <h2 className="panel__title">{title}</h2>
      <ul className="breakdown">
        {items.map((item) => (
          <li key={item.status} className="breakdown__row">
            <span className="breakdown__label">
              <StatusBadge status={item.status} />
            </span>
            <span className="breakdown__bar" aria-hidden="true">
              <span
                className="breakdown__bar-fill"
                style={{ width: max > 0 ? `${(item.count / max) * 100}%` : '0%' }}
              />
            </span>
            <span className="breakdown__value">{item.count}</span>
          </li>
        ))}
      </ul>
    </section>
  )
}

export default StatusBreakdown
