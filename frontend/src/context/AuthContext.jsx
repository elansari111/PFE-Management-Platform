import { useContext, useState, useEffect } from 'react'
import apiClient from '../services/apiClient'
import { AuthContext } from './auth-context'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const savedUser = typeof window !== 'undefined' ? localStorage.getItem('pfe_user') : null
      return savedUser ? JSON.parse(savedUser) : null
    } catch {
      return null
    }
  })

  const [token, setToken] = useState(() => {
    try {
      return typeof window !== 'undefined' ? localStorage.getItem('pfe_token') : null
    } catch {
      return null
    }
  })

  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    async function loadCurrentUser() {
      const savedToken = typeof window !== 'undefined' ? localStorage.getItem('pfe_token') : null
      if (!savedToken) {
        setIsLoading(false)
        return
      }

      try {
        const response = await apiClient.get('/auth/me')
        setUser(response.data)
        localStorage.setItem('pfe_user', JSON.stringify(response.data))
      } catch (err) {
        console.warn('Session expirée ou token invalide:', err)
        logout()
      } finally {
        setIsLoading(false)
      }
    }

    loadCurrentUser()
  }, [])

  const login = async (email, password) => {
    const response = await apiClient.post('/auth/login', { email, password })
    const { token: receivedToken, user: receivedUser } = response.data

    setToken(receivedToken)
    setUser(receivedUser)
    localStorage.setItem('pfe_token', receivedToken)
    localStorage.setItem('pfe_user', JSON.stringify(receivedUser))

    return receivedUser
  }

  const register = async (userData) => {
    const response = await apiClient.post('/auth/register', userData)
    const { token: receivedToken, user: receivedUser } = response.data

    setToken(receivedToken)
    setUser(receivedUser)
    localStorage.setItem('pfe_token', receivedToken)
    localStorage.setItem('pfe_user', JSON.stringify(receivedUser))

    return receivedUser
  }

  const logout = () => {
    setToken(null)
    setUser(null)
    try {
      localStorage.removeItem('pfe_token')
      localStorage.removeItem('pfe_user')
    } catch {
      // Ignorer
    }
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
