import { Link, useParams } from 'react-router-dom'
import { useAuditTrail, useRisk } from '../api/hooks'
import { AuditTrail, Badge, LoadState, PageHeader, StaleBadge } from '../components/ui'

export default function RiskDetailPage() {
  const { id = '' } = useParams()
  const { data: risk, isLoading, error } = useRisk(id)
  const { data: audit } = useAuditTrail('RISK_ENTRY', id)

  return (
    <LoadState isLoading={isLoading} error={error}>
      {risk && (
        <>
          <PageHeader
            title={risk.title}
            subtitle={`${risk.applicationName} — owned by ${risk.riskOwner}`}
            actions={
              <>
                {risk.stale && <StaleBadge />}
                <Link className="button secondary" to={`/risks/${risk.id}/edit`}>
                  Edit / re-assess
                </Link>
              </>
            }
          />

          <div className="card">
            <h2>Assessment</h2>
            <dl className="detail-grid">
              <div>
                <dt>Category</dt>
                <dd>
                  <Badge value={risk.category} />
                </dd>
              </div>
              <div>
                <dt>Status</dt>
                <dd>
                  <Badge value={risk.status} />
                </dd>
              </div>
              <div>
                <dt>Inherent (impact × likelihood)</dt>
                <dd>
                  <Badge value={risk.inherentImpact} /> × <Badge value={risk.inherentLikelihood} />
                </dd>
              </div>
              <div>
                <dt>Residual (impact × likelihood)</dt>
                <dd>
                  <Badge value={risk.residualImpact} /> × <Badge value={risk.residualLikelihood} /> ={' '}
                  <span className="score-chip">{risk.residualScore}</span>
                </dd>
              </div>
              <div>
                <dt>Treatment decision (TIME)</dt>
                <dd>
                  <Badge value={risk.treatmentDecision} />
                </dd>
              </div>
              <div>
                <dt>Cost to fix</dt>
                <dd>{risk.costToFix ?? '—'}</dd>
              </div>
              <div>
                <dt>Target date</dt>
                <dd>{risk.targetDate ?? '—'}</dd>
              </div>
              <div>
                <dt>Review cadence</dt>
                <dd>
                  every {risk.reviewCadenceDays} days (last assessed {risk.lastAssessedAt})
                </dd>
              </div>
              <div>
                <dt>Evidence</dt>
                <dd>{risk.evidenceLink ? <a href={risk.evidenceLink}>{risk.evidenceLink}</a> : '—'}</dd>
              </div>
            </dl>
          </div>

          <div className="card">
            <h2>Description</h2>
            <p className="prose">{risk.description}</p>
          </div>

          <div className="card">
            <h2>Current controls</h2>
            <p className="prose">{risk.currentControls ?? 'None recorded.'}</p>
          </div>

          <div className="card">
            <h2>Blast radius</h2>
            <p className="prose">{risk.blastRadius ?? 'Not assessed.'}</p>
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
