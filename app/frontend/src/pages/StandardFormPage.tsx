import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAdrs, useApplications, useSaveStandard, useStandard } from '../api/hooks'
import type { Adr, Application, Standard, StandardCategory } from '../api/types'
import { LoadState, PageHeader } from '../components/ui'

interface FormState {
  title: string
  category: StandardCategory
  content: string
  owner: string
  linkedAdrIds: string[]
  appliedApplicationIds: string[]
}

const EMPTY: FormState = {
  title: '',
  category: 'INTEGRATION',
  content: '',
  owner: '',
  linkedAdrIds: [],
  appliedApplicationIds: [],
}

function toFormState(standard: Standard): FormState {
  return {
    title: standard.title,
    category: standard.category,
    content: standard.content,
    owner: standard.owner,
    linkedAdrIds: standard.linkedAdrs.map((adr) => adr.id),
    appliedApplicationIds: standard.appliedApplications.map((app) => app.id),
  }
}

export default function StandardFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const { data: existing, isLoading, error } = useStandard(id ?? '')
  const { data: adrs } = useAdrs()
  const { data: applications } = useApplications()

  return (
    <>
      <PageHeader
        title={isEdit ? 'Edit standard' : 'New standard'}
        subtitle={
          isEdit
            ? 'Changing the content bumps the version; the previous content stays in the audit trail as version history.'
            : 'New standards start as Draft; activate them once agreed via the advice process.'
        }
      />
      {isEdit ? (
        <LoadState isLoading={isLoading} error={error}>
          {existing && (
            <StandardForm id={id} initial={toFormState(existing)} adrs={adrs ?? []} applications={applications ?? []} />
          )}
        </LoadState>
      ) : (
        <StandardForm initial={EMPTY} adrs={adrs ?? []} applications={applications ?? []} />
      )}
    </>
  )
}

function StandardForm({
  id,
  initial,
  adrs,
  applications,
}: {
  id?: string
  initial: FormState
  adrs: Adr[]
  applications: Application[]
}) {
  const navigate = useNavigate()
  const save = useSaveStandard(id)
  const [form, setForm] = useState<FormState>(initial)

  const set = <K extends keyof FormState>(key: K, value: FormState[K]) =>
    setForm((prev) => ({ ...prev, [key]: value }))

  const toggle = (key: 'linkedAdrIds' | 'appliedApplicationIds', value: string) =>
    setForm((prev) => ({
      ...prev,
      [key]: prev[key].includes(value) ? prev[key].filter((entry) => entry !== value) : [...prev[key], value],
    }))

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    save.mutate(form, { onSuccess: (saved) => navigate(`/standards/${saved.id}`) })
  }

  return (
    <form onSubmit={onSubmit}>
      <div className="card">
        <div className="form-grid">
          <div className="field full">
            <label htmlFor="title">Title</label>
            <input id="title" required maxLength={300} value={form.title} onChange={(e) => set('title', e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="category">Category</label>
            <select id="category" value={form.category} onChange={(e) => set('category', e.target.value as StandardCategory)}>
              <option value="INTEGRATION">Integration</option>
              <option value="API_CONVENTION">API convention</option>
              <option value="AI_PATTERN">AI pattern</option>
              <option value="SECURITY">Security</option>
              <option value="DATA">Data</option>
            </select>
          </div>
          <div className="field">
            <label htmlFor="owner">Owner</label>
            <input id="owner" required maxLength={200} value={form.owner} onChange={(e) => set('owner', e.target.value)} />
          </div>
          <div className="field full">
            <label htmlFor="content">Content (the standard itself)</label>
            <textarea id="content" required rows={10} value={form.content} onChange={(e) => set('content', e.target.value)} />
          </div>
        </div>
      </div>

      <div className="card">
        <h2>Backing ADRs</h2>
        {adrs.map((adr) => (
          <label key={adr.id} style={{ display: 'block', marginBottom: '0.35rem' }}>
            <input
              type="checkbox"
              checked={form.linkedAdrIds.includes(adr.id)}
              onChange={() => toggle('linkedAdrIds', adr.id)}
            />{' '}
            ADR-{adr.adrNumber} — {adr.title}
          </label>
        ))}
      </div>

      <div className="card">
        <h2>Applied by landscape entries</h2>
        {applications.map((app) => (
          <label key={app.id} style={{ display: 'block', marginBottom: '0.35rem' }}>
            <input
              type="checkbox"
              checked={form.appliedApplicationIds.includes(app.id)}
              onChange={() => toggle('appliedApplicationIds', app.id)}
            />{' '}
            {app.name} <span className="muted">({app.ownerTeam})</span>
          </label>
        ))}
        <div className="form-actions">
          <button className="button" type="submit" disabled={save.isPending}>
            {id ? 'Save (bumps version on content change)' : 'Create standard (status: Draft)'}
          </button>
          <button className="button secondary" type="button" onClick={() => navigate(-1)}>
            Cancel
          </button>
          {save.error && <span className="error-box">{save.error.message}</span>}
        </div>
      </div>
    </form>
  )
}
