import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// ─── KPI Definiciones ────────────────────────────────────────────────
export const getDefiniciones = () => api.get('/kpi/definiciones').then(r => r.data)
export const getDefinicion = (id) => api.get(`/kpi/definiciones/${id}`).then(r => r.data)
export const createDefinicion = (data) => api.post('/kpi/definiciones', data).then(r => r.data)
export const updateDefinicion = (id, data) => api.put(`/kpi/definiciones/${id}`, data).then(r => r.data)
export const deleteDefinicion = (id) => api.delete(`/kpi/definiciones/${id}`)

// ─── KPI Metricas ────────────────────────────────────────────────────
export const getMetricas = (definicionId) => api.get(`/kpi/metricas/${definicionId}`).then(r => r.data)
export const createMetrica = (data) => api.post('/kpi/metricas', data).then(r => r.data)
export const deleteMetrica = (id) => api.delete(`/kpi/metricas/${id}`)

// ─── Sucursales ──────────────────────────────────────────────────────
export const getSucursales = () => api.get('/sucursales').then(r => r.data)
export const createSucursal = (data) => api.post('/sucursales', data).then(r => r.data)
export const updateSucursal = (id, data) => api.put(`/sucursales/${id}`, data).then(r => r.data)
export const deleteSucursal = (id) => api.delete(`/sucursales/${id}`)
