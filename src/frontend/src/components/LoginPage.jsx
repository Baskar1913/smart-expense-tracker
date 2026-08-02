import { useState } from 'react'
import {
  checkForgotPasswordUser,
  login,
  registerAccount,
  resetPassword,
  verifyForgotPassword,
} from '../api.js'

export default function LoginPage() {
  const [view, setView] = useState('login')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  // Login fields
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')

  // Registration fields
  const [registerUsername, setRegisterUsername] = useState('')
  const [registerEmail, setRegisterEmail] = useState('')
  const [registerPassword, setRegisterPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  // Forgot-password fields
  const [forgotUsername, setForgotUsername] = useState('')
  const [forgotEmail, setForgotEmail] = useState('')
  const [resetToken, setResetToken] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmNewPassword, setConfirmNewPassword] =
    useState('')

  function changeView(nextView) {
    setView(nextView)
    setError('')
    setMessage('')
  }

  function openRegister() {
    setError('')
    setMessage('')
    setView('register')
  }

  function openLogin() {
    setError('')
    setMessage('')
    setView('login')
  }

  async function handleLogin(event) {
    event.preventDefault()

    setError('')
    setMessage('')
    setLoading(true)

    try {
      await login(username.trim(), password)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleRegister(event) {
    event.preventDefault()

    setError('')
    setMessage('')

    if (registerPassword !== confirmPassword) {
      setError('Passwords do not match')
      return
    }

    setLoading(true)

    try {
      const result = await registerAccount(
        registerUsername.trim(),
        registerEmail.trim(),
        registerPassword
      )

      const createdUsername = registerUsername.trim()

      // Show the created username on the login form
      setUsername(createdUsername)
      setPassword('')

      // Clear registration fields
      setRegisterUsername('')
      setRegisterEmail('')
      setRegisterPassword('')
      setConfirmPassword('')

      setView('login')

      setMessage(
        result.message ||
          'Account created successfully. Please sign in.'
      )
    } catch (err) {
      setMessage('')
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleUserCheck(event) {
    event.preventDefault()

    setError('')
    setMessage('')
    setLoading(true)

    try {
      const result = await checkForgotPasswordUser(
        forgotUsername.trim()
      )

      if (result.exists) {
        setMessage(result.message)
        setView('forgot-verify')
      } else {
        setError(result.message)
        setView('forgot-not-found')
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleForgotVerification(event) {
    event.preventDefault()

    setError('')
    setMessage('')
    setLoading(true)

    try {
      const result = await verifyForgotPassword(
        forgotUsername.trim(),
        forgotEmail.trim()
      )

      setResetToken(result.resetToken)
      setMessage(result.message)
      setView('forgot-reset')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  async function handlePasswordReset(event) {
    event.preventDefault()

    setError('')
    setMessage('')

    if (newPassword !== confirmNewPassword) {
      setError('Passwords do not match')
      return
    }

    setLoading(true)

    try {
      const result = await resetPassword(
        resetToken,
        newPassword
      )

      setUsername(forgotUsername.trim())
      setPassword('')

      // Clear password-reset fields
      setForgotEmail('')
      setResetToken('')
      setNewPassword('')
      setConfirmNewPassword('')

      setView('login')
      setMessage(result.message)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="login-shell">
      <section className="login-card">
        <div className="brand-mark">₹</div>

        <p className="eyebrow">
          SECURE EXPENSE MANAGEMENT
        </p>

        <h1>Smart Expense Tracker</h1>

        {message && (
          <div className="alert success auth-alert">
            {message}
          </div>
        )}

        {error && (
          <div className="alert error auth-alert">
            {error}
          </div>
        )}

        {view === 'login' && (
          <form
            onSubmit={handleLogin}
            className="stack"
          >
            <label>
              Username

              <input
                value={username}
                onChange={(event) =>
                  setUsername(event.target.value)
                }
                autoComplete="username"
                required
              />
            </label>

            <label>
              Password

              <input
                type="password"
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                autoComplete="current-password"
                required
              />
            </label>

            <button
              type="button"
              className="text-button forgot-link"
              onClick={() => {
                setForgotUsername(username.trim())
                changeView('forgot-check')
              }}
            >
              Forgot password?
            </button>

            <button
              type="submit"
              className="primary"
              disabled={loading}
            >
              {loading ? 'Signing in…' : 'Sign in'}
            </button>

            <p className="auth-switch">
              New customer?{' '}

              <button
                type="button"
                className="text-button"
                onClick={openRegister}
              >
                Create account
              </button>
            </p>
          </form>
        )}

        {view === 'register' && (
          <form
            onSubmit={handleRegister}
            className="stack"
          >
            <div className="auth-heading">
              <h2>Create account</h2>

              <button
                type="button"
                className="text-button"
                onClick={openLogin}
              >
                Back to login
              </button>
            </div>

            <label>
              Username

              <input
                value={registerUsername}
                onChange={(event) =>
                  setRegisterUsername(event.target.value)
                }
                autoComplete="username"
                required
              />
            </label>

            <label>
              Email

              <input
                type="email"
                value={registerEmail}
                onChange={(event) =>
                  setRegisterEmail(event.target.value)
                }
                autoComplete="email"
                required
              />
            </label>

            <label>
              Password

              <input
                type="password"
                value={registerPassword}
                onChange={(event) =>
                  setRegisterPassword(event.target.value)
                }
                autoComplete="new-password"
                minLength="8"
                required
              />
            </label>

            <label>
              Confirm password

              <input
                type="password"
                value={confirmPassword}
                onChange={(event) =>
                  setConfirmPassword(event.target.value)
                }
                autoComplete="new-password"
                minLength="8"
                required
              />
            </label>

            <button
              type="submit"
              className="primary"
              disabled={loading}
            >
              {loading
                ? 'Creating account…'
                : 'Create account'}
            </button>
          </form>
        )}

        {view === 'forgot-check' && (
          <form
            onSubmit={handleUserCheck}
            className="stack"
          >
            <div className="auth-heading">
              <h2>Forgot password</h2>

              <button
                type="button"
                className="text-button"
                onClick={openLogin}
              >
                Back to login
              </button>
            </div>

            <p className="muted compact-text">
              Enter your username. We will first check
              whether the account exists.
            </p>

            <label>
              Username

              <input
                value={forgotUsername}
                onChange={(event) =>
                  setForgotUsername(event.target.value)
                }
                autoComplete="username"
                required
              />
            </label>

            <button
              type="submit"
              className="primary"
              disabled={loading}
            >
              {loading
                ? 'Checking…'
                : 'Check username'}
            </button>
          </form>
        )}

        {view === 'forgot-not-found' && (
          <div className="stack">
            <div className="auth-heading">
              <h2>Account not found</h2>

              <button
                type="button"
                className="text-button"
                onClick={openLogin}
              >
                Back to login
              </button>
            </div>

            <p className="muted">
              The username{' '}
              <strong>{forgotUsername}</strong> does not
              exist. Create a customer account to continue.
            </p>

            <button
              type="button"
              className="primary"
              onClick={() => {
                setRegisterUsername(forgotUsername)
                changeView('register')
              }}
            >
              Create account
            </button>

            <button
              type="button"
              className="secondary"
              onClick={() =>
                changeView('forgot-check')
              }
            >
              Try another username
            </button>
          </div>
        )}

        {view === 'forgot-verify' && (
          <form
            onSubmit={handleForgotVerification}
            className="stack"
          >
            <div className="auth-heading">
              <h2>Verify account</h2>

              <button
                type="button"
                className="text-button"
                onClick={() =>
                  changeView('forgot-check')
                }
              >
                Change username
              </button>
            </div>

            <p className="muted compact-text">
              Enter the registered email for{' '}
              <strong>{forgotUsername}</strong>.
            </p>

            <label>
              Registered email

              <input
                type="email"
                value={forgotEmail}
                onChange={(event) =>
                  setForgotEmail(event.target.value)
                }
                autoComplete="email"
                required
              />
            </label>

            <button
              type="submit"
              className="primary"
              disabled={loading}
            >
              {loading
                ? 'Verifying…'
                : 'Verify identity'}
            </button>
          </form>
        )}

        {view === 'forgot-reset' && (
          <form
            onSubmit={handlePasswordReset}
            className="stack"
          >
            <div className="auth-heading">
              <h2>Set new password</h2>

              <button
                type="button"
                className="text-button"
                onClick={openLogin}
              >
                Cancel
              </button>
            </div>

            <label>
              New password

              <input
                type="password"
                value={newPassword}
                onChange={(event) =>
                  setNewPassword(event.target.value)
                }
                autoComplete="new-password"
                minLength="8"
                required
              />
            </label>

            <label>
              Confirm new password

              <input
                type="password"
                value={confirmNewPassword}
                onChange={(event) =>
                  setConfirmNewPassword(
                    event.target.value
                  )
                }
                autoComplete="new-password"
                minLength="8"
                required
              />
            </label>

            <button
              type="submit"
              className="primary"
              disabled={loading}
            >
              {loading
                ? 'Resetting…'
                : 'Reset password'}
            </button>
          </form>
        )}
      </section>
    </main>
  )
}