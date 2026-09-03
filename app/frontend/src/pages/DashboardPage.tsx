import { Link } from 'react-router-dom'
import { useDashboard } from '../api/hooks'
import { Badge, LoadState, PageHeader, StaleBadge } from '../components/ui'

export default function DashboardPage() {
  const { data, isLoading, error } = useDashboard()

  return (
    <>
      <PageHeader
        title="Governance Dashboard"
        subtitle="One connected view: landscape inventory, risk posture, and decision case law feeding the roadmap and budget cycle."
      />
      <LoadState isLoading={isLoading} error={error}>
        {data && (
          <>
            <div className="stat-grid">
              <div className="stat">
                <div className="value">{data.applicationCount}</div>
                <div className="label">Applications in landscape</div>
              </div>
              <div className="stat">
                <div className="value">{data.openRiskCount}</div>
                <div className="label">Open risk entries</div>
              </div>
              <div className={data.staleRiskCount > 0 ? 'stat alert' : 'stat'}>
                <div className="value">{data.staleRiskCount}</div>
                <div className="label">Stale risks (past review cadence)</div>
              </div>
              <div className="stat">
                <div className="value">{data.adrCount}</div>
                <div className="label">ADRs recorded</div>
              </div>
              <div className="stat">
                <div className="value">{data.proposedAdrCount}</div>
                <div className="label">ADRs awaiting decision</div>
              </div>
              <div className="stat">
                <div className="value">{data.aiAdrCount}</div>
                <div className="label">AI-architecture decisions</div>
              </div>
            </div>

            <div className="card">
              <h2>Open risks by category</h2>
              <table className="data-table">
                <tbody>
                  {Object.entries(data.risksByCategory).map(([category, count]) => (
                    <tr key={category}>
                      <td>
                        <Badge value={category} />
                      </td>
                      <td>{count}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="card">
              <h2>
                Top-priority risks <Link to="/risks">→ full register</Link>
              </h2>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Risk</th>
                    <th>Application</th>
                    <th>Residual</th>
                    <th>Treatment</th>
                    <th>Owner</th>
                  </tr>
                </thead>
                <tbody>
                  {data.topRisks.map((risk) => (
                    <tr key={risk.id}>
                      <td>
                        <Link to={`/risks/${risk.id}`}>{risk.title}</Link> {risk.stale && <StaleBadge />}
                      </td>
                      <td>{risk.applicationName}</td>
                      <td>
                        <span className="score-chip">{risk.residualScore}</span>{' '}
                        <Badge value={risk.residualImpact} /> × <Badge value={risk.residualLikelihood} />
                      </td>
                      <td>
                        <Badge value={risk.treatmentDecision} />
                      </td>
                      <td>{risk.riskOwner}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </LoadState>
    </>
  )
}
