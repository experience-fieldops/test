import { useState, useMemo } from 'react'

export function usePagination<T>(items: T[], perPage = 10) {
  const [page, setPage] = useState(1)

  // Reset to page 1 whenever the items list changes (e.g. after filter)
  const totalPages = Math.ceil(items.length / perPage)

  const paginated = useMemo(() => {
    const start = (page - 1) * perPage
    return items.slice(start, start + perPage)
  }, [items, page, perPage])

  function goTo(p: number) {
    setPage(Math.max(1, Math.min(p, totalPages || 1)))
  }

  function reset() {
    setPage(1)
  }

  return { page, paginated, total: items.length, goTo, reset, totalPages }
}
