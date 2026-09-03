import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useStandards } from '../api/hooks'
import { Badge, LoadState, PageHeader } from '../components/ui'

const CATEGORIES = ['INTEGRATION', 'API_CONVENTION', 'AI_PATTERN', 'SECURITY', 'DATA']

export default function StandardsPage() {
  const [category, setCategory] = useState('')
  const navigate = useNavigate()
  const { data, isLoading, error } = useStandards(category ? { category } : undefined)

  return (
    <>
      <PageHeader
        title="Standards & Reference Patterns"
        subtitle="Curated, versioned integration standards, API conventions and AI patterns — each linked to the ADR case law that established it and the landscape entries where it is applied."
        actions={
          <Link className="button" to="/standards/new">
            New standard
          </Link>
        }
      />

      <div className="filters">
        <select value={category} onChange={(e) => setCategory(e.target.value)} aria-label="Filter by category">
          <option value="">All categories</option>
          {CATEGORIES.map((entry) => (
            <option key={entry} value={entry}>
              {entry.replaceAll('_', ' ')}
            </option>
          ))}
        </select>
      </div>

      <LoadState isLoading={isLoading} error={error}>
        <table className="data-table">
          <thead>
            <tr>
              <th>Standard</th>
              <th>Category</th>
              <th>Status</th>
              <th>Version</th>
              <th>Owner</th>
              <th>Applied by</th>
              <th>Backing ADRs</th>
            </tr>
          </thead>
          <tbody>
            {(data ?? []).map((standard) => (
              <tr key={standard.id} className="clickable" onClick={() => navigate(`/standards/${standard.id}`)}>
                <td>
                  <Link to={`/standards/${standard.id}`} onClick={(e) => e.stopPropagation()}>
                    {standard.title}
                  </Link>
                </td>
                <td>
                  <Badge value={standard.category} />
                </td>
                <td>
                  <Badge value={standard.status} />
                </td>
                <td className="nowrap">v{standard.version}</td>
                <td>{standard.owner}</td>
                <td>{standard.appliedApplications.length} application(s)</td>
                <td>{standard.linkedAdrs.length} ADR(s)</td>
              </tr>
            ))}
          </tbody>
        </table>
        {(data ?? []).length === 0 && <p className="muted">No standards in this category yet.</p>}
      </LoadState>
    </>
  )
}
