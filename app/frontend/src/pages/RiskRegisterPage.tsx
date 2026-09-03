import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useRisks, useTopRisks } from '../api/hooks'
import type { Risk } from '../api/types'
import { Badge, LoadState, PageHeader, StaleBadge } from '../components/ui'

const CATEGORIES = ['TECHNICAL_DEBT', 'END_OF_LIFE', 'INTEGRATION_BRITTLENESS', 'SECURITY_COMPLIANCE', 'AI_READINESS']
const STATUSES = ['OPEN', 'IN_PROGRESS', 'MITIGATED', 'ACCEPTED', 'CLOSED']

function csvEscape(value: string | number | null | undefined): string {
  const text = value == null ? '' : String(value)
  return `"${text.replaceAll('"', '""')}"`
}

function exportTopPriorityCsv(risks: Risk[]) {
  const header = [
    'Rank', 'Title', 'Application', 'Category', 'Residual impact', 'Residual likelihood', 'Residual score',
    'Treatment (TIME)', 'Cost to fix', 'Target date', 'Status', 'Risk owner', 'Last assessed', 'Stale',
  ]
  const rows = risks.map((risk, index) => [
    index + 1, risk.title, risk.applicationName, risk.category, risk.residualImpact, risk.residualLikelihood,
    risk.residualScore, risk.treatmentDecision, risk.costToFix, risk.targetDate, risk.status, risk.riskOwner,
    risk.lastAssessedAt, risk.stale ? 'YES' : 'no',
  ])
  const csv = [header, ...rows].map((row) => row.map(csvEscape).join(',')).join('\r\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'top-priority-risks.csv'
  link.click()
  URL.revokeObjectURL(link.href)
}

export default function RiskRegisterPage() {
  const [category, setCategory] = useState('')
  const [status, setStatus] = useState('')
  const [staleOnly, setStaleOnly] = useState(false)
  const navigate = useNavigate()
  const { data, isLoading, error } = useRisks({ category: category || undefined, status: status || undefined })
  const { data: topRisks } = useTopRisks(10)

  const visible = (data ?? []).filter((risk) => !staleOnly || risk.stale)

  return (
    <>
      <PageHeader
        title="Risk Register"
        subtitle="A living register: every entry carries inherent vs. residual ratings, a TIME treatment decision, a named risk owner, and a review cadence with staleness flags."
        actions={
          <>
            <button
              type="button"
              className="button secondary"
              onClick={() => topRisks && exportTopPriorityCsv(topRisks)}
              disabled={!topRisks || topRisks.length === 0}
              title="Export the force-ranked top-10 for budget-cycle conversations"
            >
              Export top-10 CSV
            </button>
            <Link className="button" to="/risks/new">
              New risk entry
            </Link>
          </>
        }
      />

      <div className="filters">
        <select value={category} onChange={(e) => setCategory(e.target.value)} aria-label="Filter by category">
          <option value="">All categories</option>
          {CATEGORIES.map((value) => (
            <option key={value} value={value}>
              {value.replaceAll('_', ' ')}
            </option>
          ))}
        </select>
        <select value={status} onChange={(e) => setStatus(e.target.value)} aria-label="Filter by status">
          <option value="">All statuses</option>
          {STATUSES.map((value) => (
            <option key={value} value={value}>
              {value.replaceAll('_', ' ')}
            </option>
          ))}
        </select>
        <label>
          <input type="checkbox" checked={staleOnly} onChange={(e) => setStaleOnly(e.target.checked)} /> Stale only
        </label>
      </div>

      <LoadState isLoading={isLoading} error={error}>
        <table className="data-table">
          <thead>
            <tr>
              <th>Risk</th>
              <th>Application</th>
              <th>Category</th>
              <th>Residual</th>
              <th>Treatment</th>
              <th>Status</th>
              <th>Owner</th>
              <th>Last assessed</th>
            </tr>
          </thead>
          <tbody>
            {visible.map((risk) => (
              <tr key={risk.id} className="clickable" onClick={() => navigate(`/risks/${risk.id}`)}>
                <td>
                  <Link to={`/risks/${risk.id}`} onClick={(e) => e.stopPropagation()}>
                    {risk.title}
                  </Link>{' '}
                  {risk.stale && <StaleBadge />}
                </td>
                <td>{risk.applicationName}</td>
                <td>
                  <Badge value={risk.category} />
                </td>
                <td>
                  <span className="score-chip">{risk.residualScore}</span>
                </td>
                <td>
                  <Badge value={risk.treatmentDecision} />
                </td>
                <td>
                  <Badge value={risk.status} />
                </td>
                <td>{risk.riskOwner}</td>
                <td className="nowrap">{risk.lastAssessedAt}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {visible.length === 0 && <p className="muted">No risk entries match the current filters.</p>}
      </LoadState>
    </>
  )
}
