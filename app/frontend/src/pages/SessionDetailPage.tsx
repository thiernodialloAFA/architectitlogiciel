import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useForumSession, useHoldSession } from '../api/hooks'
import { Badge, LoadState, PageHeader } from '../components/ui'

export default function SessionDetailPage() {
  const { id = '' } = useParams()
  const { data, isLoading, error } = useForumSession(id)

  return (
    <LoadState isLoading={isLoading} error={error}>
      {data && (
        <>
          <PageHeader
            title={data.session.title}
            subtitle={`Forum session on ${data.session.sessionDate}`}
            actions={<Badge value={data.session.status} />}
          />

          <div className="card">
            <h2>Agenda</h2>
            {data.agenda.length === 0 ? (
              <p className="muted">No proposals scheduled onto this session yet.</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Proposal</th>
                    <th>Scope</th>
                    <th>Status</th>
                    <th>Submitted by</th>
                  </tr>
                </thead>
                <tbody>
                  {data.agenda.map((proposal) => (
                    <tr key={proposal.id}>
                      <td>
                        <Link to={`/forum/proposals/${proposal.id}`}>{proposal.title}</Link>
                      </td>
                      <td>
                        <Badge value={proposal.scope} />
                      </td>
                      <td>
                        <Badge value={proposal.status} />
                      </td>
                      <td>{proposal.submittedBy}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          <div className="card">
            <h2>Session notes</h2>
            {data.session.notes ? <p className="prose">{data.session.notes}</p> : <p className="muted">No notes recorded.</p>}
            {data.session.status === 'PLANNED' && <HoldSessionForm sessionId={data.session.id} />}
          </div>
        </>
      )}
    </LoadState>
  )
}

function HoldSessionForm({ sessionId }: { sessionId: string }) {
  const hold = useHoldSession(sessionId)
  const [notes, setNotes] = useState('')

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    hold.mutate({ notes: notes || null })
  }

  return (
    <form onSubmit={onSubmit}>
      <div className="field">
        <label htmlFor="hold-notes">Notes from the session (optional)</label>
        <textarea id="hold-notes" value={notes} onChange={(e) => setNotes(e.target.value)} />
      </div>
      <div className="form-actions">
        <button className="button" type="submit" disabled={hold.isPending}>
          Mark session as held
        </button>
        {hold.error && <span className="error-box">{hold.error.message}</span>}
      </div>
    </form>
  )
}
