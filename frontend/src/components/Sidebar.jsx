import { NavLink } from 'react-router-dom'
import { LayoutDashboard, Target, BarChart3, Building2, TrendingUp } from 'lucide-react'

const links = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/kpis', icon: Target, label: 'KPIs' },
  { to: '/metricas', icon: BarChart3, label: 'Métricas' },
  { to: '/sucursales', icon: Building2, label: 'Sucursales' },
]

export default function Sidebar() {
  return (
    <aside className="w-60 shrink-0 bg-surface-900 border-r border-slate-800 flex flex-col min-h-screen">
      {/* Logo */}
      <div className="px-6 py-5 border-b border-slate-800">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-accent-500 flex items-center justify-center">
            <TrendingUp size={16} className="text-white" />
          </div>
          <div>
            <p className="text-sm font-semibold text-slate-100 leading-tight">KPI Manager</p>
            <p className="text-xs text-slate-500">Grupo Cordillera</p>
          </div>
        </div>
      </div>

      {/* Nav links */}
      <nav className="flex-1 px-3 py-4 space-y-0.5">
        {links.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 ${
                isActive
                  ? 'bg-accent-500/15 text-accent-400 border border-accent-500/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-surface-800'
              }`
            }
          >
            <Icon size={16} />
            {label}
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="px-4 py-4 border-t border-slate-800">
        <p className="text-xs text-slate-600 font-mono">v1.0.0 · DSY1106</p>
      </div>
    </aside>
  )
}
