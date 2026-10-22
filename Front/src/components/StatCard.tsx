import type { LucideIcon } from 'lucide-react'
import styles from './StatCard.module.css'

interface StatCardProps {
  label: string
  value: number | string
  icon: LucideIcon
  color?: 'green' | 'blue' | 'yellow' | 'purple'
}

export function StatCard({ label, value, icon: Icon, color = 'green' }: StatCardProps) {
  return (
    <div className={[styles.card, styles[color]].join(' ')}>
      <div className={styles.iconWrap}>
        <Icon size={22} aria-hidden="true" />
      </div>
      <div className={styles.info}>
        <span className={styles.value}>{value}</span>
        <span className={styles.label}>{label}</span>
      </div>
    </div>
  )
}
