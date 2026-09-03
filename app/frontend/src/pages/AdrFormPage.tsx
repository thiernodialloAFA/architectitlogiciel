import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAdr, useCreateAdr, useRisks, useUpdateAdr } from '../api/hooks'
import type { Adr, Risk } from '../api/types'
import { LoadState, PageHeader } from '../components/ui'

interface FormState {
  title: string
  context: string
  decision: string
  consequences: string
  alternativesConsidered: string
  author: string
  department: string
  tags: string
  aiRelated: boolean
  linkedRiskIds: string[]
}

const EMPTY: FormState = {
  title: '',
  context: '',
  decision: '',
  consequences: '',
  alternativesConsidered: '',
  author: '',
  department: '',
  tags: '',
  aiRelated: false,
  linkedRiskIds: [],
}

function toFormState(adr: Adr): FormState {
  return {
    title: adr.title,
    context: adr.context,
    decision: adr.decision,
    consequences: adr.consequences ?? '',
    alternativesConsidered: adr.alternativesConsidered ?? '',
    author: adr.author,
    department: adr.department ?? '',
    tags: adr.tags ?? '',
    aiRelated: adr.aiRelated,
    linkedRiskIds: adr.linkedRisks.map((risk) => risk.id),
  }
}

export default function AdrFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const { data: existing, isLoading, error } = useAdr(id ?? '')
  const { data: risks } = useRisks()

  return (
    <>
      <PageHeader
        title={isEdit ? `Edit ADR-${existing?.adrNumber ?? ''}` : 'New Architecture Decision Record'}
        subtitle="New ADRs start as Proposed; every edit and status change is captured in the audit trail."
      />
      {isEdit ? (
        <LoadState isLoading={isLoading} error={error}>
          {existing && <AdrForm id={id} initial={toFormState(existing)} risks={risks ?? []} />}
        </LoadState>
      ) : (
        <AdrForm initial={EMPTY} risks={risks ?? []} />
      )}
    </>
  )
}

function AdrForm({ id, initial, risks }: { id?: string; initial: FormState; risks: Risk[] }) {
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const create = useCreateAdr()
  const update = useUpdateAdr(id ?? '')
  const mutation = isEdit ? update : create
  const [form, setForm] = useState<FormState>(initial)

  const set = <K extends keyof FormState>(key: K, value: FormState[K]) =>
    setForm((prev) => ({ ...prev, [key]: value }))

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    const shared = {
      title: form.title,
      context: form.context,
      decision: form.decision,
      consequences: form.consequences || null,
      alternativesConsidered: form.alternativesConsidered || null,
      department: form.department || null,
      tags: form.tags || null,
      aiRelated: form.aiRelated,
      linkedRiskIds: form.linkedRiskIds,
    }
    const onSuccess = (saved: { id: string }) => navigate(`/adrs/${saved.id}`)
    if (isEdit) {
      update.mutate(shared, { onSuccess })
    } else {
      create.mutate({ ...shared, author: form.author }, { onSuccess })
    }
  }

  const toggleRisk = (riskId: string) =>
    setForm((prev) => ({
      ...prev,
      linkedRiskIds: prev.linkedRiskIds.includes(riskId)
        ? prev.linkedRiskIds.filter((existingId) => existingId !== riskId)
        : [...prev.linkedRiskIds, riskId],
    }))

  return (
    <form onSubmit={onSubmit}>
      <div className="card">
        <div className="form-grid">
          <div className="field full">
            <label htmlFor="title">Title</label>
            <input id="title" required maxLength={300} value={form.title} onChange={(e) => set('title', e.target.value)} />
          </div>
          {!isEdit && (
            <div className="field">
              <label htmlFor="author">Author</label>
              <input id="author" required maxLength={200} value={form.author} onChange={(e) => set('author', e.target.value)} />
            </div>
          )}
          <div className="field">
            <label htmlFor="department">Department / domain</label>
            <input id="department" maxLength={200} value={form.department} onChange={(e) => set('department', e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="tags">Tags (comma-separated)</label>
            <input id="tags" value={form.tags} onChange={(e) => set('tags', e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="ai">AI-integration decision?</label>
            <select id="ai" value={form.aiRelated ? 'yes' : 'no'} onChange={(e) => set('aiRelated', e.target.value === 'yes')}>
              <option value="no">No</option>
              <option value="yes">Yes — show in AI Architecture Register</option>
            </select>
          </div>
          <div className="field full">
            <label htmlFor="context">Context</label>
            <textarea id="context" required value={form.context} onChange={(e) => set('context', e.target.value)} />
          </div>
          <div className="field full">
            <label htmlFor="decision">Decision</label>
            <textarea id="decision" required value={form.decision} onChange={(e) => set('decision', e.target.value)} />
          </div>
          <div className="field full">
            <label htmlFor="consequences">Consequences</label>
            <textarea id="consequences" value={form.consequences} onChange={(e) => set('consequences', e.target.value)} />
          </div>
          <div className="field full">
            <label htmlFor="alternatives">Alternatives considered</label>
            <textarea id="alternatives" value={form.alternativesConsidered} onChange={(e) => set('alternativesConsidered', e.target.value)} />
          </div>
        </div>
      </div>

      <div className="card">
        <h2>Linked risk-register entries</h2>
        {risks.map((risk) => (
          <label key={risk.id} style={{ display: 'block', marginBottom: '0.35rem' }}>
            <input type="checkbox" checked={form.linkedRiskIds.includes(risk.id)} onChange={() => toggleRisk(risk.id)} /> {risk.title}{' '}
            <span className="muted">({risk.applicationName})</span>
          </label>
        ))}
        <div className="form-actions">
          <button className="button" type="submit" disabled={mutation.isPending}>
            {isEdit ? 'Save changes' : 'Create ADR (status: Proposed)'}
          </button>
          <button className="button secondary" type="button" onClick={() => navigate(-1)}>
            Cancel
          </button>
          {mutation.error && <span className="error-box">{mutation.error.message}</span>}
        </div>
      </div>
    </form>
  )
}
