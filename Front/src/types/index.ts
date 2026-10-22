export type UserRole = 'ADMIN' | 'MANAGER' | 'INSPECTOR' | 'REVIEWER'

export interface User {
  id: number
  name: string
  email: string
  role: UserRole
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface Client {
  id: number
  name: string
  contactEmail: string
  phone?: string
  document?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface Location {
  id: number
  name: string
  address?: string
  city?: string
  state?: string
  zipCode?: string
  clientId: number
  clientName?: string
  active: boolean
  createdAt?: string
  updatedAt?: string
}

export interface Equipment {
  id: number
  name: string
  serialNumber?: string
  qrCode?: string
  category?: string
  status: string
  locationId: number
  locationName?: string
  clientName?: string
  createdAt?: string
  updatedAt?: string
}

export interface AuthUser {
  id: number
  name: string
  email: string
  role: UserRole
}

export interface LoginCredentials {
  email: string
  password: string
}
