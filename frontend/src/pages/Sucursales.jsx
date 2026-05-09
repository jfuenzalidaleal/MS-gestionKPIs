import { useEffect, useState } from 'react'
import { getSucursales, createSucursal, updateSucursal, deleteSucursal } from '../api/kpiApi'
import Modal from '../components/Modal'
import { Plus, Pencil, Trash2, Building2 } from 'lucide-react'

const empty = { nombre: '', ciudad: '' }

export default function Sucursales() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState(empty)
  const [editId, setEditId] = useState(null)
  const [saving, setSaving] = useState(false)

  const load = () => getSucursales().then(setItems).finally(() => setLoading(false))
  useEffect(() => { load() }, [])

  const openCreate = () => { setForm(empty); setEditId(null); setModal(true) }
  const openEdit = (item) => { setForm({ nombre: item.nombre, ciudad: item.ciudad }); setEditId(item.id); setModal(true) }
  const closeModal = () => setModal(false)
  const handleChange = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      if (editId) await updateSucursal(editId, form)
      else await createSucursal(form)
      closeModal()
      load()
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id) => {
    if (!confirm('¿Eliminar esta sucursal?')) return
    await deleteSucursal(id)
    load()
  }

  if (loading) return (
    <div className="flex-1 flex items-center justify-center">
      <div className="w-8 h-8 border-2 border-accent-500 border-t-transparent rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="flex-1 p-8 space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-100">Sucursales</h1>
          <p className="text-sm text-slate-500 mt-1">Gestión de sucursales de la organización</p>
        </div>
        <button className="btn-primary" onClick={openCreate}>
          <Plus size={16} /> Nueva Sucursal
        </button>
      </div>

      <div className="card !p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-800">
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">#</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Nombre</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Ciudad</th>
              <th className="px-6 py-3" />
            </tr>
          </thead>
          <tbody>
            {items.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-6 py-12 text-center">
                  <Building2 size={32} className="text-slate-700 mx-auto mb-3" />
                  <p className="text-slate-500 text-sm">No hay sucursales registradas</p>
                  <button className="btn-primary mt-4 mx-auto" onClick={openCreate}>
                    <Plus size={14} /> Agregar sucursal
                  </button>
                </td>
              </tr>
            ) : items.map(item => (
              <tr key={item.id} className="table-row">
                <td className="px-6 py-4 font-mono text-slate-600 text-xs">{item.id}</td>
                <td className="px-6 py-4 font-medium text-slate-200">{item.nombre}</td>
                <td className="px-6 py-4">
                  <span className="badge bg-surface-800 text-slate-400">{item.ciudad}</span>
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

      {modal && (
        <Modal title={editId ? 'Editar Sucursal' : 'Nueva Sucursal'} onClose={closeModal}>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label">Nombre *</label>
              <input name="nombre" value={form.nombre} onChange={handleChange}
                required className="input" placeholder="Ej: Sucursal Centro" />
            </div>
            <div>
              <label className="label">Ciudad *</label>
              <input name="ciudad" value={form.ciudad} onChange={handleChange}
                required className="input" placeholder="Ej: Santiago" />
            </div>
            <div className="flex gap-3 pt-2">
              <button type="button" className="btn-secondary flex-1 justify-center" onClick={closeModal}>
                Cancelar
              </button>
              <button type="submit" className="btn-primary flex-1 justify-center" disabled={saving}>
                {saving ? 'Guardando...' : editId ? 'Actualizar' : 'Crear'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
