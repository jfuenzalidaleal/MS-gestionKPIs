import { useEffect, useState } from 'react'
import { getDefiniciones, getSucursales, getMetricas, createMetrica, deleteMetrica } from '../api/kpiApi'
import Modal from '../components/Modal'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ReferenceLine, ResponsiveContainer,
} from 'recharts'
import { Plus, Trash2, BarChart3 } from 'lucide-react'

export default function KpiMetricas() {
  const [definiciones, setDefiniciones] = useState([])
  const [sucursales, setSucursales] = useState([])
  const [selectedDef, setSelectedDef] = useState('')
  const [metricas, setMetricas] = useState([])
  const [loading, setLoading] = useState(false)
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState({ valorActual: '', sucursalId: '' })
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    Promise.all([getDefiniciones(), getSucursales()]).then(([defs, sucs]) => {
      setDefiniciones(defs)
      setSucursales(sucs)
      if (defs.length > 0) setSelectedDef(String(defs[0].id))
    })
  }, [])

  useEffect(() => {
    if (!selectedDef) return
    setLoading(true)
    getMetricas(selectedDef).then(setMetricas).finally(() => setLoading(false))
  }, [selectedDef])

  const defActual = definiciones.find(d => String(d.id) === selectedDef)

  const chartData = metricas.map(m => ({
    fecha: new Date(m.fechaRegistro).toLocaleDateString('es-CL', { day: '2-digit', month: 'short' }),
    valor: m.valorActual,
    sucursal: m.sucursal?.nombre || 'Sin sucursal',
  }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      await createMetrica({
        valorActual: Number(form.valorActual),
        definicion: { id: Number(selectedDef) },
        sucursal: form.sucursalId ? { id: Number(form.sucursalId) } : null,
      })
      setForm({ valorActual: '', sucursalId: '' })
      setModal(false)
      getMetricas(selectedDef).then(setMetricas)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id) => {
    if (!confirm('¿Eliminar esta métrica?')) return
    await deleteMetrica(id)
    getMetricas(selectedDef).then(setMetricas)
  }

  return (
    <div className="flex-1 p-8 space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-100">Métricas</h1>
          <p className="text-sm text-slate-500 mt-1">Historial de valores registrados por KPI</p>
        </div>
        <button className="btn-primary" onClick={() => setModal(true)} disabled={!selectedDef}>
          <Plus size={16} /> Registrar Métrica
        </button>
      </div>

      {/* KPI Selector */}
      <div className="card !py-4">
        <label className="label mb-2">Seleccionar KPI</label>
        <select
          value={selectedDef}
          onChange={e => setSelectedDef(e.target.value)}
          className="input max-w-sm"
        >
          {definiciones.map(d => (
            <option key={d.id} value={d.id}>{d.nombre}</option>
          ))}
        </select>
      </div>

      {/* Chart */}
      {defActual && (
        <div className="card">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-sm font-semibold text-slate-300">{defActual.nombre}</h2>
            <span className="badge bg-accent-500/10 text-accent-400 border border-accent-500/20">
              Objetivo: {defActual.valorObjetivo ?? '—'} {defActual.unidad || ''}
            </span>
          </div>
          {loading ? (
            <div className="flex items-center justify-center h-48">
              <div className="w-6 h-6 border-2 border-accent-500 border-t-transparent rounded-full animate-spin" />
            </div>
          ) : chartData.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-48 gap-2">
              <BarChart3 size={32} className="text-slate-700" />
              <p className="text-sm text-slate-600">Sin métricas registradas para este KPI</p>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={240}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="fecha" tick={{ fill: '#64748b', fontSize: 11 }} axisLine={{ stroke: '#1e293b' }} tickLine={false} />
                <YAxis tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} />
                <Tooltip
                  contentStyle={{ background: '#0a1628', border: '1px solid #1e293b', borderRadius: 8, fontSize: 12 }}
                  labelStyle={{ color: '#e2e8f0' }}
                />
                {defActual.valorObjetivo && (
                  <ReferenceLine y={defActual.valorObjetivo} stroke="#10b981" strokeDasharray="6 3"
                    label={{ value: 'Objetivo', fill: '#10b981', fontSize: 11 }} />
                )}
                <Line type="monotone" dataKey="valor" stroke="#3b82f6" strokeWidth={2}
                  dot={{ fill: '#3b82f6', r: 3 }} activeDot={{ r: 5 }} />
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>
      )}

      {/* Table */}
      <div className="card !p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-800">
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Fecha</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Sucursal</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Valor Actual</th>
              <th className="px-6 py-3" />
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={4} className="px-6 py-10 text-center text-slate-600 text-sm">Cargando...</td></tr>
            ) : metricas.length === 0 ? (
              <tr><td colSpan={4} className="px-6 py-10 text-center text-slate-600 text-sm">Sin métricas registradas</td></tr>
            ) : metricas.map(m => (
              <tr key={m.id} className="table-row">
                <td className="px-6 py-3.5 font-mono text-slate-400 text-xs">
                  {new Date(m.fechaRegistro).toLocaleString('es-CL')}
                </td>
                <td className="px-6 py-3.5 text-slate-300">{m.sucursal?.nombre || '—'}</td>
                <td className="px-6 py-3.5 font-mono font-medium text-accent-400">
                  {m.valorActual} {defActual?.unidad || ''}
                </td>
                <td className="px-6 py-3.5 text-right">
                  <button
                    onClick={() => handleDelete(m.id)}
                    className="p-1.5 rounded-lg text-slate-600 hover:text-red-400 hover:bg-red-500/10 transition-colors"
                  >
                    <Trash2 size={13} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal */}
      {modal && (
        <Modal title="Registrar Métrica" onClose={() => setModal(false)}>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label">Valor Actual *</label>
              <input
                value={form.valorActual} onChange={e => setForm(f => ({ ...f, valorActual: e.target.value }))}
                type="number" step="any" required className="input"
                placeholder={`Ej: 85 ${defActual?.unidad || ''}`}
              />
            </div>
            <div>
              <label className="label">Sucursal</label>
              <select
                value={form.sucursalId}
                onChange={e => setForm(f => ({ ...f, sucursalId: e.target.value }))}
                className="input"
              >
                <option value="">Sin sucursal</option>
                {sucursales.map(s => (
                  <option key={s.id} value={s.id}>{s.nombre} — {s.ciudad}</option>
                ))}
              </select>
            </div>
            <div className="flex gap-3 pt-2">
              <button type="button" className="btn-secondary flex-1 justify-center" onClick={() => setModal(false)}>
                Cancelar
              </button>
              <button type="submit" className="btn-primary flex-1 justify-center" disabled={saving}>
                {saving ? 'Guardando...' : 'Registrar'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
