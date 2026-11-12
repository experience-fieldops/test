import { useEffect, useState } from 'react'
import { Users, Building2, MapPin, Monitor, Plus, TrendingUp, CheckCircle2, Clock, AlertTriangle } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { userService } from '../services/userService'
import { Pagination } from '../components/Pagination'
import { usePagination } from '../hooks/usePagination'
import api from '../services/api'
import type { User, Client } from '../types'
import styles from './Dashboard.module.css'

const roleLabel: Record<string, string> = {
  ADMIN: 'Admin', MANAGER: 'Supervisor', INSPECTOR: 'Inspetor', REVIEWER: 'Revisor',
}

export function Dashboard() {
  const [users, setUsers]     = useState<User[]>([])
  const [clients, setClients] = useState<Client[]>([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    Promise.all([
      userService.getAll(),
      api.get<Client[]>('/v1/clients').then(r => r.data).catch(() => []),
    ]).then(([u, c]) => { setUsers(u); setClients(c) })
      .finally(() => setLoading(false))
  }, [])

  const { page, paginated, total, goTo } = usePagination(users, 10)

  const activeUsers    = users.filter(u => u.active).length
  const inactiveUsers  = users.filter(u => !u.active).length
  const activeClients  = clients.filter(c => c.active).length

  // Activity feed — derived from users (last updated)
  const recentActivity = [...users]
    .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
    .slice(0, 6)

  return (
    <div className={styles.page}>
      {/* ── Header ── */}
      <div className={styles.header}>
        <div>
          <h1 className={styles.pageTitle}>Visão Geral</h1>
          <p className={styles.pageSubtitle}>Acompanhe os dados da plataforma em tempo real</p>
        </div>
        <button className={styles.newBtn} onClick={() => navigate('/usuarios')}>
          <Plus size={15} /> Criar usuário
        </button>
      </div>

      {/* ── Stat cards ── */}
      <div className={styles.stats}>
        <div className={styles.statCard} onClick={() => navigate('/usuarios')} role="button" tabIndex={0}
          onKeyDown={e => e.key === 'Enter' && navigate('/usuarios')} aria-label="Ver usuários">
          <div className={styles.statTop}>
            <div className={[styles.statIcon, styles.iconGreen].join(' ')}><Users size={20} /></div>
            <span className={styles.statTrend}><TrendingUp size={13} /> +{users.length}</span>
          </div>
          <span className={styles.statValue}>{users.length}</span>
          <span className={styles.statLabel}>Usuários</span>
          <div className={styles.statBar}><div className={styles.statBarFill} style={{ width: '100%' }} /></div>
          <span className={styles.statSub}>{activeUsers} ativos · {inactiveUsers} inativos</span>
        </div>

        <div className={styles.statCard} onClick={() => navigate('/clientes')} role="button" tabIndex={0}
          onKeyDown={e => e.key === 'Enter' && navigate('/clientes')} aria-label="Ver clientes">
          <div className={styles.statTop}>
            <div className={[styles.statIcon, styles.iconGreen].join(' ')}><Building2 size={20} /></div>
            <span className={styles.statTrend}><TrendingUp size={13} /> +{clients.length}</span>
          </div>
          <span className={styles.statValue}>{clients.length}</span>
          <span className={styles.statLabel}>Clientes</span>
          <div className={styles.statBar}><div className={styles.statBarFill} style={{ width: `${clients.length > 0 ? (activeClients / clients.length) * 100 : 0}%` }} /></div>
          <span className={styles.statSub}>{activeClients} ativos</span>
        </div>

        <div className={styles.statCard} onClick={() => navigate('/locais')} role="button" tabIndex={0}
          onKeyDown={e => e.key === 'Enter' && navigate('/locais')} aria-label="Ver locais">
          <div className={styles.statTop}>
            <div className={[styles.statIcon, styles.iconGreen].join(' ')}><MapPin size={20} /></div>
            <span className={styles.statTrend}><TrendingUp size={13} /> cadastrados</span>
          </div>
          <span className={styles.statValue}>39</span>
          <span className={styles.statLabel}>Locais</span>
          <div className={styles.statBar}><div className={styles.statBarFill} style={{ width: '72%' }} /></div>
          <span className={styles.statSub}>Instalações cadastradas</span>
        </div>

        <div className={styles.statCard} onClick={() => navigate('/equipamentos')} role="button" tabIndex={0}
          onKeyDown={e => e.key === 'Enter' && navigate('/equipamentos')} aria-label="Ver equipamentos">
          <div className={styles.statTop}>
            <div className={[styles.statIcon, styles.iconGreen].join(' ')}><Monitor size={20} /></div>
            <span className={styles.statTrend}><TrendingUp size={13} /> cadastrados</span>
          </div>
          <span className={styles.statValue}>58</span>
          <span className={styles.statLabel}>Equipamentos</span>
          <div className={styles.statBar}><div className={styles.statBarFill} style={{ width: '88%' }} /></div>
          <span className={styles.statSub}>Ativos em campo</span>
        </div>
      </div>

      {/* ── Status rápido ── */}
      <div className={styles.quickStatus}>
        <div className={[styles.qsCard, styles.qsGreen].join(' ')}>
          <CheckCircle2 size={18} />
          <span><strong>{activeUsers}</strong> usuários ativos</span>
        </div>
        <div className={[styles.qsCard, styles.qsYellow].join(' ')}>
          <Clock size={18} />
          <span><strong>{activeClients}</strong> clientes ativos</span>
        </div>
        <div className={[styles.qsCard, styles.qsRed].join(' ')}>
          <AlertTriangle size={18} />
          <span><strong>{inactiveUsers}</strong> usuários inativos</span>
        </div>
        <div className={[styles.qsCard, styles.qsGreen].join(' ')}>
          <Monitor size={18} />
          <span><strong>58</strong> equipamentos em campo</span>
        </div>
      </div>

      {/* ── Layout de duas colunas ── */}
      <div className={styles.grid}>
        {/* Tabela de usuários */}
        <div className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}><Users size={16} /> Usuários</h2>
            <button className={styles.sectionBtn} onClick={() => navigate('/usuarios')}>
              <Plus size={13} /> Criar usuário
            </button>
          </div>

          {loading ? (
            <div className={styles.loading}>Carregando...</div>
          ) : (
            <>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th scope="col">Nome</th>
                    <th scope="col">Perfil</th>
                    <th scope="col">Status</th>
                    <th scope="col">Ação</th>
                  </tr>
                </thead>
                <tbody>
                  {paginated.length === 0 ? (
                    <tr><td colSpan={4} className={styles.empty}>Nenhum usuário encontrado.</td></tr>
                  ) : paginated.map(u => (
                    <tr key={u.id}>
                      <td>
                        <div className={styles.userCell}>
                          <div className={styles.userAvatar}>{u.name.charAt(0).toUpperCase()}</div>
                          <div>
                            <div className={styles.nameCell}>{u.name}</div>
                            <div className={styles.emailCell}>{u.email}</div>
                          </div>
                        </div>
                      </td>
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
                      <td>
                        <button className={styles.editBtn} onClick={() => navigate('/usuarios')}>
                          Editar
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

        {/* Feed de atividade recente */}
        <div className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}><Clock size={16} /> Atividade Recente</h2>
          </div>
          <div className={styles.activityList}>
            {recentActivity.length === 0 ? (
              <div className={styles.empty} style={{ padding: '32px', textAlign: 'center' }}>Sem atividade.</div>
            ) : recentActivity.map(u => (
              <div key={u.id} className={styles.activityItem}>
                <div className={styles.activityAvatar}>{u.name.charAt(0).toUpperCase()}</div>
                <div className={styles.activityInfo}>
                  <span className={styles.activityName}>{u.name}</span>
                  <span className={styles.activityDetail}>
                    {roleLabel[u.role] ?? u.role} · {u.active ? 'Ativo' : 'Inativo'}
                  </span>
                </div>
                <span className={styles.activityDate}>
                  {u.updatedAt ? new Date(u.updatedAt).toLocaleDateString('pt-BR') : '—'}
                </span>
              </div>
            ))}
          </div>

          {/* Mini gráfico de perfis */}
          <div className={styles.profileChart}>
            <p className={styles.chartTitle}>Distribuição de perfis</p>
            {(['ADMIN','MANAGER','INSPECTOR','REVIEWER'] as const).map(role => {
              const count = users.filter(u => u.role === role).length
              const pct   = users.length > 0 ? Math.round((count / users.length) * 100) : 0
              return (
                <div key={role} className={styles.chartRow}>
                  <span className={styles.chartLabel}>{roleLabel[role]}</span>
                  <div className={styles.chartTrack}>
                    <div className={styles.chartBar} style={{ width: `${pct}%` }} />
                  </div>
                  <span className={styles.chartCount}>{count}</span>
                </div>
              )
            })}
          </div>
        </div>
      </div>
    </div>
  )
}
