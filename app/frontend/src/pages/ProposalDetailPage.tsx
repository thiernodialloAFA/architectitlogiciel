import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  useAdrs,
  useArbitrate,
  useArbitrations,
  useAuditTrail,
  useForumSessions,
  useProposal,
  useRecordOutcome,
  useScheduleProposal,
} from '../api/hooks'
import type { Proposal } from '../api/types'
import { AuditTrail, Badge, LoadState, PageHeader } from '../components/ui'

export default function ProposalDetailPage() {
  const { id = '' } = useParams()
  const { data: proposal, isLoading, error } = useProposal(id)
  const { data: audit } = useAuditTrail('AAF_PROPOSAL', id)
  const { data: arbitrations } = useArbitrations()
  const arbitration = (arbitrations ?? []).find((entry) => entry.proposalId === id)

  return (
    <LoadState isLoading={isLoading} error={error}>
      {proposal && (
        <>
          <PageHeader
            title={proposal.title}
            subtitle={`Submitted by ${proposal.submittedBy}${proposal.department ? ` — ${proposal.department}` : ''}`}
            actions={
              <>
                <Badge value={proposal.scope} /> <Badge value={proposal.status} />
              </>
            }
          />

          <div className="card">
            <h2>Proposal</h2>
            <p className="prose">{proposal.summary}</p>
            <dl className="detail-grid">
              <div>
                <dt>Related landscape entry</dt>
                <dd>
                  {proposal.application ? (
                    <Link to={`/landscape/${proposal.application.id}`}>{proposal.application.name}</Link>
                  ) : (
                    '—'
                  )}
                </dd>
              </div>
              <div>
                <dt>Related ADR</dt>
                <dd>
                  {proposal.adr ? (
                    <Link to={`/adrs/${proposal.adr.id}`}>
                      ADR-{proposal.adr.adrNumber} — {proposal.adr.title}
                    </Link>
                  ) : (
                    '—'
                  )}
                </dd>
              </div>
              <div>
                <dt>Forum session</dt>
                <dd>
                  {proposal.session ? (
                    <Link to={`/forum/sessions/${proposal.session.id}`}>
                      {proposal.session.sessionDate} — {proposal.session.title}
                    </Link>
                  ) : (
                    'Not scheduled yet'
                  )}
                </dd>
              </div>
              <div>
                <dt>Decided at</dt>
                <dd>{proposal.decidedAt ? new Date(proposal.decidedAt).toLocaleString() : '—'}</dd>
              </div>
            </dl>
          </div>

          {proposal.status === 'DECIDED' && (
            <div className="card">
              <h2>Advice &amp; decision</h2>
              <dl className="detail-grid">
                <div>
                  <dt>Advice given</dt>
                  <dd className="prose">{proposal.adviceGiven}</dd>
                </div>
                <div>
                  <dt>Advised by</dt>
                  <dd>{proposal.advisedBy}</dd>
                </div>
                <div>
                  <dt>Final decision (proposer decides)</dt>
                  <dd className="prose">{proposal.finalDecision}</dd>
                </div>
              </dl>
            </div>
          )}

          {arbitration && (
            <div className="card">
              <h2>Arbitration record</h2>
              <dl className="detail-grid">
                <div>
                  <dt>Requested by</dt>
                  <dd>{arbitration.requestedBy}</dd>
                </div>
                <div>
                  <dt>Decided by</dt>
                  <dd>{arbitration.decidedBy}</dd>
                </div>
                <div>
                  <dt>Rationale</dt>
                  <dd className="prose">{arbitration.rationale}</dd>
                </div>
                <div>
                  <dt>Outcome</dt>
                  <dd className="prose">{arbitration.outcome}</dd>
                </div>
              </dl>
            </div>
          )}

          {proposal.status === 'SUBMITTED' && <ScheduleForm proposal={proposal} />}
          {proposal.status === 'SCHEDULED' && <OutcomeForm proposal={proposal} />}
          {proposal.scope === 'GROUP' && proposal.status !== 'DECIDED' && <ArbitrationForm proposal={proposal} />}

          <div className="card">
            <h2>Audit trail</h2>
            <AuditTrail events={audit ?? []} />
          </div>
        </>
      )}
    </LoadState>
  )
}

