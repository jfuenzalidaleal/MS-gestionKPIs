import { useEffect, useState } from 'react'
import { getDefiniciones, createDefinicion, updateDefinicion, deleteDefinicion } from '../api/kpiApi'
import Modal from '../components/Modal'
import { Plus, Pencil, Trash2, Target } from 'lucide-react'

const empty = { nombre: '', descripcion: '', valorObjetivo: '', unidad: '' }

export default function KpiDefiniciones() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState(null) // null | 'create' | 'edit'
  const [form, setForm] = useState(empty)
  const [editId, setEditId] = useState(null)
  const [saving, setSaving] = useState(false)

  const load = () =>
    getDefiniciones()
      .then(setItems)
      .finally(() => setLoading(false))

  useEffect(() => { load() }, [])

  const openCreate = () => { setForm(empty); setEditId(null); setModal('form') }
  const openEdit = (item) => {
    setForm({ nombre: item.nombre, descripcion: item.descripcion || '', valorObjetivo: item.valorObjetivo ?? '', unidad: item.unidad || '' })
    setEditId(item.id)
    setModal('form')
  }
  const closeModal = () => setModal(null)

  const handleChange = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      const payload = { ...form, valorObjetivo: form.valorObjetivo ? Number(form.valorObjetivo) : null }
      if (editId) await updateDefinicion(editId, payload)
      else await createDefinicion(payload)
      closeModal()
      load()
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id) => {
    if (!confirm('¿Eliminar este KPI? También se eliminarán sus métricas.')) return
    await deleteDefinicion(id)
    load()
  }

  if (loading) return (
    <div className="flex-1 flex items-center justify-center">
      <div className="w-8 h-8 border-2 border-accent-500 border-t-transparent rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="flex-1 p-8 space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-100">KPIs</h1>
          <p className="text-sm text-slate-500 mt-1">Gestión de indicadores de desempeño</p>
        </div>
        <button className="btn-primary" onClick={openCreate}>
          <Plus size={16} /> Nuevo KPI
        </button>
      </div>

      {/* Table */}
      <div className="card !p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-800">
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Nombre</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Descripción</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Objetivo</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Unidad</th>
              <th className="px-6 py-3" />
            </tr>
          </thead>
          <tbody>
            {items.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-6 py-12 text-center">
                  <Target size={32} className="text-slate-700 mx-auto mb-3" />
                  <p className="text-slate-500 text-sm">No hay KPIs registrados</p>
                  <button className="btn-primary mt-4 mx-auto" onClick={openCreate}>
                    <Plus size={14} /> Crear primer KPI
                  </button>
                </td>
              </tr>
            ) : items.map(item => (
              <tr key={item.id} className="table-row">
                <td className="px-6 py-4 font-medium text-slate-200">{item.nombre}</td>
                <td className="px-6 py-4 text-slate-400 max-w-xs truncate">{item.descripcion || '—'}</td>
                <td className="px-6 py-4 font-mono text-accent-400">{item.valorObjetivo ?? '—'}</td>
                <td className="px-6 py-4">
                  {item.unidad ? (
                    <span className="badge bg-slate-800 text-slate-300">{item.unidad}</span>
                  ) : '—'}
                </td>
                <td className="px-6 py-4">
                  <div className="flex items-center gap-2 justify-end">
                    <button
                      onClick={() => openEdit(item)}
                      className="p-1.5 rounded-lg text-slate-500 hover:text-accent-400 hover:bg-accent-500/10 transition-colors"
                    >
                      <Pencil size={14} />
                    </button>
                    <button
                      onClick={() => handleDelete(item.id)}
                      className="p-1.5 rounded-lg text-slate-500 hover:text-red-400 hover:bg-red-500/10 transition-colors"
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal */}
      {modal === 'form' && (
        <Modal title={editId ? 'Editar KPI' : 'Nuevo KPI'} onClose={closeModal}>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label">Nombre *</label>
              <input name="nombre" value={form.nombre} onChange={handleChange}
                required className="input" placeholder="Ej: Ventas mensuales" />
            </div>
            <div>
              <label className="label">Descripción</label>
              <textarea name="descripcion" value={form.descripcion} onChange={handleChange}
                className="input resize-none" rows={3} placeholder="Descripción del KPI..." />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Valor Objetivo</label>
                <input name="valorObjetivo" value={form.valorObjetivo} onChange={handleChange}
                  type="number" step="any" className="input" placeholder="100" />
              </div>
              <div>
                <label className="label">Unidad</label>
                <input name="unidad" value={form.unidad} onChange={handleChange}
                  className="input" placeholder="Ej: %, $, unidades" />
              </div>
            </div>
            <div className="flex gap-3 pt-2">
              <button type="button" className="btn-secondary flex-1 justify-center" onClick={closeModal}>
                Cancelar
              </button>
              <button type="submit" className="btn-primary flex-1 justify-center" disabled={saving}>
                {saving ? 'Guardando...' : editId ? 'Actualizar' : 'Crear KPI'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
