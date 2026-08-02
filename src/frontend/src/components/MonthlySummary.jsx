import { useState } from 'react'
import { apiRequest } from '../api.js'

function money(value) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(Number(value || 0))
}

function monthName(month) {
  return new Intl.DateTimeFormat('en-IN', { month: 'long' })
    .format(new Date(2000, Number(month) - 1, 1))
}

function downloadSummary(summary) {
  const categoryLines = Object.entries(summary.totalsByCategory)
    .map(([category, value]) => `- ${category}: ${money(value)}`)

  const lines = [
    'Smart Expense Tracker - Monthly Summary',
    '=======================================',
    `Year: ${summary.year}`,
    `Month: ${monthName(summary.month)} (${String(summary.month).padStart(2, '0')})`,
    `Total spend: ${money(summary.total)}`,
    `Number of expenses: ${summary.expenseCount}`,
    '',
    'Category totals:',
    ...(categoryLines.length > 0 ? categoryLines : ['- No expenses in this month']),
  ]

  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `expense-summary-${summary.year}-${String(summary.month).padStart(2, '0')}.txt`
  document.body.appendChild(link)
  link.click()
  link.remove()
  setTimeout(() => URL.revokeObjectURL(url), 0)
}

export default function MonthlySummary() {
  const [year, setYear] = useState('')
  const [month, setMonth] = useState('')
  const [summary, setSummary] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function load(event) {
    event.preventDefault()
    setError('')
    setLoading(true)

    try {
      const selectedYear = Number(year)
      const selectedMonth = Number(month)
      const result = await apiRequest(`/expenses/summary/monthly?year=${selectedYear}&month=${selectedMonth}`)
      setSummary(result)
      downloadSummary(result)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">ANALYTICS</p>
          <h2>Monthly summary</h2>
        </div>
      </div>
      <form className="inline-form" onSubmit={load}>
        <label>Year<input type="number" min="2000" max="2200" value={year} onChange={(e) => setYear(e.target.value)} required /></label>
        <label>Month<input type="number" min="1" max="12" value={month} onChange={(e) => setMonth(e.target.value)} required /></label>
        <button className="secondary" disabled={loading}>{loading ? 'Loading…' : 'Load summary'}</button>
      </form>
      {error && <div className="alert error">{error}</div>}
      {summary && (
        <div className="monthly-result">
          <div><span>Total</span><strong>{money(summary.total)}</strong></div>
          <div><span>Records</span><strong>{summary.expenseCount}</strong></div>
          <div className="category-pills">
            {Object.entries(summary.totalsByCategory).map(([category, value]) => (
              <span key={category}>{category}: {money(value)}</span>
            ))}
            {Object.keys(summary.totalsByCategory).length === 0 && <span>No expenses in this month</span>}
          </div>
          <p className="download-note">The selected monthly summary was downloaded as a text file.</p>
        </div>
      )}
    </section>
  )
}
