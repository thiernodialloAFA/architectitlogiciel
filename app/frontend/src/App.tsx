import { NavLink, Navigate, Route, Routes } from 'react-router-dom'
import DashboardPage from './pages/DashboardPage'
import LandscapePage from './pages/LandscapePage'
import ApplicationDetailPage from './pages/ApplicationDetailPage'
import RiskRegisterPage from './pages/RiskRegisterPage'
import RiskDetailPage from './pages/RiskDetailPage'
import RiskFormPage from './pages/RiskFormPage'
import AdrListPage from './pages/AdrListPage'
import AdrDetailPage from './pages/AdrDetailPage'
import AdrFormPage from './pages/AdrFormPage'
import RoadmapPage from './pages/RoadmapPage'

export default function App() {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">AG</span>
          <div>
            <strong>Architecture Governance</strong>
            <small>Pierre &amp; Vacances Group — demo</small>
          </div>
        </div>
        <nav>
          <NavLink to="/" end>
            Dashboard
          </NavLink>
          <NavLink to="/landscape">Application Landscape</NavLink>
          <NavLink to="/risks">Risk Register</NavLink>
          <NavLink to="/adrs">ADRs &amp; Case Law</NavLink>
          <NavLink to="/adrs-ai">AI Architecture Register</NavLink>
          <NavLink to="/roadmap">Roadmap</NavLink>
        </nav>
        <footer className="sidebar-footer">
          Seeded demo data only — no production information (proposal §5).
        </footer>
      </aside>
      <main className="content">
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/landscape" element={<LandscapePage />} />
          <Route path="/landscape/:id" element={<ApplicationDetailPage />} />
          <Route path="/risks" element={<RiskRegisterPage />} />
          <Route path="/risks/new" element={<RiskFormPage />} />
          <Route path="/risks/:id" element={<RiskDetailPage />} />
          <Route path="/risks/:id/edit" element={<RiskFormPage />} />
          <Route path="/adrs" element={<AdrListPage aiOnly={false} />} />
          <Route path="/adrs-ai" element={<AdrListPage aiOnly={true} />} />
          <Route path="/adrs/new" element={<AdrFormPage />} />
          <Route path="/adrs/:id" element={<AdrDetailPage />} />
          <Route path="/adrs/:id/edit" element={<AdrFormPage />} />
          <Route path="/roadmap" element={<RoadmapPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  )
}
