import { useCallback, useEffect, useState } from 'react'
import { apiRequest, logout } from '../api.js'
import ExpenseForm from './ExpenseForm.jsx'
import ExpenseTable from './ExpenseTable.jsx'
import MonthlySummary from './MonthlySummary.jsx'
import SummaryCards from './SummaryCards.jsx'


function isValidFourDigitDate(value) {
  return !value || /^\d{4}-\d{2}-\d{2}$/.test(value)
}

export default function Dashboard({ auth }) {
  const [expenses, setExpenses] = useState([])
  const [total, setTotal] = useState(0)
  const [categoryTotals, setCategoryTotals] = useState([])
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const loadDashboard = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [expenseData, totalData, categories] = await Promise.all([
        apiRequest('/expenses'),
        apiRequest('/expenses/total'),
        apiRequest('/expenses/total/by-category'),
      ])
      setExpenses(expenseData)
      setTotal(totalData.total)
      setCategoryTotals(categories)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadDashboard() }, [loadDashboard])

  async function search(event) {
    event.preventDefault()
    setError('')
    if (!isValidFourDigitDate(from)) {
      setError('From date must use a four-digit year.')
      return
    }
    if (!isValidFourDigitDate(to)) {
      setError('To date must use a four-digit year.')
      return
    }
    if (from && to && from > to) {
      setError('From date cannot be after To date.')
      return
    }

    const params = new URLSearchParams()
    if (query.trim()) params.set('query', query.trim())
    if (category.trim()) params.set('category', category.trim())
    if (from) params.set('from', from)
    if (to) params.set('to', to)

    try {
      setExpenses(await apiRequest(`/expenses/search?${params.toString()}`))
    } catch (err) {
      setError(err.message)
    }
  }

  function clearSearch() {
    setQuery('')
    setCategory('')
    setFrom('')
    setTo('')
    loadDashboard()
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div><p className="eyebrow">SMART EXPENSE TRACKER</p><h1>Expense dashboard</h1></div>
		<div className="user-area">
		  <span className="username">
		    {auth?.username}
		  </span>

		  <button className="secondary" onClick={logout}>
		    Log out
		  </button>
		</div>
      </header>

      <main className="content">
        {error && <div className="alert error">{error}</div>}
        <SummaryCards total={total} categoryTotals={categoryTotals} />

        <div className="two-column">
          <ExpenseForm onCreated={loadDashboard} />
          <MonthlySummary />
        </div>

        <section className="panel">
          <div className="panel-heading split">
            <div><h2>Your expenses</h2></div>
            <button className="secondary" onClick={loadDashboard}>Refresh</button>
          </div>

          <form className="search-grid" onSubmit={search}>
            <label>Search<input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Title or category" /></label>
            <label>Category<input value={category} onChange={(e) => setCategory(e.target.value)} /></label>
            <label>From<input type="date" min="1000-01-01" max="9999-12-31" value={from} onChange={(e) => setFrom(e.target.value)} /></label>
            <label>To<input type="date" min="1000-01-01" max="9999-12-31" value={to} onChange={(e) => setTo(e.target.value)} /></label>
            <div className="search-actions"><button className="primary">Search</button><button type="button" className="secondary" onClick={clearSearch}>Clear</button></div>
          </form>

          {loading ? <div className="empty">Loading expenses…</div> : (
            <ExpenseTable expenses={expenses} onDeleted={loadDashboard} onError={setError} />
          )}
        </section>

      </main>
    </div>
  )
}
