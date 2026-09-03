import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAdrSearch, useAdrs } from '../api/hooks'
import { AiBadge, Badge, LoadState, PageHeader } from '../components/ui'

export default function AdrListPage({ aiOnly }: { aiOnly: boolean }) {
  const [query, setQuery] = useState('')
  const navigate = useNavigate()
  const list = useAdrs(aiOnly)
  const search = useAdrSearch(query)
  const searching = query.trim().length > 0
  const { data, isLoading, error } = searching ? search : list
  const visible = (data ?? []).filter((adr) => !aiOnly || adr.aiRelated)

  return (
    <>
      <PageHeader
        title={aiOnly ? 'AI Architecture Register' : 'Architecture Decision Records'}
        subtitle={
          aiOnly
            ? 'AI-integration decisions governed like any other architecture decision — a filtered view over the same ADR case law, not a separate silo.'
            : 'Structured decision case law: status lifecycle, supersedes chains, tags, and full-text search backed by PostgreSQL tsvector.'
        }
        actions={
          <Link className="button" to="/adrs/new">
            New ADR
          </Link>
        }
      />

      <div className="filters">
        <input
          type="search"
          placeholder="Full-text search (e.g. 'prompt injection guardrails')…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          size={45}
          aria-label="Search ADRs"
        />
      </div>

      <LoadState isLoading={isLoading} error={error}>
        <table className="data-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Title</th>
              <th>Status</th>
              <th>Department</th>
              <th>Author</th>
              <th>Tags</th>
            </tr>
          </thead>
          <tbody>
            {visible.map((adr) => (
              <tr key={adr.id} className="clickable" onClick={() => navigate(`/adrs/${adr.id}`)}>
                <td className="nowrap">ADR-{adr.adrNumber}</td>
                <td>
                  <Link to={`/adrs/${adr.id}`} onClick={(e) => e.stopPropagation()}>
                    {adr.title}
                  </Link>{' '}
                  {adr.aiRelated && <AiBadge />}
                </td>
                <td>
                  <Badge value={adr.status} />
                </td>
                <td>{adr.department ?? '—'}</td>
                <td>{adr.author}</td>
                <td className="muted">{adr.tags ?? ''}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {visible.length === 0 && (
          <p className="muted">{searching ? 'No ADRs match this search.' : 'No ADRs recorded yet.'}</p>
        )}
      </LoadState>
    </>
  )
}
