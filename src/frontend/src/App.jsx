import { useEffect, useState } from 'react'
import { loadAuth } from './api.js'
import LoginPage from './components/LoginPage.jsx'
import Dashboard from './components/Dashboard.jsx'

export default function App() {
  const [auth, setAuth] = useState(loadAuth())

  useEffect(() => {
    const update = () => setAuth(loadAuth())
    window.addEventListener('expense-auth-changed', update)
    return () => window.removeEventListener('expense-auth-changed', update)
  }, [])

  return auth ? <Dashboard auth={auth} /> : <LoginPage />
}