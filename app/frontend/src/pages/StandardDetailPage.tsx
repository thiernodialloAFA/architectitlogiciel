import { Link, useParams } from 'react-router-dom'
import { useAuditTrail, useChangeStandardStatus, useStandard } from '../api/hooks'
import type { StandardStatus } from '../api/types'
import { AuditTrail, Badge, LoadState, PageHeader } from '../components/ui'

export default function StandardDetailPage() {
  const { id = '' } = useParams()
  const { data: standard, isLoading, error } = useStandard(id)
  const { data: audit } = useAuditTrail('STANDARD', id)
  const changeStatus = useChangeStandardStatus(id)

  const nextStatus: StandardStatus | null =
    standard?.status === 'DRAFT' ? 'ACTIVE' : standard?.status === 'ACTIVE' ? 'RETIRED' : null

  return (
    <LoadState isLoading={isLoading} error={error}>
      {standard && (
        <>
          <PageHeader
            title={standard.title}
            subtitle={`Version ${standard.version} — owned by ${standard.owner}`}
            actions={
              <>
                <Badge value={standard.category} /> <Badge value={standard.status} />
                {standard.status !== 'RETIRED' && (
                  <Link className="button secondary" to={`/standards/${standard.id}/edit`}>
                    Edit (bumps version)
                  </Link>
                )}
                {nextStatus && (
                  <button
                    className="button"
                    type="button"
                    disabled={changeStatus.isPending}
                    onClick={() => changeStatus.mutate({ status: nextStatus })}
                  >
                    {nextStatus === 'ACTIVE' ? 'Activate' : 'Retire'}
                  </button>
                )}
              </>
            }
          />
          {changeStatus.error && <p className="error-box">{changeStatus.error.message}</p>}

          <div className="card">
            <h2>Standard</h2>
            <p className="prose">{standard.content}</p>
            {standard.status === 'RETIRED' && (
              <p className="muted">This standard is retired and can no longer be edited; its history stays in the audit trail.</p>
            )}
          </div>

          <div className="card">
            <h2>Established by (ADR case law)</h2>
            {standard.linkedAdrs.length === 0 ? (
              <p className="muted">No backing ADRs linked.</p>
            ) : (
              <ul className="inline-links">
                {standard.linkedAdrs.map((adr) => (
                  <li key={adr.id}>
                    <Link to={`/adrs/${adr.id}`}>
                      ADR-{adr.adrNumber} — {adr.title}
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="card">
            <h2>Applied by landscape entries</h2>
            {standard.appliedApplications.length === 0 ? (
              <p className="muted">Not yet marked as applied anywhere.</p>
            ) : (
              <ul className="inline-links">
                {standard.appliedApplications.map((app) => (
                  <li key={app.id}>
                    <Link to={`/landscape/${app.id}`}>{app.name}</Link>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="card">
            <h2>Version history &amp; audit trail</h2>
            <p className="muted">Every content change bumps the version; old and new content are both retained below.</p>
            <AuditTrail events={audit ?? []} />
          </div>
        </>
      )}
    </LoadState>
  )
}
