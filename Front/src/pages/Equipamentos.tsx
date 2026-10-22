import { useEffect, useState, useCallback } from 'react'
import { Plus, Search, Pencil, Trash2, Monitor } from 'lucide-react'
import api from '../services/api'
import { Modal } from '../components/Modal'
import { Pagination } from '../components/Pagination'
import { usePagination } from '../hooks/usePagination'
import type { Equipment, Location } from '../types'
import styles from './Clientes.module.css'

const statusLabel: Record<string, string> = {
  ATIVO: 'Ativo',
  EM_MANUTENCAO: 'Manutenção',
  INATIVO: 'Inativo',
}

export function Equipamentos() {
  const [equipamentos, setEquipamentos] = useState<Equipment[]>([])
  const [locais, setLocais] = useState<Location[]>([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [filterStatus, setFilterStatus] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<Equipment | null>(null)
  const [form, setForm] = useState({ name: '', serialNumber: '', qrCode: '', category: '', locationId: 0, status: 'ATIVO' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [eqRes, locRes] = await Promise.all([
        api.get<Equipment[]>('/v1/equipment'),
        api.get<Location[]>('/v1/locations'),
      ])
      setEquipamentos(eqRes.data); setLocais(locRes.data)
    } catch { setEquipamentos([]) }
    finally { setLoading(false) }
  }, [])

  useEffect(() => { load() }, [load])

  const filtered = equipamentos.filter(e =>
    (!search ||
      e.name.toLowerCase().includes(search.toLowerCase()) ||
      e.serialNumber?.toLowerCase().includes(search.toLowerCase()) ||
      e.locationName?.toLowerCase().includes(search.toLowerCase()) ||
      e.clientName?.toLowerCase().includes(search.toLowerCase())
    ) &&
    (!filterStatus || e.status === filterStatus)
  )

  const { page, paginated, total, goTo, reset } = usePagination(filtered, 10)
  const handleSearch = (v: string) => { setSearch(v); reset() }
  const handleStatus = (v: string) => { setFilterStatus(v); reset() }

  const openCreate = () => {
    setEditing(null)
    setForm({ name: '', serialNumber: '', qrCode: '', category: '', locationId: locais[0]?.id ?? 0, status: 'ATIVO' })
    setError(''); setShowModal(true)
  }
  const openEdit = (e: Equipment) => {
    setEditing(e)
    setForm({ name: e.name, serialNumber: e.serialNumber ?? '', qrCode: e.qrCode ?? '', category: e.category ?? '', locationId: e.locationId, status: e.status })
    setError(''); setShowModal(true)
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Deseja excluir este equipamento?')) return
    try { await api.delete(`/v1/equipment/${id}`); setEquipamentos(prev => prev.filter(e => e.id !== id)) }
    catch { alert('Erro ao excluir equipamento.') }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.name) { setError('Nome é obrigatório.'); return }
    setSubmitting(true)
    try {
      if (editing) await api.put(`/v1/equipment/${editing.id}`, { ...form, id: editing.id })
      else await api.post('/v1/equipment', form)
      setShowModal(false); await load()
    } catch { setError('Erro ao salvar equipamento.') }
    finally { setSubmitting(false) }
  }

  const badgeClass = (status: string) =>
    status === 'ATIVO' ? styles.active : status === 'EM_MANUTENCAO' ? styles.badgeMaint : styles.inactive

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}><Monitor size={22} /> Equipamentos</h1>
        <button className={styles.newBtn} onClick={openCreate}><Plus size={16} /> Novo equipamento</button>
      </div>

      <div className={styles.searchRow} style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
        <div className={styles.searchWrap} style={{ flex: 1 }}>
          <Search size={15} className={styles.searchIcon} />
          <input type="text" className={styles.searchInput}
            placeholder="Pesquisar por nome, série, local ou cliente..."
            value={search} onChange={e => handleSearch(e.target.value)} aria-label="Pesquisar equipamentos" />
        </div>
        <select className={styles.searchInput} style={{ maxWidth: '180px', paddingLeft: '12px' }}
          value={filterStatus} onChange={e => handleStatus(e.target.value)} aria-label="Filtrar por status">
          <option value="">Todos os status</option>
          <option value="ATIVO">Ativo</option>
          <option value="EM_MANUTENCAO">Em manutenção</option>
          <option value="INATIVO">Inativo</option>
        </select>
      </div>

      <div className={styles.tableWrap}>
        {loading ? <div className={styles.loading}>Carregando...</div> : (
          <>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th scope="col">Nome</th>
                  <th scope="col">Nº Série</th>
                  <th scope="col">Categoria</th>
                  <th scope="col">Local</th>
                  <th scope="col">Cliente</th>
                  <th scope="col">Status</th>
                  <th scope="col">Ações</th>
                </tr>
              </thead>
              <tbody>
                {paginated.length === 0 ? (
                  <tr><td colSpan={7} style={{ textAlign: 'center', color: '#9ca3af', padding: '32px' }}>Nenhum equipamento encontrado.</td></tr>
                ) : paginated.map(eq => (
                  <tr key={eq.id}>
                    <td className={styles.nameCell}>{eq.name}</td>
                    <td style={{ fontFamily: 'monospace', fontSize: '12px' }}>{eq.serialNumber || '—'}</td>
                    <td>{eq.category || '—'}</td>
                    <td>{eq.locationName || '—'}</td>
                    <td>{eq.clientName || '—'}</td>
                    <td>
                      <span className={[styles.badge, badgeClass(eq.status)].join(' ')}>
                        {statusLabel[eq.status] ?? eq.status}
                      </span>
                    </td>
                    <td className={styles.actions}>
                      <button className={styles.editBtn} onClick={() => openEdit(eq)}><Pencil size={14} /> Editar</button>
                      <button className={styles.deleteBtn} onClick={() => handleDelete(eq.id)}><Trash2 size={14} /> Excluir</button>
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
        <Modal title={editing ? 'Editar Equipamento' : 'Novo Equipamento'} onClose={() => setShowModal(false)}>
          <form onSubmit={handleSubmit} className={styles.form} noValidate>
            <div className={styles.formField}>
              <label htmlFor="eq-name" className={styles.formLabel}>Nome *</label>
              <input id="eq-name" type="text" className={styles.formInput} value={form.name}
                onChange={e => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div className={styles.formField}>
              <label htmlFor="eq-serial" className={styles.formLabel}>Número de série</label>
              <input id="eq-serial" type="text" className={styles.formInput} value={form.serialNumber}
                onChange={e => setForm({ ...form, serialNumber: e.target.value })} />
            </div>
            <div className={styles.formField}>
              <label htmlFor="eq-cat" className={styles.formLabel}>Categoria</label>
              <select id="eq-cat" className={styles.formInput} value={form.category}
                onChange={e => setForm({ ...form, category: e.target.value })}>
                <option value="">Selecione</option>
                {['Elétrico','Mecânico','HVAC','Hidráulico','Instrumentação','Segurança','Estrutural'].map(c =>
                  <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
            <div className={styles.formField}>
              <label htmlFor="eq-local" className={styles.formLabel}>Local</label>
              <select id="eq-local" className={styles.formInput} value={form.locationId}
                onChange={e => setForm({ ...form, locationId: Number(e.target.value) })}>
                <option value={0}>Selecione um local</option>
                {locais.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
              </select>
            </div>
            <div className={styles.formField}>
              <label htmlFor="eq-status" className={styles.formLabel}>Status</label>
              <select id="eq-status" className={styles.formInput} value={form.status}
                onChange={e => setForm({ ...form, status: e.target.value })}>
                <option value="ATIVO">Ativo</option>
                <option value="EM_MANUTENCAO">Em manutenção</option>
                <option value="INATIVO">Inativo</option>
              </select>
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
