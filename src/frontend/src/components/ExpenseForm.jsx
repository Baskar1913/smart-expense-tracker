import { useState } from 'react'
import { apiRequest } from '../api.js'

function getTodayDate() {
  const today = new Date()

  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

const initial = {
  title: '',
  amount: '',
  category: '',
  date: getTodayDate(),
}

function isValidExpenseDate(value) {
  if (!value) {
    return false
  }

  // Date must be exactly yyyy-MM-dd.
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return false
  }

  const [year, month, day] = value
    .split('-')
    .map(Number)

  if (year < 1000 || year > 9999) {
    return false
  }

  const parsedDate = new Date(
    Date.UTC(year, month - 1, day)
  )

  return (
    parsedDate.getUTCFullYear() === year &&
    parsedDate.getUTCMonth() === month - 1 &&
    parsedDate.getUTCDate() === day
  )
}

export default function ExpenseForm({ onCreated }) {
  const [form, setForm] = useState(initial)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  function update(field, value) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  function handleDateChange(event) {
    const value = event.target.value

    // Allow clearing the field.
    if (value === '') {
      update('date', '')
      setError('')
      return
    }

    const yearPart = value.split('-')[0]

    // Reject years such as 202666.
    if (yearPart.length !== 4) {
      setError(
        'The year must contain exactly four digits.'
      )
      return
    }

    const year = Number(yearPart)

    if (year < 1000 || year > 9999) {
      setError(
        'Enter a valid year between 1000 and 9999.'
      )
      return
    }

    setError('')
    update('date', value)
  }

  async function submit(event) {
    event.preventDefault()
    setError('')

    if (!form.title.trim()) {
      setError('Title is required.')
      return
    }

    const amount = Number(form.amount)

    if (
      !Number.isFinite(amount) ||
      amount <= 0
    ) {
      setError(
        'Amount must be greater than zero.'
      )
      return
    }

    if (!form.category.trim()) {
      setError('Category is required.')
      return
    }

    if (!form.date) {
      setError('Expense date is required.')
      return
    }

    if (!isValidExpenseDate(form.date)) {
      setError(
        'Enter a valid date using a four-digit year.'
      )
      return
    }

    setSaving(true)

    try {
      await apiRequest('/expenses', {
        method: 'POST',
        body: JSON.stringify({
          ...form,
          title: form.title.trim(),
          category: form.category.trim(),
          amount,
        }),
      })

      setForm({
        ...initial,
        date: getTodayDate(),
      })

      onCreated()
    } catch (err) {
      setError(
        err.message ||
          'Unable to add the expense.'
      )
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">
            NEW RECORD
          </p>

          <h2>Add expense</h2>
        </div>
      </div>

      <form
        className="form-grid"
        onSubmit={submit}
      >
        <label className="span-2">
          Title

          <input
            value={form.title}
            onChange={(event) =>
              update(
                'title',
                event.target.value
              )
            }
            maxLength="120"
            required
          />
        </label>

        <label>
          Amount

          <input
            type="number"
            min="0.01"
            step="0.01"
            value={form.amount}
            onChange={(event) =>
              update(
                'amount',
                event.target.value
              )
            }
            required
          />
        </label>

        <label>
          Category

          <input
            value={form.category}
            onChange={(event) =>
              update(
                'category',
                event.target.value
              )
            }
            maxLength="60"
            required
          />
        </label>

        <label>
          Date

          <input
            type="date"
            value={form.date}
            onChange={handleDateChange}
            min="1000-01-01"
            max="9999-12-31"
            required
          />
        </label>

        <div className="form-action">
          <button
            type="submit"
            className="primary"
            disabled={saving}
          >
            {saving
              ? 'Saving…'
              : 'Add expense'}
          </button>
        </div>

        {error && (
          <div className="alert error span-2">
            {error}
          </div>
        )}
      </form>
    </section>
  )
}