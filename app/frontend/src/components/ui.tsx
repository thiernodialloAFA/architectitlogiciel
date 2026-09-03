import type { ReactNode } from 'react'
import type { AuditEvent } from '../api/types'

const BADGE_TONES: Record<string, string> = {
  // lifecycle
  ACTIVE: 'tone-green',
  PILOT: 'tone-blue',
  PLANNED: 'tone-blue',
  DEPRECATED: 'tone-orange',
  RETIRED: 'tone-gray',
  // risk status
  OPEN: 'tone-red',
  IN_PROGRESS: 'tone-blue',
  MITIGATED: 'tone-green',
  ACCEPTED: 'tone-green',
  CLOSED: 'tone-gray',
  // adr status
  PROPOSED: 'tone-blue',
  SUPERSEDED: 'tone-gray',
  // ratings
  LOW: 'tone-green',
  MEDIUM: 'tone-orange',
  HIGH: 'tone-red',
  // treatment (TIME)
  TOLERATE: 'tone-gray',
  INVEST: 'tone-blue',
  MIGRATE: 'tone-orange',
  ELIMINATE: 'tone-red',
  // advice forum
  SUBMITTED: 'tone-blue',
  SCHEDULED: 'tone-orange',
  DECIDED: 'tone-green',
  HELD: 'tone-green',
  TEAM: 'tone-gray',
  DOMAIN: 'tone-blue',
  GROUP: 'tone-purple',
  // standards
  DRAFT: 'tone-blue',
  INTEGRATION: 'tone-blue',
  API_CONVENTION: 'tone-purple',
  AI_PATTERN: 'tone-purple',
  SECURITY: 'tone-red',
  DATA: 'tone-orange',
  // checks
  PASSING: 'tone-green',
  FAILING: 'tone-red',
  UNKNOWN: 'tone-gray',
  FITNESS_FUNCTION: 'tone-blue',
  CONTRACT_TEST: 'tone-purple',
  // adr source
  REPOSITORY: 'tone-orange',
  PLATFORM: 'tone-gray',
}

export function Badge({ value }: { value: string }) {
  const tone = BADGE_TONES[value] ?? 'tone-gray'
  return <span className={`badge ${tone}`}>{value.replaceAll('_', ' ')}</span>
}

export function StaleBadge() {
  return (
    <span className="badge tone-red" title="Past its review cadence — needs re-assessment">
      STALE
    </span>
  )
}

export function AiBadge() {
  return (
    <span className="badge tone-purple" title="AI-integration decision (AI Architecture Register)">
      AI
    </span>
  )
}

export function PageHeader({ title, subtitle, actions }: { title: string; subtitle?: string; actions?: ReactNode }) {
  return (
    <header className="page-header">
      <div>
        <h1>{title}</h1>
        {subtitle && <p className="subtitle">{subtitle}</p>}
      </div>
      {actions && <div className="page-actions">{actions}</div>}
    </header>
  )
}

export function LoadState({ isLoading, error, children }: { isLoading: boolean; error: Error | null; children: ReactNode }) {
  if (isLoading) return <p className="muted">Loading…</p>
  if (error) return <p className="error-box">Failed to load: {error.message}</p>
  return <>{children}</>
}

export function AuditTrail({ events }: { events: AuditEvent[] }) {
  if (events.length === 0) return <p className="muted">No audit events.</p>
  return (
    <table className="data-table audit-table">
      <thead>
        <tr>
          <th>When</th>
          <th>Action</th>
          <th>Field</th>
          <th>Old → New</th>
          <th>Actor</th>
        </tr>
      </thead>
      <tbody>
        {events.map((event) => (
          <tr key={event.id}>
            <td className="nowrap">{new Date(event.occurredAt).toLocaleString()}</td>
            <td>
              <Badge value={event.action} />
            </td>
            <td>{event.fieldName ?? '—'}</td>
            <td className="audit-values">
              {event.action === 'CREATED' ? (
                <span>{event.newValue}</span>
              ) : (
                <span>
                  <s>{event.oldValue ?? '∅'}</s> → {event.newValue ?? '∅'}
                </span>
              )}
            </td>
            <td>{event.actor}</td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
