import { Link, useParams } from 'react-router-dom'
import { useApplication, useAuditTrail, useRisks } from '../api/hooks'
import { AuditTrail, Badge, LoadState, PageHeader, StaleBadge } from '../components/ui'

export default function ApplicationDetailPage() {
  const { id = '' } = useParams()
  const { data: app, isLoading, error } = useApplication(id)
  const { data: risks } = useRisks({ applicationId: id })
  const { data: audit } = useAuditTrail('APPLICATION', id)

  return (
    <LoadState isLoading={isLoading} error={error}>
      {app && (
        <>
          <PageHeader
            title={app.name}
            subtitle={app.description ?? undefined}
            actions={<Badge value={app.lifecycleStatus} />}
          />

          <div className="card">
            <h2>Ownership</h2>
            <dl className="detail-grid">
              <div>
                <dt>Owner team</dt>
                <dd>{app.ownerTeam}</dd>
              </div>
              <div>
                <dt>Department</dt>
                <dd>{app.ownerDepartment}</dd>
              </div>
              <div>
                <dt>C4 model</dt>
                <dd>{app.c4ModelLink ? <a href={app.c4ModelLink}>{app.c4ModelLink}</a> : 'Not yet linked'}</dd>
              </div>
              <div>
                <dt>ADR log</dt>
                <dd>{app.adrLogLink ? <a href={app.adrLogLink}>{app.adrLogLink}</a> : 'Not yet linked'}</dd>
              </div>
            </dl>
          </div>

          <div className="card">
            <h2>Integration dependencies</h2>
            {app.dependencies.length === 0 ? (
              <p className="muted">No outgoing dependencies recorded.</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Depends on</th>
                    <th>Integration</th>
                    <th>Description</th>
                  </tr>
                </thead>
                <tbody>
                  {app.dependencies.map((dep) => (
                    <tr key={dep.id}>
                      <td>
                        <Link to={`/landscape/${dep.dependsOnId}`}>{dep.dependsOnName}</Link>
                      </td>
                      <td>
                        <Badge value={dep.integrationType} />
                      </td>
                      <td>{dep.description}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          <div className="card">
            <h2>Risk entries</h2>
            {!risks || risks.length === 0 ? (
              <p className="muted">No risks recorded for this application.</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Risk</th>
                    <th>Category</th>
                    <th>Residual</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {risks.map((risk) => (
                    <tr key={risk.id}>
                      <td>
                        <Link to={`/risks/${risk.id}`}>{risk.title}</Link> {risk.stale && <StaleBadge />}
                      </td>
                      <td>
                        <Badge value={risk.category} />
                      </td>
                      <td>
                        <span className="score-chip">{risk.residualScore}</span>
                      </td>
                      <td>
                        <Badge value={risk.status} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
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
