import styles from './Logo.module.css'

interface LogoProps {
  size?: 'sm' | 'md' | 'lg'
  variant?: 'dark' | 'light'
}

export function Logo({ size = 'md', variant = 'dark' }: LogoProps) {
  const cls = [styles.logo, styles[size], styles[variant]].join(' ')
  return (
    <div className={cls}>
      <div className={styles.icon}>
        <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
          <rect x="4" y="4" width="32" height="32" rx="6" fill="#4CAF50" />
          <rect x="10" y="10" width="20" height="3" rx="1.5" fill="white" />
          <rect x="10" y="16" width="14" height="3" rx="1.5" fill="white" />
          <rect x="10" y="22" width="17" height="3" rx="1.5" fill="white" />
          <circle cx="30" cy="28" r="6" fill="#1e2a1e" />
          <path d="M27 28l2 2 4-4" stroke="#4CAF50" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </div>
      <div className={styles.text}>
        <span className={styles.brand}>FIELDOPS</span>
        <span className={styles.tagline}>Plataforma de Inspeção em Campo</span>
      </div>
    </div>
  )
}
