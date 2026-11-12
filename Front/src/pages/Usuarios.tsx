import { useEffect, useState, useCallback } from 'react'
import { Plus, Search, Filter, Pencil, Trash2, X } from 'lucide-react'
import { userService } from '../services/userService'
import { Modal } from '../components/Modal'
import { Pagination } from '../components/Pagination'
import { usePagination } from '../hooks/usePagination'
import type { User, UserRole } from '../types'
import styles from './Usuarios.module.css'

const roleOptions: { value: UserRole; label: string }[] = [
  { value: 'ADMIN',    label: 'Admin' },
  { value: 'MANAGER',  label: 'Gerente' },
  { value: 'INSPECTOR',label: 'Inspetor' },
  { value: 'REVIEWER', label: 'Revisor' },
]

const roleLabel: Record<string, string> = {
  ADMIN: 'Admin', MANAGER: 'Gerente', INSPECTOR: 'Inspetor', REVIEWER: 'Revisor',
}

interface UserForm {
  name: string; email: string; password: string; role: UserRole; active: boolean
}
const emptyForm: UserForm = { name: '', email: '', password: '', role: 'INSPECTOR', active: true }

export function Usuarios() {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [filterRole, setFilterRole] = useState('')
  const [filterStatus, setFilterStatus] = useState('')
  const [showFilters, setShowFilters] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<User | null>(null)
  const [form, setForm] = useState<UserForm>(emptyForm)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    try { setUsers(await userService.getAll()) }
    catch { setUsers([]) }
    finally { setLoading(false) }
  }, [])

  useEffect(() => { load() }, [load])

  const filtered = users.filter((u) => {
    const s = search.toLowerCase()
    const matchSearch = !s || u.name.toLowerCase().includes(s) || u.email.toLowerCase().includes(s)
    const matchRole   = !filterRole   || u.role === filterRole
    const matchStatus = !filterStatus || (filterStatus === 'ativo' ? u.active : !u.active)
    return matchSearch && matchRole && matchStatus
  })

  const { page, paginated, total, goTo, reset } = usePagination(filtered, 10)

  // Reset page when filters change
  const handleSearch = (v: string) => { setSearch(v); reset() }
  const handleRole   = (v: string) => { setFilterRole(v); reset() }
  const handleStatus = (v: string) => { setFilterStatus(v); reset() }

  const openCreate = () => { setEditing(null); setForm(emptyForm); setError(''); setShowModal(true) }
  const openEdit   = (u: User) => {
    setEditing(u)
    setForm({ name: u.name, email: u.email, password: '', role: u.role, active: u.active })
    setError(''); setShowModal(true)
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Deseja excluir este usuário?')) return
    try { await userService.delete(id); setUsers(prev => prev.filter(u => u.id !== id)) }
    catch { alert('Erro ao excluir usuário.') }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); setError('')
    if (!form.name || !form.email) { setError('Nome e e-mail são obrigatórios.'); return }
    if (!editing && !form.password) { setError('Senha é obrigatória.'); return }
    setSubmitting(true)
    try {
      if (editing) await userService.update(editing.id, { name: form.name, email: form.email, role: form.role, active: form.active })
      else await userService.create(form)
      setShowModal(false); await load()
    } catch { setError('Erro ao salvar usuário.') }
    finally { setSubmitting(false) }
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}>Usuários</h1>
        <button className={styles.newBtn} onClick={openCreate}>
          <Plus size={16} /> Criar usuário
        </button>
      </div>

      {/* Busca rápida */}
      <div className={styles.novoCard}>
        <span className={styles.novoLabel}>Pesquisar</span>
        <input
          type="text" className={styles.novoInput}
          placeholder="Nome ou e-mail..."
          value={search} onChange={e => handleSearch(e.target.value)}
          aria-label="Busca rápida"
        />
      </div>

      {/* Filtros */}
      <div className={styles.filterBar}>
        <button className={styles.filterToggle} onClick={() => setShowFilters(v => !v)} aria-expanded={showFilters}>
          <Filter size={15} /> Filtro
        </button>
        {showFilters && (
          <div className={styles.filters}>
            <div className={styles.searchWrap}>
              <Search size={15} className={styles.searchIcon} />
              <input type="text" className={styles.searchInput}
                placeholder="Pesquisar por nome ou e-mail..."
                value={search} onChange={e => handleSearch(e.target.value)} />
              {search && (
                <button className={styles.clearBtn} onClick={() => handleSearch('')} aria-label="Limpar">
                  <X size={14} />
                </button>
              )}
            </div>
            <select className={styles.select} value={filterRole} onChange={e => handleRole(e.target.value)}>
              <option value="">Todos os perfis</option>
              {roleOptions.map(r => <option key={r.value} value={r.value}>{r.label}</option>)}
            </select>
            <select className={styles.select} value={filterStatus} onChange={e => handleStatus(e.target.value)}>
              <option value="">Todos os status</option>
              <option value="ativo">Ativo</option>
              <option value="inativo">Inativo</option>
            </select>
          </div>
        )}
      </div>

      {/* Tabela */}
      <div className={styles.tableWrap}>
        {loading ? (
          <div className={styles.loading}>Carregando...</div>
        ) : (
          <>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th scope="col">Nome</th>
                  <th scope="col">E-mail</th>
                  <th scope="col">Perfil</th>
                  <th scope="col">Situação</th>
                  <th scope="col">Último acesso</th>
                  <th scope="col">Ações</th>
                </tr>
              </thead>
              <tbody>
                {paginated.length === 0 ? (
                  <tr><td colSpan={6} style={{ textAlign: 'center', color: '#9ca3af', padding: '32px' }}>Nenhum usuário encontrado.</td></tr>
                ) : paginated.map(u => (
                  <tr key={u.id}>
                    <td className={styles.nameCell}>{u.name}</td>
                    <td>{u.email}</td>
                    <td>
                      <span className={[styles.badge, styles[`role${u.role}`]].join(' ')}>
                        {roleLabel[u.role] ?? u.role}
                      </span>
                    </td>
                    <td>
                      <span className={[styles.badge, u.active ? styles.active : styles.inactive].join(' ')}>
                        {u.active ? 'Ativo' : 'Inativo'}
                      </span>
                    </td>
                    <td>{u.updatedAt ? new Date(u.updatedAt).toLocaleDateString('pt-BR') : '—'}</td>
                    <td className={styles.actions}>
                      <button className={styles.editBtn} onClick={() => openEdit(u)} aria-label={`Editar ${u.name}`}>
                        <Pencil size={15} /> Editar
                      </button>
                      <button className={styles.deleteBtn} onClick={() => handleDelete(u.id)} aria-label={`Excluir ${u.name}`}>
                        <Trash2 size={15} /> Excluir
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Pagination total={total} page={page} perPage={10} onPageChange={goTo} />
          </>
        )}
      </div>

      {showModal && (
        <Modal title={editing ? 'Editar Usuário' : 'Novo Usuário'} onClose={() => setShowModal(false)}>
          <form onSubmit={handleSubmit} className={styles.form} noValidate>
            <div className={styles.formField}>
              <label htmlFor="u-name" className={styles.formLabel}>Nome *</label>
              <input id="u-name" type="text" className={styles.formInput} value={form.name}
                onChange={e => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div className={styles.formField}>
              <label htmlFor="u-email" className={styles.formLabel}>E-mail *</label>
              <input id="u-email" type="email" className={styles.formInput} value={form.email}
                onChange={e => setForm({ ...form, email: e.target.value })} required />
            </div>
            {!editing && (
              <div className={styles.formField}>
                <label htmlFor="u-password" className={styles.formLabel}>Senha *</label>
                <input id="u-password" type="password" className={styles.formInput} value={form.password}
                  onChange={e => setForm({ ...form, password: e.target.value })} required />
              </div>
            )}
            <div className={styles.formField}>
              <label htmlFor="u-role" className={styles.formLabel}>Perfil</label>
              <select id="u-role" className={styles.formInput} value={form.role}
                onChange={e => setForm({ ...form, role: e.target.value as UserRole })}>
                {roleOptions.map(r => <option key={r.value} value={r.value}>{r.label}</option>)}
              </select>
            </div>
            <div className={styles.formField}>
              <label className={styles.checkboxLabel}>
                <input type="checkbox" checked={form.active}
                  onChange={e => setForm({ ...form, active: e.target.checked })} />
                Usuário ativo
              </label>
            </div>
            {error && <p className={styles.formError} role="alert">{error}</p>}
            <div className={styles.formActions}>
              <button type="button" className={styles.cancelBtn} onClick={() => setShowModal(false)}>Cancelar</button>
              <button type="submit" className={styles.submitBtn} disabled={submitting}>
                {submitting ? 'Salvando...' : editing ? 'Salvar' : 'Criar'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
