import { useEffect, useState } from 'react'
import { getDefiniciones, getSucursales, getMetricas } from '../api/kpiApi'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  LineChart, Line, ResponsiveContainer,
} from 'recharts'
import { Target, Building2, Activity, TrendingUp } from 'lucide-react'

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6']

export default function Dashboard() {
  const [definiciones, setDefiniciones] = useState([])
  const [sucursales, setSucursales] = useState([])
  const [chartData, setChartData] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        const [defs, sucs] = await Promise.all([getDefiniciones(), getSucursales()])
        setDefiniciones(defs)
        setSucursales(sucs)

        // Cargar métricas de los primeros 5 KPIs para el gráfico
        const metricsPromises = defs.slice(0, 5).map(d => getMetricas(d.id))
        const allMetrics = await Promise.all(metricsPromises)

        const data = defs.slice(0, 5).map((def, i) => {
          const metricas = allMetrics[i]
          const ultimas = metricas.slice(-5)
          const promedio =
            ultimas.length > 0
              ? +(ultimas.reduce((s, m) => s + (m.valorActual || 0), 0) / ultimas.length).toFixed(2)
              : 0
          return {
            nombre: def.nombre,
            promedio,
            objetivo: def.valorObjetivo || 0,
          }
        })
        setChartData(data)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  const stats = [
    { label: 'KPIs Definidos', value: definiciones.length, icon: Target, color: 'text-blue-400', bg: 'bg-blue-500/10' },
    { label: 'Sucursales', value: sucursales.length, icon: Building2, color: 'text-emerald-400', bg: 'bg-emerald-500/10' },
    { label: 'Con Objetivo', value: definiciones.filter(d => d.valorObjetivo).length, icon: TrendingUp, color: 'text-amber-400', bg: 'bg-amber-500/10' },
    { label: 'Activos', value: definiciones.length, icon: Activity, color: 'text-purple-400', bg: 'bg-purple-500/10' },
  ]

  if (loading) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-accent-500 border-t-transparent rounded-full animate-spin" />
          <p className="text-sm text-slate-500">Cargando dashboard...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex-1 p-8 space-y-8 animate-fade-in">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-semibold text-slate-100">Dashboard</h1>
        <p className="text-sm text-slate-500 mt-1">Resumen general de indicadores de desempeño</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
        {stats.map(({ label, value, icon: Icon, color, bg }) => (
          <div key={label} className="card flex items-center gap-4">
            <div className={`w-11 h-11 rounded-xl ${bg} flex items-center justify-center shrink-0`}>
              <Icon size={20} className={color} />
            </div>
            <div>
              <p className="text-2xl font-semibold font-mono text-slate-100">{value}</p>
              <p className="text-xs text-slate-500 mt-0.5">{label}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* Bar chart: Promedio vs Objetivo */}
        <div className="card">
          <h2 className="text-sm font-semibold text-slate-300 mb-6">Promedio vs Objetivo por KPI</h2>
          {chartData.length === 0 ? (
            <p className="text-sm text-slate-600 text-center py-8">Sin datos de métricas aún</p>
          ) : (
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={chartData} barGap={4}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis
                  dataKey="nombre"
                  tick={{ fill: '#64748b', fontSize: 11 }}
                  axisLine={{ stroke: '#1e293b' }}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fill: '#64748b', fontSize: 11 }}
                  axisLine={false}
                  tickLine={false}
                />
                <Tooltip
                  contentStyle={{ background: '#0a1628', border: '1px solid #1e293b', borderRadius: 8, fontSize: 12 }}
                  labelStyle={{ color: '#e2e8f0' }}
                />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Bar dataKey="promedio" name="Promedio actual" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                <Bar dataKey="objetivo" name="Valor objetivo" fill="#10b981" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* KPI list with progress */}
        <div className="card">
          <h2 className="text-sm font-semibold text-slate-300 mb-6">Estado de KPIs</h2>
          {definiciones.length === 0 ? (
            <p className="text-sm text-slate-600 text-center py-8">No hay KPIs registrados</p>
          ) : (
            <div className="space-y-4">
              {definiciones.slice(0, 6).map((def, i) => {
                const entry = chartData.find(c => c.nombre === def.nombre)
                const pct = entry && entry.objetivo > 0
                  ? Math.min(100, Math.round((entry.promedio / entry.objetivo) * 100))
                  : 0
                const color = pct >= 100 ? 'bg-emerald-500' : pct >= 60 ? 'bg-blue-500' : 'bg-amber-500'
                return (
                  <div key={def.id}>
                    <div className="flex items-center justify-between mb-1.5">
                      <span className="text-sm text-slate-300">{def.nombre}</span>
                      <span className="text-xs font-mono text-slate-500">
                        {entry?.promedio ?? '–'} / {def.valorObjetivo ?? '–'} {def.unidad || ''}
                      </span>
                    </div>
                    <div className="h-1.5 bg-surface-800 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-700 ${color}`}
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
