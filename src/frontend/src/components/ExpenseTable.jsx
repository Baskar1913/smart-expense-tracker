import { apiRequest } from '../api.js'

function money(value) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(Number(value || 0))
}

export default function ExpenseTable({ expenses, onDeleted, onError }) {
  async function remove(expense) {
    if (!window.confirm(`Delete the expense "${expense.title}"?`)) return
    try {
      await apiRequest(`/expenses/${expense.id}`, { method: 'DELETE' })
      onDeleted()
    } catch (err) {
      onError(err.message)
    }
  }

  return (
    <div className="table-wrap">
      <table>
        <thead><tr><th>Date</th><th>Title</th><th>Category</th><th className="number">Amount</th><th>Action</th></tr></thead>
        <tbody>
          {expenses.map((expense) => (
            <tr key={expense.id}>
              <td>{expense.date}</td><td>{expense.title}</td>
              <td><span className="badge neutral">{expense.category}</span></td>
              <td className="number strong">{money(expense.amount)}</td>
              <td><button className="danger-link" onClick={() => remove(expense)}>Delete</button></td>
            </tr>
          ))}
          {expenses.length === 0 && <tr><td className="empty" colSpan="5">No expenses found.</td></tr>}
        </tbody>
      </table>
    </div>
  )
}
