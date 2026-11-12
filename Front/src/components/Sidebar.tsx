import { NavLink } from 'react-router-dom'
import { LayoutDashboard, Users, Building2, MapPin, Monitor, LogOut } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import styles from './Sidebar.module.css'

const navItems = [
  { to: '/dashboard',    icon: LayoutDashboard, label: 'Visão Geral' },
  { to: '/usuarios',     icon: Users,           label: 'Usuários' },
  { to: '/clientes',     icon: Building2,       label: 'Clientes' },
  { to: '/locais',       icon: MapPin,          label: 'Locais' },
  { to: '/equipamentos', icon: Monitor,         label: 'Equipamentos' },
]

export function Sidebar() {
  const { user, logout } = useAuth()

  return (
    <aside className={styles.sidebar} role="navigation" aria-label="Menu principal">

      {/* Logo */}
      <div className={styles.logoWrap}>
        <svg className={styles.logoSvg} viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
          <rect x="3" y="5" width="24" height="28" rx="3" stroke="#ffffff" strokeWidth="2" fill="none"/>
          <rect x="12" y="2" width="8" height="7" rx="2" stroke="#ffffff" strokeWidth="1.8" fill="none"/>
          <polyline points="8,14 10,17 14,12" stroke="#4CAF50" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
          <line x1="17" y1="14" x2="23" y2="14" stroke="#ffffff" strokeWidth="1.5" strokeLinecap="round"/>
          <polyline points="8,21 10,24 14,19" stroke="#4CAF50" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
          <line x1="17" y1="21" x2="23" y2="21" stroke="#ffffff" strokeWidth="1.5" strokeLinecap="round"/>
          {/* Gear */}
          <circle cx="27" cy="27" r="8" fill="#1b2a3b"/>
          <circle cx="27" cy="27" r="3.2" stroke="#4CAF50" strokeWidth="1.8" fill="none"/>
          <rect x="25.8" y="19.5" width="2.4" height="3" rx="1.2" fill="#4CAF50"/>
          <rect x="25.8" y="31.5" width="2.4" height="3" rx="1.2" fill="#4CAF50"/>
          <rect x="19.5" y="25.8" width="3" height="2.4" rx="1.2" fill="#4CAF50"/>
          <rect x="31.5" y="25.8" width="3" height="2.4" rx="1.2" fill="#4CAF50"/>
        </svg>
        <div className={styles.logoText}>
          <span className={styles.logoField}>
            FIELD<span className={styles.logoOps}>OPS</span>
          </span>
          <span className={styles.logoTagline}>Plataforma de Inspeção em Campo</span>
        </div>
      </div>

      {/* Nav */}
      <nav className={styles.nav}>
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              [styles.navItem, isActive ? styles.active : ''].join(' ')
            }
          >
            <Icon size={16} aria-hidden="true" />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className={styles.footer}>
        <div className={styles.userRow}>
          <div className={styles.avatar} aria-hidden="true">
            {user?.name?.charAt(0).toUpperCase() ?? 'A'}
          </div>
          <div className={styles.userDetails}>
            <span className={styles.userName}>{user?.name ?? 'Administrator'}</span>
            <span className={styles.userEmail}>{user?.email ?? ''}</span>
          </div>
        </div>
        <button
          className={styles.logoutBtn}
          onClick={logout}
          aria-label="Sair do sistema"
        >
          <LogOut size={15} aria-hidden="true" />
          <span>Sair</span>
        </button>
      </div>
    </aside>
  )
}
