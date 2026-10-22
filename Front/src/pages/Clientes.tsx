import { useEffect, useState, useCallback } from 'react'
import { Plus, Search, Pencil, Trash2, Building2 } from 'lucide-react'
import api from '../services/api'
import { Modal } from '../components/Modal'
import { Pagination } from '../components/Pagination'
import { usePagination } from '../hooks/usePagination'
import type { Client } from '../types'
import styles from './Clientes.module.css'

export function Clientes() {
  const [clients, setClients] = useState<Client[]>([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<Client | null>(null)
  const [form, setForm] = useState({ name: '', contactEmail: '', phone: '', document: '', active: true })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    try { const { data } = await api.get<Client[]>('/v1/clients'); setClients(data) }
    catch { setClients([]) }
    finally { setLoading(false) }
  }, [])

  useEffect(() => { load() }, [load])

  const filtered = clients.filter(c =>
    !search ||
    c.name.toLowerCase().includes(search.toLowerCase()) ||
    c.contactEmail?.toLowerCase().includes(search.toLowerCase()) ||
    c.document?.includes(search)
  )

  const { page, paginated, total, goTo, reset } = usePagination(filtered, 10)
  const handleSearch = (v: string) => { setSearch(v); reset() }

  const openCreate = () => { setEditing(null); setForm({ name: '', contactEmail: '', phone: '', document: '', active: true }); setError(''); setShowModal(true) }
  const openEdit = (c: Client) => {
    setEditing(c)
    setForm({ name: c.name, contactEmail: c.contactEmail ?? '', phone: c.phone ?? '', document: c.document ?? '', active: c.active })
    setError(''); setShowModal(true)
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Deseja excluir este cliente?')) return
    try { await api.delete(`/v1/clients/${id}`); setClients(prev => prev.filter(c => c.id !== id)) }
    catch { alert('Erro ao excluir cliente.') }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.name) { setError('Nome é obrigatório.'); return }
    setSubmitting(true)
    try {
      if (editing) await api.put(`/v1/clients/${editing.id}`, { ...form, id: editing.id })
      else await api.post('/v1/clients', form)
      setShowModal(false); await load()
    } catch { setError('Erro ao salvar cliente.') }
    finally { setSubmitting(false) }
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}><Building2 size={22} /> Clientes</h1>
        <button className={styles.newBtn} onClick={openCreate}><Plus size={16} /> Novo cliente</button>
      </div>

      <div className={styles.searchRow}>
        <div className={styles.searchWrap}>
          <Search size={15} className={styles.searchIcon} />
          <input type="text" className={styles.searchInput} placeholder="Pesquisar por nome, e-mail ou CNPJ..."
            value={search} onChange={e => handleSearch(e.target.value)} aria-label="Pesquisar clientes" />
        </div>
      </div>

      <div className={styles.tableWrap}>
        {loading ? <div className={styles.loading}>Carregando...</div> : (
          <>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th scope="col">Nome</th>
                  <th scope="col">E-mail de contato</th>
                  <th scope="col">Telefone</th>
                  <th scope="col">CNPJ/CPF</th>
                  <th scope="col">Situação</th>
                  <th scope="col">Ações</th>
                </tr>
              </thead>
              <tbody>
                {paginated.length === 0 ? (
                  <tr><td colSpan={6} style={{ textAlign: 'center', color: '#9ca3af', padding: '32px' }}>Nenhum cliente encontrado.</td></tr>
                ) : paginated.map(c => (
                  <tr key={c.id}>
                    <td className={styles.nameCell}>{c.name}</td>
                    <td>{c.contactEmail || '—'}</td>
                    <td>{c.phone || '—'}</td>
                    <td style={{ fontFamily: 'monospace', fontSize: '12px' }}>{c.document || '—'}</td>
                    <td>
                      <span className={[styles.badge, c.active ? styles.active : styles.inactive].join(' ')}>
                        {c.active ? 'Ativo' : 'Inativo'}
                      </span>
                    </td>
                    <td className={styles.actions}>
                      <button className={styles.editBtn} onClick={() => openEdit(c)}><Pencil size={14} /> Editar</button>
                      <button className={styles.deleteBtn} onClick={() => handleDelete(c.id)}><Trash2 size={14} /> Excluir</button>
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
        <Modal title={editing ? 'Editar Cliente' : 'Novo Cliente'} onClose={() => setShowModal(false)}>
          <form onSubmit={handleSubmit} className={styles.form} noValidate>
            <div className={styles.formField}>
              <label htmlFor="c-name" className={styles.formLabel}>Nome *</label>
              <input id="c-name" type="text" className={styles.formInput} value={form.name}
                onChange={e => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div className={styles.formField}>
              <label htmlFor="c-email" className={styles.formLabel}>E-mail de contato</label>
              <input id="c-email" type="email" className={styles.formInput} value={form.contactEmail}
                onChange={e => setForm({ ...form, contactEmail: e.target.value })} />
            </div>
            <div className={styles.formField}>
              <label htmlFor="c-phone" className={styles.formLabel}>Telefone</label>
              <input id="c-phone" type="text" className={styles.formInput} value={form.phone}
                onChange={e => setForm({ ...form, phone: e.target.value })} placeholder="(00) 0000-0000" />
            </div>
            <div className={styles.formField}>
              <label htmlFor="c-doc" className={styles.formLabel}>CNPJ / CPF</label>
              <input id="c-doc" type="text" className={styles.formInput} value={form.document}
                onChange={e => setForm({ ...form, document: e.target.value })} placeholder="00.000.000/0000-00" />
            </div>
            <div className={styles.formField}>
              <label className={styles.checkboxLabel}>
                <input type="checkbox" checked={form.active}
                  onChange={e => setForm({ ...form, active: e.target.checked })} />
                Cliente ativo
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
