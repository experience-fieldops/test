import styles from './Pagination.module.css'
import { ChevronLeft, ChevronRight } from 'lucide-react'

interface PaginationProps {
  total: number
  page: number
  perPage: number
  onPageChange: (page: number) => void
}

export function Pagination({ total, page, perPage, onPageChange }: PaginationProps) {
  const totalPages = Math.ceil(total / perPage)
  if (totalPages <= 1) return null

  const getPages = (): (number | '...')[] => {
    const pages: (number | '...')[] = []
    if (totalPages <= 7) {
      for (let i = 1; i <= totalPages; i++) pages.push(i)
    } else {
      pages.push(1)
      if (page > 4) pages.push('...')
      for (let i = Math.max(2, page - 1); i <= Math.min(totalPages - 1, page + 1); i++) pages.push(i)
      if (page < totalPages - 3) pages.push('...')
      pages.push(totalPages)
    }
    return pages
  }

  const from = (page - 1) * perPage + 1
  const to = Math.min(page * perPage, total)

  return (
    <div className={styles.wrapper}>
      <span className={styles.info}>
        Mostrando <strong>{from}–{to}</strong> de <strong>{total}</strong>
      </span>
      <div className={styles.controls}>
        <button
          className={styles.btn}
          onClick={() => onPageChange(page - 1)}
          disabled={page === 1}
          aria-label="Página anterior"
        >
          <ChevronLeft size={16} />
        </button>

        {getPages().map((p, i) =>
          p === '...' ? (
            <span key={`dots-${i}`} className={styles.dots}>…</span>
          ) : (
            <button
              key={p}
              className={[styles.btn, p === page ? styles.active : ''].join(' ')}
              onClick={() => onPageChange(p as number)}
              aria-label={`Página ${p}`}
              aria-current={p === page ? 'page' : undefined}
            >
              {p}
            </button>
          )
        )}

        <button
          className={styles.btn}
          onClick={() => onPageChange(page + 1)}
          disabled={page === totalPages}
          aria-label="Próxima página"
        >
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  )
}
