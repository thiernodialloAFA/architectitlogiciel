import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useApplications, useRisk, useSaveRisk } from '../api/hooks'
import type { Application, RatingLevel, Risk, RiskCategory, RiskRequest, RiskStatus, TreatmentDecision } from '../api/types'
import { LoadState, PageHeader } from '../components/ui'

const EMPTY: RiskRequest = {
  applicationId: '',
  title: '',
  category: 'TECHNICAL_DEBT',
  description: '',
  currentControls: null,
  inherentImpact: 'MEDIUM',
  inherentLikelihood: 'MEDIUM',
  residualImpact: 'MEDIUM',
  residualLikelihood: 'MEDIUM',
  blastRadius: null,
  treatmentDecision: 'INVEST',
  costToFix: null,
  targetDate: null,
  status: 'OPEN',
  riskOwner: '',
  evidenceLink: null,
  reviewCadenceDays: 90,
  lastAssessedAt: new Date().toISOString().slice(0, 10),
}

function toRequest(risk: Risk): RiskRequest {
  return {
    applicationId: risk.applicationId,
    title: risk.title,
    category: risk.category,
    description: risk.description,
    currentControls: risk.currentControls,
    inherentImpact: risk.inherentImpact,
    inherentLikelihood: risk.inherentLikelihood,
    residualImpact: risk.residualImpact,
    residualLikelihood: risk.residualLikelihood,
    blastRadius: risk.blastRadius,
    treatmentDecision: risk.treatmentDecision,
    costToFix: risk.costToFix,
    targetDate: risk.targetDate,
    status: risk.status,
    riskOwner: risk.riskOwner,
    evidenceLink: risk.evidenceLink,
    reviewCadenceDays: risk.reviewCadenceDays,
    lastAssessedAt: risk.lastAssessedAt,
  }
}

export default function RiskFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const { data: applications } = useApplications()
  const { data: existing, isLoading, error } = useRisk(id ?? '')

  return (
    <>
      <PageHeader
        title={isEdit ? 'Re-assess risk entry' : 'New risk entry'}
        subtitle="Every save is recorded in the append-only audit trail with old and new values."
      />
      {isEdit ? (
        <LoadState isLoading={isLoading} error={error}>
          {existing && <RiskForm id={id} initial={toRequest(existing)} applications={applications ?? []} />}
        </LoadState>
      ) : (
        <RiskForm initial={EMPTY} applications={applications ?? []} />
      )}
    </>
  )
}

function RiskForm({ id, initial, applications }: { id?: string; initial: RiskRequest; applications: Application[] }) {
  const navigate = useNavigate()
  const save = useSaveRisk(id)
  const [form, setForm] = useState<RiskRequest>(initial)
  const isEdit = Boolean(id)

  const set = <K extends keyof RiskRequest>(key: K, value: RiskRequest[K]) =>
    setForm((prev) => ({ ...prev, [key]: value }))

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    save.mutate(form, {
      onSuccess: (saved) => navigate(`/risks/${saved.id}`),
    })
  }

  return (
    <form onSubmit={onSubmit}>
      <div className="card">
        <h2>Identification</h2>
        <div className="form-grid">
          <div className="field full">
            <label htmlFor="title">Title</label>
            <input id="title" required maxLength={300} value={form.title} onChange={(e) => set('title', e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="application">System / application</label>
            <select id="application" required value={form.applicationId} onChange={(e) => set('applicationId', e.target.value)}>
              <option value="" disabled>
                Select an application…
              </option>
              {applications.map((app) => (
                <option key={app.id} value={app.id}>
                  {app.name}
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="category">Category</label>
            <select id="category" value={form.category} onChange={(e) => set('category', e.target.value as RiskCategory)}>
              {['TECHNICAL_DEBT', 'END_OF_LIFE', 'INTEGRATION_BRITTLENESS', 'SECURITY_COMPLIANCE', 'AI_READINESS'].map((c) => (
                <option key={c} value={c}>
                  {c.replaceAll('_', ' ')}
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="riskOwner">Risk owner (accountable for treating this risk)</label>
            <input id="riskOwner" required maxLength={200} value={form.riskOwner} onChange={(e) => set('riskOwner', e.target.value)} />
          </div>
          <div className="field full">
            <label htmlFor="description">Description — what, specifically, is wrong</label>
            <textarea id="description" required value={form.description} onChange={(e) => set('description', e.target.value)} />
          </div>
          <div className="field full">
            <label htmlFor="controls">Current controls</label>
            <textarea id="controls" value={form.currentControls ?? ''} onChange={(e) => set('currentControls', e.target.value || null)} />
          </div>
          <div className="field full">
            <label htmlFor="blast">Blast radius</label>
            <textarea id="blast" value={form.blastRadius ?? ''} onChange={(e) => set('blastRadius', e.target.value || null)} />
          </div>
        </div>
      </div>

      <div className="card">
        <h2>Assessment</h2>
        <div className="form-grid">
          {(
            [
              ['inherentImpact', 'Inherent impact'],
              ['inherentLikelihood', 'Inherent likelihood'],
              ['residualImpact', 'Residual impact'],
              ['residualLikelihood', 'Residual likelihood'],
            ] as const
          ).map(([key, label]) => (
            <div className="field" key={key}>
              <label htmlFor={key}>{label}</label>
              <select id={key} value={form[key]} onChange={(e) => set(key, e.target.value as RatingLevel)}>
                {['LOW', 'MEDIUM', 'HIGH'].map((level) => (
                  <option key={level} value={level}>
                    {level}
                  </option>
                ))}
              </select>
            </div>
          ))}
          <div className="field">
            <label htmlFor="treatment">Treatment decision (TIME)</label>
            <select id="treatment" value={form.treatmentDecision} onChange={(e) => set('treatmentDecision', e.target.value as TreatmentDecision)}>
              {['TOLERATE', 'INVEST', 'MIGRATE', 'ELIMINATE'].map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="status">Status</label>
            <select id="status" value={form.status} onChange={(e) => set('status', e.target.value as RiskStatus)}>
              {['OPEN', 'IN_PROGRESS', 'MITIGATED', 'ACCEPTED', 'CLOSED'].map((s) => (
                <option key={s} value={s}>
                  {s.replaceAll('_', ' ')}
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="cost">Cost to fix (estimate)</label>
            <input id="cost" maxLength={100} value={form.costToFix ?? ''} onChange={(e) => set('costToFix', e.target.value || null)} />
          </div>
          <div className="field">
            <label htmlFor="target">Target date</label>
            <input id="target" type="date" value={form.targetDate ?? ''} onChange={(e) => set('targetDate', e.target.value || null)} />
          </div>
          <div className="field">
            <label htmlFor="evidence">Evidence link</label>
            <input id="evidence" type="url" value={form.evidenceLink ?? ''} onChange={(e) => set('evidenceLink', e.target.value || null)} />
          </div>
          <div className="field">
            <label htmlFor="cadence">Review cadence (days)</label>
            <input
              id="cadence"
              type="number"
              min={1}
              max={3650}
              value={form.reviewCadenceDays}
              onChange={(e) => set('reviewCadenceDays', Number(e.target.value))}
            />
          </div>
          <div className="field">
            <label htmlFor="assessed">Last assessed</label>
            <input id="assessed" type="date" required value={form.lastAssessedAt} onChange={(e) => set('lastAssessedAt', e.target.value)} />
          </div>
        </div>
        <div className="form-actions">
          <button className="button" type="submit" disabled={save.isPending}>
            {isEdit ? 'Save re-assessment' : 'Create risk entry'}
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
