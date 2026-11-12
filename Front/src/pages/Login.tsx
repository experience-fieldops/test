import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Eye, EyeOff, Mail, Lock } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import styles from './Login.module.css'

export function Login() {
  const { login, isLoading } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(true)
  const [error, setError] = useState('')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    try {
      await login({ email, password })
      navigate('/dashboard')
    } catch {
      setError('E-mail ou senha inválidos. Tente novamente.')
    }
  }

  return (
    <div className={styles.page}>

      {/* ── Painel esquerdo ── */}
      <div className={styles.leftPanel}>
        {/* Ícone clipboard + engrenagem */}
        <svg
          className={styles.logoIcon}
          viewBox="0 0 120 120"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          aria-hidden="true"
        >
          {/* Clipboard body */}
          <rect x="14" y="22" width="72" height="86" rx="6" fill="none" stroke="#ffffff" strokeWidth="3.5"/>
          {/* Clip top */}
          <rect x="36" y="14" width="28" height="16" rx="4" fill="none" stroke="#ffffff" strokeWidth="3"/>
          {/* Check lines */}
          <polyline points="26,50 32,56 42,44" stroke="#4CAF50" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/>
          <line x1="50" y1="50" x2="76" y2="50" stroke="#ffffff" strokeWidth="2.5" strokeLinecap="round"/>
          <polyline points="26,68 32,74 42,62" stroke="#4CAF50" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/>
          <line x1="50" y1="68" x2="76" y2="68" stroke="#ffffff" strokeWidth="2.5" strokeLinecap="round"/>
          <polyline points="26,86 32,92 42,80" stroke="#4CAF50" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/>
          <line x1="50" y1="86" x2="68" y2="86" stroke="#ffffff" strokeWidth="2.5" strokeLinecap="round"/>
          {/* Gear */}
          <circle cx="88" cy="90" r="26" fill="#1b2a3b"/>
          <circle cx="88" cy="90" r="10" stroke="#4CAF50" strokeWidth="3.5" fill="none"/>
          {/* Gear teeth */}
          <rect x="85.5" y="68" width="5" height="8" rx="2.5" fill="#4CAF50"/>
          <rect x="85.5" y="104" width="5" height="8" rx="2.5" fill="#4CAF50"/>
          <rect x="66" y="87.5" width="8" height="5" rx="2.5" fill="#4CAF50"/>
          <rect x="102" y="87.5" width="8" height="5" rx="2.5" fill="#4CAF50"/>
          <rect x="71.5" y="73.5" width="5" height="8" rx="2.5" transform="rotate(45 71.5 73.5)" fill="#4CAF50"/>
          <rect x="100" y="98.5" width="5" height="8" rx="2.5" transform="rotate(45 100 98.5)" fill="#4CAF50"/>
          <rect x="100" y="73.5" width="5" height="8" rx="2.5" transform="rotate(-45 100 73.5)" fill="#4CAF50"/>
          <rect x="71.5" y="98.5" width="5" height="8" rx="2.5" transform="rotate(-45 71.5 98.5)" fill="#4CAF50"/>
        </svg>

        <div className={styles.logoTextGroup}>
          <div className={styles.logoName}>
            <span className={styles.logoNameField}>FIELD</span>
            <span className={styles.logoNameOps}>OPS</span>
          </div>
          <span className={styles.logoTagline}>Plataforma de Inspeção em Campo</span>
        </div>
      </div>

      {/* ── Painel direito ── */}
      <div className={styles.rightPanel}>
        <div className={styles.formBox}>
          <h1 className={styles.title}>Seja Bem-Vindo(a)!</h1>
          <p className={styles.subtitle}>Realize o login para continuar</p>

          <form onSubmit={handleSubmit} noValidate className={styles.form}>

            {/* E-mail */}
            <div className={styles.inputWrap}>
              <span className={styles.inputIcon}>
                <Mail size={17} />
              </span>
              <input
                id="email"
                type="email"
                className={styles.input}
                placeholder="E-MAIL"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
                aria-label="E-mail"
              />
            </div>

            {/* Senha */}
            <div className={styles.inputWrap}>
              <span className={styles.inputIcon}>
                <Lock size={17} />
              </span>
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                className={styles.input}
                placeholder="SENHA"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
                aria-label="Senha"
              />
              <button
                type="button"
                className={styles.eyeBtn}
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'}
              >
                {showPassword ? <Eye size={17} /> : <EyeOff size={17} />}
              </button>
            </div>

            {error && (
              <p className={styles.error} role="alert">{error}</p>
            )}

            {/* Lembrar-me */}
            <div className={styles.rememberRow}>
              <label className={styles.toggleLabel}>
                <div
                  className={[styles.toggle, rememberMe ? styles.toggleOn : ''].join(' ')}
                  role="switch"
                  aria-checked={rememberMe}
                  tabIndex={0}
                  onClick={() => setRememberMe((v) => !v)}
                  onKeyDown={(e) => {
                    if (e.key === ' ' || e.key === 'Enter') setRememberMe((v) => !v)
                  }}
                >
                  <div className={styles.toggleThumb} />
                </div>
                <span>Lembrar-me</span>
              </label>
            </div>

            <button
              type="submit"
              className={styles.submitBtn}
              disabled={isLoading}
            >
              {isLoading ? 'Entrando...' : 'LOGAR'}
            </button>
          </form>

          <a href="#" className={styles.forgotLink}>
            Esqueci minha senha
          </a>
        </div>
      </div>
    </div>
  )
}
