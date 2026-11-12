import { Link } from 'react-router-dom'

export function NotFound() {
  return (
    <div style={{ textAlign: 'center', paddingTop: '80px' }}>
      <h1 style={{ fontSize: '64px', fontWeight: 800, color: '#4CAF50', margin: 0 }}>404</h1>
      <p style={{ fontSize: '18px', color: '#6b7280', marginBottom: '24px' }}>Página não encontrada</p>
      <Link
        to="/dashboard"
        style={{
          display: 'inline-block',
          padding: '10px 24px',
          background: '#4CAF50',
          color: 'white',
          borderRadius: '8px',
          fontWeight: 600,
          textDecoration: 'none',
        }}
      >
        Ir para o início
      </Link>
    </div>
  )
}
