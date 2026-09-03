import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  useAddC4Version,
  useApplication,
  useAuditTrail,
  useC4Diagrams,
  useChecks,
  useCreateCheck,
  useRisks,
  useUpdateCheckStatus,
} from '../api/hooks'
import type { C4Diagram, CheckReference, CheckStatus, CheckType } from '../api/types'
import C4DiagramView from '../components/C4DiagramView'
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

          <C4Section applicationId={app.id} />

          <ChecksSection applicationId={app.id} />

          <div className="card">
            <h2>Audit trail</h2>
            <AuditTrail events={audit ?? []} />
          </div>
        </>
      )}
    </LoadState>
  )
}

function C4Section({ applicationId }: { applicationId: string }) {
  const { data: diagrams } = useC4Diagrams(applicationId)
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const latest = diagrams && diagrams.length > 0 ? diagrams[diagrams.length - 1] : null
  const selected = (diagrams ?? []).find((diagram) => diagram.id === selectedId) ?? latest

  return (
    <div className="card">
      <h2>C4 model</h2>
      {!diagrams || diagrams.length === 0 ? (
        <p className="muted">No C4 diagram recorded for this landscape entry yet.</p>
      ) : (
        <>
          <div className="filters">
            <select
              value={selected?.id ?? ''}
              onChange={(e) => setSelectedId(e.target.value)}
              aria-label="Diagram version"
            >
              {diagrams.map((diagram: C4Diagram) => (
                <option key={diagram.id} value={diagram.id}>
                  v{diagram.version} — {diagram.label} ({new Date(diagram.createdAt).toLocaleDateString()}, {diagram.author})
                </option>
              ))}
            </select>
          </div>
          {selected && <C4DiagramView source={selected.source} />}
        </>
      )}
      {showForm ? (
        <AddC4VersionForm applicationId={applicationId} onDone={() => setShowForm(false)} />
      ) : (
        <div className="form-actions">
          <button className="button secondary" type="button" onClick={() => setShowForm(true)}>
            Add new version
          </button>
        </div>
      )}
    </div>
  )
}

function AddC4VersionForm({ applicationId, onDone }: { applicationId: string; onDone: () => void }) {
  const addVersion = useAddC4Version(applicationId)
  const [label, setLabel] = useState('')
  const [source, setSource] = useState('')

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    addVersion.mutate({ label, source }, { onSuccess: onDone })
  }

  return (
    <form onSubmit={onSubmit}>
      <div className="form-grid">
        <div className="field">
          <label htmlFor="c4-label">Label</label>
          <input id="c4-label" required maxLength={200} value={label} onChange={(e) => setLabel(e.target.value)} />
        </div>
        <div className="field full">
          <label htmlFor="c4-source">Mermaid C4 source (versions are append-only)</label>
          <textarea
            id="c4-source"
            required
            rows={10}
            placeholder={'C4Context\n  title System context\n  Person(user, "User")\n  System(app, "This application")\n  Rel(user, app, "Uses")'}
            value={source}
            onChange={(e) => setSource(e.target.value)}
          />
        </div>
      </div>
      <div className="form-actions">
        <button className="button" type="submit" disabled={addVersion.isPending}>
          Save as new version
        </button>
        <button className="button secondary" type="button" onClick={onDone}>
          Cancel
        </button>
        {addVersion.error && <span className="error-box">{addVersion.error.message}</span>}
      </div>
    </form>
  )
}

function ChecksSection({ applicationId }: { applicationId: string }) {
  const { data: checks } = useChecks(applicationId)
  const [showForm, setShowForm] = useState(false)

  return (
    <div className="card">
      <h2>Fitness functions &amp; contract tests</h2>
      <p className="muted">
        The platform tracks that automated checks exist and their last reported status — it does not reimplement
        them. CI pushes results to <code>/api/checks/{'{id}'}/status</code>.
      </p>
      {!checks || checks.length === 0 ? (
        <p className="muted">No checks registered for this landscape entry.</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>Check</th>
              <th>Type</th>
              <th>Tool</th>
              <th>Last status</th>
              <th>Last run</th>
              <th>Report result</th>
            </tr>
          </thead>
          <tbody>
            {checks.map((check: CheckReference) => (
              <tr key={check.id}>
                <td>
                  {check.link ? <a href={check.link}>{check.name}</a> : check.name}
                  {check.description && <div className="muted">{check.description}</div>}
                </td>
                <td>
                  <Badge value={check.checkType} />
                </td>
                <td>{check.tool}</td>
                <td>
                  <Badge value={check.lastStatus} />
                </td>
                <td className="nowrap">{check.lastRunAt ? new Date(check.lastRunAt).toLocaleString() : 'never'}</td>
                <td className="nowrap">
                  <ReportStatusButtons checkId={check.id} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {showForm ? (
        <AddCheckForm applicationId={applicationId} onDone={() => setShowForm(false)} />
      ) : (
        <div className="form-actions">
          <button className="button secondary" type="button" onClick={() => setShowForm(true)}>
            Register a check
          </button>
        </div>
      )}
    </div>
  )
}

function ReportStatusButtons({ checkId }: { checkId: string }) {
  const update = useUpdateCheckStatus(checkId)
  const report = (status: CheckStatus) => update.mutate({ status, runAt: null })
  return (
    <>
      <button className="button secondary" type="button" disabled={update.isPending} onClick={() => report('PASSING')}>
        Passing
      </button>{' '}
      <button className="button secondary" type="button" disabled={update.isPending} onClick={() => report('FAILING')}>
        Failing
      </button>
    </>
  )
}

function AddCheckForm({ applicationId, onDone }: { applicationId: string; onDone: () => void }) {
  const createCheck = useCreateCheck(applicationId)
  const [name, setName] = useState('')
  const [checkType, setCheckType] = useState<CheckType>('FITNESS_FUNCTION')
  const [tool, setTool] = useState('')
  const [link, setLink] = useState('')
  const [description, setDescription] = useState('')

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    createCheck.mutate(
      { name, checkType, tool, link: link || null, description: description || null },
      { onSuccess: onDone },
    )
  }

  return (
    <form onSubmit={onSubmit}>
      <div className="form-grid">
        <div className="field">
          <label htmlFor="check-name">Name</label>
          <input id="check-name" required maxLength={300} value={name} onChange={(e) => setName(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="check-type">Type</label>
          <select id="check-type" value={checkType} onChange={(e) => setCheckType(e.target.value as CheckType)}>
            <option value="FITNESS_FUNCTION">Fitness function (e.g. ArchUnit, dependency-cruiser)</option>
            <option value="CONTRACT_TEST">Contract test (e.g. Pact)</option>
          </select>
        </div>
        <div className="field">
          <label htmlFor="check-tool">Tool</label>
          <input id="check-tool" required maxLength={100} value={tool} onChange={(e) => setTool(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="check-link">Link to CI job / broker (optional)</label>
          <input id="check-link" maxLength={500} value={link} onChange={(e) => setLink(e.target.value)} />
        </div>
        <div className="field full">
          <label htmlFor="check-description">Description (optional)</label>
          <input id="check-description" value={description} onChange={(e) => setDescription(e.target.value)} />
        </div>
      </div>
      <div className="form-actions">
        <button className="button" type="submit" disabled={createCheck.isPending}>
          Register check (status: Unknown until CI reports)
        </button>
        <button className="button secondary" type="button" onClick={onDone}>
          Cancel
        </button>
        {createCheck.error && <span className="error-box">{createCheck.error.message}</span>}
      </div>
    </form>
  )
}
