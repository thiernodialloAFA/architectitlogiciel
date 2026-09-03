import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAdrs, useApplications, useCreateProposal } from '../api/hooks'
import type { AafScope } from '../api/types'
import { PageHeader } from '../components/ui'

export default function ProposalFormPage() {
  const navigate = useNavigate()
  const create = useCreateProposal()
  const { data: applications } = useApplications()
  const { data: adrs } = useAdrs()

  const [title, setTitle] = useState('')
  const [summary, setSummary] = useState('')
  const [scope, setScope] = useState<AafScope>('TEAM')
  const [submittedBy, setSubmittedBy] = useState('')
  const [department, setDepartment] = useState('')
  const [applicationId, setApplicationId] = useState('')
  const [adrId, setAdrId] = useState('')

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    create.mutate(
      {
        title,
        summary,
        scope,
        submittedBy,
        department: department || null,
        adrId: adrId || null,
        applicationId: applicationId || null,
      },
      { onSuccess: (saved) => navigate(`/forum/proposals/${saved.id}`) },
    )
  }

  return (
    <>
      <PageHeader
        title="New advice-forum proposal"
        subtitle="Declare the scope honestly: team-scope decisions need no forum slot, domain and group scope are brought to the next session for advice — the proposer still decides."
      />
      <form onSubmit={onSubmit}>
        <div className="card">
          <div className="form-grid">
            <div className="field full">
              <label htmlFor="title">Title</label>
              <input id="title" required maxLength={300} value={title} onChange={(e) => setTitle(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="scope">Declared scope</label>
              <select id="scope" value={scope} onChange={(e) => setScope(e.target.value as AafScope)}>
                <option value="TEAM">Team — affects only the submitting team</option>
                <option value="DOMAIN">Domain — affects one business domain</option>
                <option value="GROUP">Group — affects several domains / the whole group</option>
              </select>
            </div>
            <div className="field">
              <label htmlFor="submitted-by">Submitted by</label>
              <input id="submitted-by" required maxLength={200} value={submittedBy} onChange={(e) => setSubmittedBy(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="department">Department / domain</label>
              <input id="department" maxLength={200} value={department} onChange={(e) => setDepartment(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="application">Related landscape entry (optional)</label>
              <select id="application" value={applicationId} onChange={(e) => setApplicationId(e.target.value)}>
                <option value="">None</option>
                {(applications ?? []).map((app) => (
                  <option key={app.id} value={app.id}>
                    {app.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="field">
              <label htmlFor="adr">Related draft ADR (optional)</label>
              <select id="adr" value={adrId} onChange={(e) => setAdrId(e.target.value)}>
                <option value="">None</option>
                {(adrs ?? []).map((adr) => (
                  <option key={adr.id} value={adr.id}>
                    ADR-{adr.adrNumber} — {adr.title}
                  </option>
                ))}
              </select>
            </div>
            <div className="field full">
              <label htmlFor="summary">Summary of the decision you intend to take</label>
              <textarea id="summary" required value={summary} onChange={(e) => setSummary(e.target.value)} />
            </div>
          </div>
          <div className="form-actions">
            <button className="button" type="submit" disabled={create.isPending}>
              Submit proposal
            </button>
            <button className="button secondary" type="button" onClick={() => navigate(-1)}>
              Cancel
            </button>
            {create.error && <span className="error-box">{create.error.message}</span>}
          </div>
        </div>
      </form>
    </>
  )
}
