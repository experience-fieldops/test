import { useEffect, useState, useCallback } from 'react'
import { Plus, Search, Pencil, Trash2, MapPin } from 'lucide-react'
import api from '../services/api'
import { Modal } from '../components/Modal'
import { Pagination } from '../components/Pagination'
import { usePagination } from '../hooks/usePagination'
import type { Location, Client } from '../types'
import styles from './Clientes.module.css'

export function Locais() {
  const [locations, setLocations] = useState<Location[]>([])
  const [clients, setClients] = useState<Client[]>([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<Location | null>(null)
  const [form, setForm] = useState({ name: '', address: '', city: '', state: '', clientId: 0, active: true })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [locRes, cliRes] = await Promise.all([
        api.get<Location[]>('/v1/locations'),
        api.get<Client[]>('/v1/clients'),
      ])
      setLocations(locRes.data); setClients(cliRes.data)
    } catch { setLocations([]) }
    finally { setLoading(false) }
  }, [])

  useEffect(() => { load() }, [load])

  const filtered = locations.filter(l =>
    !search ||
    l.name.toLowerCase().includes(search.toLowerCase()) ||
    l.clientName?.toLowerCase().includes(search.toLowerCase()) ||
    l.city?.toLowerCase().includes(search.toLowerCase())
  )

  const { page, paginated, total, goTo, reset } = usePagination(filtered, 10)
  const handleSearch = (v: string) => { setSearch(v); reset() }

  const openCreate = () => {
    setEditing(null)
    setForm({ name: '', address: '', city: '', state: '', clientId: clients[0]?.id ?? 0, active: true })
    setError(''); setShowModal(true)
  }
  const openEdit = (l: Location) => {
    setEditing(l)
    setForm({ name: l.name, address: l.address ?? '', city: l.city ?? '', state: l.state ?? '', clientId: l.clientId, active: l.active })
    setError(''); setShowModal(true)
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Deseja excluir este local?')) return
    try { await api.delete(`/v1/locations/${id}`); setLocations(prev => prev.filter(l => l.id !== id)) }
    catch { alert('Erro ao excluir local.') }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.name) { setError('Nome é obrigatório.'); return }
    setSubmitting(true)
    try {
      if (editing) await api.put(`/v1/locations/${editing.id}`, { ...form, id: editing.id })
      else await api.post('/v1/locations', form)
      setShowModal(false); await load()
    } catch { setError('Erro ao salvar local.') }
    finally { setSubmitting(false) }
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}><MapPin size={22} /> Locais</h1>
        <button className={styles.newBtn} onClick={openCreate}><Plus size={16} /> Novo local</button>
      </div>

      <div className={styles.searchRow}>
        <div className={styles.searchWrap}>
          <Search size={15} className={styles.searchIcon} />
          <input type="text" className={styles.searchInput} placeholder="Pesquisar por nome, cliente ou cidade..."
            value={search} onChange={e => handleSearch(e.target.value)} aria-label="Pesquisar locais" />
        </div>
      </div>

      <div className={styles.tableWrap}>
        {loading ? <div className={styles.loading}>Carregando...</div> : (
          <>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th scope="col">Nome</th>
                  <th scope="col">Cidade / UF</th>
                  <th scope="col">Endereço</th>
                  <th scope="col">Cliente</th>
                  <th scope="col">Situação</th>
                  <th scope="col">Ações</th>
                </tr>
              </thead>
              <tbody>
                {paginated.length === 0 ? (
                  <tr><td colSpan={6} style={{ textAlign: 'center', color: '#9ca3af', padding: '32px' }}>Nenhum local encontrado.</td></tr>
                ) : paginated.map(l => (
                  <tr key={l.id}>
                    <td className={styles.nameCell}>{l.name}</td>
                    <td>{l.city ? `${l.city}${l.state ? ` / ${l.state}` : ''}` : '—'}</td>
                    <td>{l.address || '—'}</td>
                    <td>{l.clientName || '—'}</td>
                    <td>
                      <span className={[styles.badge, l.active ? styles.active : styles.inactive].join(' ')}>
                        {l.active ? 'Ativo' : 'Inativo'}
                      </span>
                    </td>
                    <td className={styles.actions}>
                      <button className={styles.editBtn} onClick={() => openEdit(l)}><Pencil size={14} /> Editar</button>
                      <button className={styles.deleteBtn} onClick={() => handleDelete(l.id)}><Trash2 size={14} /> Excluir</button>
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
        <Modal title={editing ? 'Editar Local' : 'Novo Local'} onClose={() => setShowModal(false)}>
          <form onSubmit={handleSubmit} className={styles.form} noValidate>
            <div className={styles.formField}>
              <label htmlFor="l-name" className={styles.formLabel}>Nome *</label>
              <input id="l-name" type="text" className={styles.formInput} value={form.name}
                onChange={e => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div className={styles.formField}>
              <label htmlFor="l-address" className={styles.formLabel}>Endereço</label>
              <input id="l-address" type="text" className={styles.formInput} value={form.address}
                onChange={e => setForm({ ...form, address: e.target.value })} />
            </div>
            <div className={styles.formField}>
              <label htmlFor="l-city" className={styles.formLabel}>Cidade</label>
              <input id="l-city" type="text" className={styles.formInput} value={form.city}
                onChange={e => setForm({ ...form, city: e.target.value })} />
            </div>
            <div className={styles.formField}>
              <label htmlFor="l-state" className={styles.formLabel}>UF</label>
              <input id="l-state" type="text" className={styles.formInput} value={form.state}
                onChange={e => setForm({ ...form, state: e.target.value })} maxLength={2} placeholder="SP" />
            </div>
            <div className={styles.formField}>
              <label htmlFor="l-client" className={styles.formLabel}>Cliente</label>
              <select id="l-client" className={styles.formInput} value={form.clientId}
                onChange={e => setForm({ ...form, clientId: Number(e.target.value) })}>
                <option value={0}>Selecione um cliente</option>
                {clients.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
            <div className={styles.formField}>
              <label className={styles.checkboxLabel}>
                <input type="checkbox" checked={form.active}
                  onChange={e => setForm({ ...form, active: e.target.checked })} />
                Local ativo
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
