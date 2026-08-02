function money(value) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(Number(value || 0))
}

export default function SummaryCards({ total, categoryTotals }) {
  const largest = [...categoryTotals].sort((a, b) => Number(b.total) - Number(a.total))[0]

  return (
    <section className="summary-grid">
      <article className="metric-card">
        <span>Total spend</span>
        <strong>{money(total)}</strong>
      </article>
      <article className="metric-card">
        <span>Largest category</span>
        <strong>{largest ? largest.category : '—'}</strong>
        <small>{largest ? money(largest.total) : 'No data'}</small>
      </article>
    </section>
  )
}
