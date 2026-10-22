import api from './api'
import type { User } from '../types'

export const userService = {
  async getAll(): Promise<User[]> {
    const { data } = await api.get<User[]>('/user')
    return data
  },

  async create(user: Omit<User, 'id' | 'createdAt' | 'updatedAt'> & { password: string }): Promise<User> {
    const payload = {
      name: user.name,
      email: user.email,
      passwordHash: user.password,
      role: user.role,
      active: user.active ?? true,
    }
    const { data } = await api.post<User>('/user', payload)
    return data
  },

  async update(id: number, user: Partial<User>): Promise<User> {
    const { data } = await api.put<User>(`/user/${id}`, { ...user, id })
    return data
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/user/${id}`)
  },
}
