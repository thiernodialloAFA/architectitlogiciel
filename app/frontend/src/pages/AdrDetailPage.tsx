import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useAdr, useAdrs, useAuditTrail, useChangeAdrStatus } from '../api/hooks'
import { AiBadge, AuditTrail, Badge, LoadState, PageHeader } from '../components/ui'

export default function AdrDetailPage() {
  const { id = '' } = useParams()
  const { data: adr, isLoading, error } = useAdr(id)
  const { data: audit } = useAuditTrail('ADR', id)
  const { data: allAdrs } = useAdrs(false)
  const changeStatus = useChangeAdrStatus(id)
  const [supersededById, setSupersededById] = useState('')

  const candidates = (allAdrs ?? []).filter((candidate) => candidate.id !== id)

  return (
    <LoadState isLoading={isLoading} error={error}>
      {adr && (
        <>
          <PageHeader
            title={`ADR-${adr.adrNumber}: ${adr.title}`}
            subtitle={`${adr.author}${adr.department ? ` — ${adr.department}` : ''}`}
            actions={
              <>
                {adr.aiRelated && <AiBadge />}
                {adr.source === 'REPOSITORY' && <Badge value="REPOSITORY" />}
                <Badge value={adr.status} />
                {(adr.status === 'PROPOSED' || adr.status === 'ACCEPTED') && adr.source !== 'REPOSITORY' && (
                  <Link className="button secondary" to={`/adrs/${adr.id}/edit`}>
                    Edit
                  </Link>
                )}
              </>
            }
          />

          {(adr.supersedes || adr.supersededBy || adr.source === 'REPOSITORY') && (
            <div className="card inline-links">
              {adr.supersedes && (
                <p>
                  Supersedes <Link to={`/adrs/${adr.supersedes.id}`}>ADR-{adr.supersedes.adrNumber}: {adr.supersedes.title}</Link>
                </p>
              )}
              {adr.supersededBy && (
                <p>
                  Superseded by{' '}
                  <Link to={`/adrs/${adr.supersededBy.id}`}>
                    ADR-{adr.supersededBy.adrNumber}: {adr.supersededBy.title}
                  </Link>
                </p>
              )}
              {adr.source === 'REPOSITORY' && (
                <p className="muted">
                  Read-only import{adr.sourcePath ? ` of ${adr.sourcePath}` : ''} — the canonical record lives in{' '}
                  {adr.sourceRepoUrl ? <a href={adr.sourceRepoUrl}>{adr.sourceRepoUrl}</a> : 'the owning team’s repository'}.
                  Changes are picked up on the next import.
                </p>
              )}
            </div>
          )}

          <div className="card">
            <h2>Context</h2>
            <p className="prose">{adr.context}</p>
          </div>
          <div className="card">
            <h2>Decision</h2>
            <p className="prose">{adr.decision}</p>
          </div>
          <div className="card">
            <h2>Consequences</h2>
            <p className="prose">{adr.consequences ?? 'Not recorded.'}</p>
          </div>
          <div className="card">
            <h2>Alternatives considered</h2>
            <p className="prose">{adr.alternativesConsidered ?? 'Not recorded.'}</p>
          </div>

          <div className="card">
            <h2>Linked risk-register entries</h2>
            {adr.linkedRisks.length === 0 ? (
              <p className="muted">No linked risks.</p>
            ) : (
              <ul>
                {adr.linkedRisks.map((risk) => (
                  <li key={risk.id}>
                    <Link to={`/risks/${risk.id}`}>{risk.title}</Link>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="card">
            <h2>Status lifecycle</h2>
            <p className="muted">Allowed transitions: Proposed → Accepted → Deprecated / Superseded.</p>
            {adr.source === 'REPOSITORY' ? (
              <p className="muted">
                Status is managed in the owning repository and synchronised on import — it cannot be changed here.
              </p>
            ) : (
            <div className="form-actions">
              {adr.status === 'PROPOSED' && (
                <button
                  type="button"
                  className="button"
                  disabled={changeStatus.isPending}
                  onClick={() => changeStatus.mutate({ status: 'ACCEPTED' })}
                >
                  Accept
                </button>
              )}
              {adr.status === 'ACCEPTED' && (
                <>
                  <button
                    type="button"
                    className="button secondary"
                    disabled={changeStatus.isPending}
                    onClick={() => changeStatus.mutate({ status: 'DEPRECATED' })}
                  >
                    Deprecate
                  </button>
                  <select
                    value={supersededById}
                    onChange={(e) => setSupersededById(e.target.value)}
                    aria-label="Superseding ADR"
                  >
                    <option value="">Superseded by…</option>
                    {candidates.map((candidate) => (
                      <option key={candidate.id} value={candidate.id}>
                        ADR-{candidate.adrNumber}: {candidate.title}
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    className="button secondary"
                    disabled={changeStatus.isPending || !supersededById}
                    onClick={() => changeStatus.mutate({ status: 'SUPERSEDED', supersededById })}
                  >
                    Mark superseded
                  </button>
                </>
              )}
              {(adr.status === 'DEPRECATED' || adr.status === 'SUPERSEDED') && (
                <p className="muted">This ADR is {adr.status.toLowerCase()} and immutable — decision case law is never rewritten.</p>
              )}
              {changeStatus.error && <span className="error-box">{changeStatus.error.message}</span>}
            </div>
            )}
          </div>

          <div className="card">
            <h2>Audit trail</h2>
            <AuditTrail events={audit ?? []} />
          </div>
        </>
      )}
    </LoadState>
  )
}