function ScheduleForm({ proposal }: { proposal: Proposal }) {
  const schedule = useScheduleProposal(proposal.id)
  const { data: sessions } = useForumSessions()
  const planned = (sessions ?? []).filter((session) => session.status === 'PLANNED')
  const [sessionId, setSessionId] = useState('')

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    schedule.mutate({ sessionId })
  }

  return (
    <div className="card">
      <h2>Schedule onto a forum session</h2>
      {planned.length === 0 ? (
        <p className="muted">
          No planned sessions available — plan one on the <Link to="/forum">Advice Forum</Link> page first.
        </p>
      ) : (
        <form onSubmit={onSubmit}>
          <div className="field">
            <label htmlFor="session">Planned session</label>
            <select id="session" required value={sessionId} onChange={(e) => setSessionId(e.target.value)}>
              <option value="">Select a session…</option>
              {planned.map((session) => (
                <option key={session.id} value={session.id}>
                  {session.sessionDate} — {session.title}
                </option>
              ))}
            </select>
          </div>
          <div className="form-actions">
            <button className="button" type="submit" disabled={schedule.isPending || sessionId === ''}>
              Schedule
            </button>
            {schedule.error && <span className="error-box">{schedule.error.message}</span>}
          </div>
        </form>
      )}
    </div>
  )
}

function OutcomeForm({ proposal }: { proposal: Proposal }) {
  const record = useRecordOutcome(proposal.id)
  const { data: adrs } = useAdrs()
  const [adviceGiven, setAdviceGiven] = useState('')
  const [advisedBy, setAdvisedBy] = useState('')
  const [finalDecision, setFinalDecision] = useState('')
  const [resultingAdrId, setResultingAdrId] = useState('')

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    record.mutate({
      adviceGiven,
      advisedBy,
      finalDecision,
      resultingAdrId: resultingAdrId || null,
    })
  }

  return (
    <div className="card">
      <h2>Record the session outcome</h2>
      <form onSubmit={onSubmit}>
        <div className="form-grid">
          <div className="field full">
            <label htmlFor="advice">Advice given</label>
            <textarea id="advice" required value={adviceGiven} onChange={(e) => setAdviceGiven(e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="advised-by">Advised by (attendees)</label>
            <input id="advised-by" required maxLength={300} value={advisedBy} onChange={(e) => setAdvisedBy(e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="resulting-adr">Resulting ADR (optional)</label>
            <select id="resulting-adr" value={resultingAdrId} onChange={(e) => setResultingAdrId(e.target.value)}>
              <option value="">None</option>
              {(adrs ?? []).map((adr) => (
                <option key={adr.id} value={adr.id}>
                  ADR-{adr.adrNumber} — {adr.title}
                </option>
              ))}
            </select>
          </div>
          <div className="field full">
            <label htmlFor="final-decision">Final decision (taken by the proposer)</label>
            <textarea id="final-decision" required value={finalDecision} onChange={(e) => setFinalDecision(e.target.value)} />
          </div>
        </div>
        <div className="form-actions">
          <button className="button" type="submit" disabled={record.isPending}>
            Record outcome (decides proposal)
          </button>
          {record.error && <span className="error-box">{record.error.message}</span>}
        </div>
      </form>
    </div>
  )
}

function ArbitrationForm({ proposal }: { proposal: Proposal }) {
  const arbitrate = useArbitrate(proposal.id)
  const [requestedBy, setRequestedBy] = useState('')
  const [rationale, setRationale] = useState('')
  const [outcome, setOutcome] = useState('')
  const [decidedBy, setDecidedBy] = useState('')

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    arbitrate.mutate({ requestedBy, rationale, outcome, decidedBy })
  }

  return (
    <div className="card">
      <h2>Arbitration (group scope only — use sparingly)</h2>
      <p className="muted">
        Arbitration overrides the advice process for this proposal and permanently records who escalated, why, and
        who decided. It immediately marks the proposal as decided.
      </p>
      <form onSubmit={onSubmit}>
        <div className="form-grid">
          <div className="field">
            <label htmlFor="requested-by">Requested by</label>
            <input id="requested-by" required maxLength={200} value={requestedBy} onChange={(e) => setRequestedBy(e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="decided-by">Decided by</label>
            <input id="decided-by" required maxLength={200} value={decidedBy} onChange={(e) => setDecidedBy(e.target.value)} />
          </div>
          <div className="field full">
            <label htmlFor="rationale">Why is arbitration needed?</label>
            <textarea id="rationale" required value={rationale} onChange={(e) => setRationale(e.target.value)} />
          </div>
          <div className="field full">
            <label htmlFor="outcome">Arbitrated outcome</label>
            <textarea id="outcome" required value={outcome} onChange={(e) => setOutcome(e.target.value)} />
          </div>
        </div>
        <div className="form-actions">
          <button className="button" type="submit" disabled={arbitrate.isPending}>
            Record arbitration
          </button>
          {arbitrate.error && <span className="error-box">{arbitrate.error.message}</span>}
        </div>
      </form>
    </div>
  )
}
