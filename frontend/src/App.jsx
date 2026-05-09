import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Sidebar from './components/Sidebar'
import Dashboard from './pages/Dashboard'
import KpiDefiniciones from './pages/KpiDefiniciones'
import KpiMetricas from './pages/KpiMetricas'
import Sucursales from './pages/Sucursales'

export default function App() {
  return (
    <BrowserRouter>
      <div className="flex min-h-screen">
        <Sidebar />
        <main className="flex-1 flex flex-col overflow-auto">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/kpis" element={<KpiDefiniciones />} />
            <Route path="/metricas" element={<KpiMetricas />} />
            <Route path="/sucursales" element={<Sucursales />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
