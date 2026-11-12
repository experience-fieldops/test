import api from './api'
import type { LoginCredentials, AuthUser } from '../types'

interface LoginResponse {
  token: string
  user: AuthUser
}

export const authService = {
  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    const { data } = await api.post<LoginResponse>('/public/auth/login', credentials)
    return data
  },

  logout() {
    localStorage.removeItem('fieldops_token')
    localStorage.removeItem('fieldops_user')
  },

  getStoredUser(): AuthUser | null {
    const raw = localStorage.getItem('fieldops_user')
    if (!raw) return null
    try {
      return JSON.parse(raw) as AuthUser
    } catch {
      return null
    }
  },

  setSession(token: string, user: AuthUser) {
    localStorage.setItem('fieldops_token', token)
    localStorage.setItem('fieldops_user', JSON.stringify(user))
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem('fieldops_token')
  },
}
