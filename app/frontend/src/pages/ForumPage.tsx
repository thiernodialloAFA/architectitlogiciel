import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useArbitrations, useCreateSession, useForumSessions, useProposals } from '../api/hooks'
import { Badge, LoadState, PageHeader } from '../components/ui'

export default function ForumPage() {
  const sessions = useForumSessions()
  const [statusFilter, setStatusFilter] = useState('')
  const proposals = useProposals(statusFilter ? { status: statusFilter } : undefined)
  const arbitrations = useArbitrations()

  return (
    <>
      <PageHeader
        title="Architecture Advice Forum"
        subtitle="Advice process (proposal §3.3): anyone may propose, scoped advice is sought in a forum session, the proposer decides — and group-scope escalations get an explicit, rarely used arbitration record."
        actions={
          <Link className="button" to="/forum/proposals/new">
            New proposal
          </Link>
        }
      />

      <div className="card">
        <h2>Forum sessions</h2>
        <LoadState isLoading={sessions.isLoading} error={sessions.error}>
          {sessions.data && sessions.data.length === 0 ? (
            <p className="muted">No sessions planned yet.</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Session</th>
                  <th>Status</th>
                  <th>Agenda items</th>
                </tr>
              </thead>
              <tbody>
                {(sessions.data ?? []).map((session) => (
                  <tr key={session.id}>
                    <td className="nowrap">{session.sessionDate}</td>
                    <td>
                      <Link to={`/forum/sessions/${session.id}`}>{session.title}</Link>
                    </td>
                    <td>
                      <Badge value={session.status} />
                    </td>
                    <td>{session.agendaSize}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </LoadState>
        <PlanSessionForm />
      </div>

      <div className="card">
        <h2>Proposals</h2>
        <div className="filters">
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} aria-label="Filter by status">
            <option value="">All statuses</option>
            <option value="SUBMITTED">Submitted</option>
            <option value="SCHEDULED">Scheduled</option>
            <option value="DECIDED">Decided</option>
          </select>
        </div>
        <LoadState isLoading={proposals.isLoading} error={proposals.error}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Proposal</th>
                <th>Scope</th>
                <th>Status</th>
                <th>Submitted by</th>
                <th>Session</th>
              </tr>
            </thead>
            <tbody>
              {(proposals.data ?? []).map((proposal) => (
                <tr key={proposal.id}>
                  <td>
                    <Link to={`/forum/proposals/${proposal.id}`}>{proposal.title}</Link>{' '}
                    {proposal.arbitrated && <Badge value="GROUP" />}
                  </td>
                  <td>
                    <Badge value={proposal.scope} />
                  </td>
                  <td>
                    <Badge value={proposal.status} />
                  </td>
                  <td>{proposal.submittedBy}</td>
                  <td className="nowrap">{proposal.session ? proposal.session.sessionDate : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {(proposals.data ?? []).length === 0 && <p className="muted">No proposals match this filter.</p>}
        </LoadState>
      </div>

      <div className="card">
        <h2>Arbitration log</h2>
        <p className="muted">
          Escalations are the exception, not the process: only group-scope proposals can be arbitrated, and every
          arbitration is recorded here permanently.
        </p>
        <LoadState isLoading={arbitrations.isLoading} error={arbitrations.error}>
          {(arbitrations.data ?? []).length === 0 ? (
            <p className="muted">No arbitrations recorded.</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Proposal</th>
                  <th>Requested by</th>
                  <th>Decided by</th>
                  <th>When</th>
                </tr>
              </thead>
              <tbody>
                {(arbitrations.data ?? []).map((arbitration) => (
                  <tr key={arbitration.id}>
                    <td>
                      <Link to={`/forum/proposals/${arbitration.proposalId}`}>{arbitration.proposalTitle}</Link>
                    </td>
                    <td>{arbitration.requestedBy}</td>
                    <td>{arbitration.decidedBy}</td>
                    <td className="nowrap">{new Date(arbitration.occurredAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </LoadState>
      </div>
    </>
  )
}

function PlanSessionForm() {
  const create = useCreateSession()
  const [sessionDate, setSessionDate] = useState('')
  const [title, setTitle] = useState('')

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    create.mutate(
      { sessionDate, title, notes: null },
      {
        onSuccess: () => {
          setSessionDate('')
          setTitle('')
        },
      },
    )
  }

  return (
    <form onSubmit={onSubmit}>
      <h3>Plan a session</h3>
      <div className="form-grid">
        <div className="field">
          <label htmlFor="session-date">Date</label>
          <input id="session-date" type="date" required value={sessionDate} onChange={(e) => setSessionDate(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="session-title">Title</label>
          <input id="session-title" required maxLength={300} value={title} onChange={(e) => setTitle(e.target.value)} />
        </div>
      </div>
      <div className="form-actions">
        <button className="button" type="submit" disabled={create.isPending}>
          Plan session
        </button>
        {create.error && <span className="error-box">{create.error.message}</span>}
      </div>
    </form>
  )
}
